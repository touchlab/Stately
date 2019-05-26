/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "SQLiteConnection"

#include "Assert.h"
#include "Memory.h"
#include "Natives.h"
#include "KString.h"
#include "Porting.h"
#include "Types.h"

#include "utf8.h"

#include <stdlib.h>

#include <string.h>
#include <unistd.h>
#include "KonanHelper.h"

#include <sqlite3.h>

#include "SQLiteCommon.h"

// Set to 1 to use UTF16 storage for localized indexes.
#define UTF16_STORAGE 0

namespace android {

struct SQLiteConnection {
    // Open flags.
    // Must be kept in sync with the constants defined in SQLiteDatabase.java.
    enum {
        OPEN_READWRITE          = 0x00000000,
        OPEN_READONLY           = 0x00000001,
        OPEN_READ_MASK          = 0x00000001,
        NO_LOCALIZED_COLLATORS  = 0x00000010,
        CREATE_IF_NECESSARY     = 0x10000000,
    };

    sqlite3* const db;
    const int openFlags;
    char* path;
    char* label;

    volatile bool canceled;

    SQLiteConnection(sqlite3* db, int openFlags, char* path, char* label) :
        db(db), openFlags(openFlags), path(path), label(label), canceled(false) { }

        ~SQLiteConnection(){
        if(path != nullptr)
            DisposeCStringHelper(path);
        if(label != nullptr)
            DisposeCStringHelper(label);
    }
};

// Called each time a statement begins execution, when tracing is enabled.
static void sqliteTraceCallback(void *data, const char *sql) {
    SQLiteConnection* connection = static_cast<SQLiteConnection*>(data);
    ALOGV("%s: \"%s\"\n",
            connection->label, sql);
}

// Called each time a statement finishes execution, when profiling is enabled.
static void sqliteProfileCallback(void *data, const char *sql, sqlite3_uint64 tm) {
    SQLiteConnection* connection = static_cast<SQLiteConnection*>(data);
    ALOGV("%s: \"%s\" took %0.3f ms\n",
            connection->label, sql, tm * 0.000001f);
}

static int executeNonQuery(SQLiteConnection* connection, sqlite3_stmt* statement) {
    int err = sqlite3_step(statement);
    if (err == SQLITE_ROW) {
        throw_sqlite3_exception(
                "Queries can be performed using SQLiteDatabase query or rawQuery methods only.");
    } else if (err != SQLITE_DONE) {
        throw_sqlite3_exception( connection->db);
    }
    return err;
}

static int nativeBindParameterIndex(KLong statementPtr, KString paramName)
{
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    size_t utf8Size;

    char * value = CreateCStringFromStringWithSize(paramName, &utf8Size);

    int result = sqlite3_bind_parameter_index(statement, reinterpret_cast<const char*>(value));

    DisposeCStringHelper(value);

    return result;
}

extern "C"{

KInt SQLiter_SQLiteConnection_nativeBindParameterIndex(KLong statementPtr, KString paramName)
{
    return nativeBindParameterIndex(statementPtr, paramName);
}

KBoolean SQLiter_SQLiteConnection_nativeStep(KLong connectionPtr, KLong statementPtr)
{
    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    int retryCount = 0;
    bool gotException = false;
    while (!gotException) {
        int err = sqlite3_step(statement);
        if (err == SQLITE_ROW) {
            return true;
        } else if (err == SQLITE_DONE) {
            return false;
        } else if (err == SQLITE_LOCKED || err == SQLITE_BUSY) {
            // The table is locked, retry
//            LOG_WINDOW("Database locked, retrying");
            if (retryCount > 50) {
//                ALOGE("Bailing on database busy retry");
                throw_sqlite3_exception(connection->db, "retrycount exceeded");
                gotException = true;
            } else {
                // Sleep to give the thread holding the lock a chance to finish
                usleep(1000);
                retryCount++;
            }
        } else {
            throw_sqlite3_exception(connection->db);
            gotException = true;
        }
    }

    return false;
}

KBoolean SQLiter_SQLiteConnection_nativeColumnIsNull(KLong statementPtr, KInt columnIndex)
{
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    int type = sqlite3_column_type(statement, columnIndex);
    return type == SQLITE_NULL;
}

KLong SQLiter_SQLiteConnection_nativeColumnGetLong(KLong statementPtr, KInt columnIndex)
{
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    return sqlite3_column_int64(statement, columnIndex);
}

KDouble SQLiter_SQLiteConnection_nativeColumnGetDouble(KLong statementPtr, KInt columnIndex)
{
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    return sqlite3_column_double(statement, columnIndex);
}

KInt SQLiter_SQLiteConnection_nativeColumnCount(KLong statementPtr)
{
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    return sqlite3_column_count(statement);
}

KInt SQLiter_SQLiteConnection_nativeColumnType(KLong statementPtr, KInt columnIndex)
{
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    return sqlite3_column_type(statement, columnIndex);
}

void SQLiter_SQLiteConnection_nativeResetStatement(KLong connectionPtr, KLong statementPtr) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = sqlite3_reset(statement);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteConnection_nativeClearBindings(KLong connectionPtr, KLong statementPtr) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = sqlite3_clear_bindings(statement);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

OBJ_GETTER(SQLiter_SQLiteConnection_nativeColumnName, KLong statementPtr, KInt columnIndex) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    auto colName = sqlite3_column_name(statement, columnIndex);

    RETURN_RESULT_OF(CreateStringFromUtf8, colName, strlen(colName));
}

OBJ_GETTER(SQLiter_SQLiteConnection_nativeColumnGetString, KLong statementPtr, KInt columnIndex) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    int colSize = sqlite3_column_bytes(statement, columnIndex);

    if (colSize <= 0) {
        RETURN_RESULT_OF(CreateStringFromUtf8, "", 0);
    }
    // Convert to UTF-16 here instead of calling NewStringUTF.  NewStringUTF
    // doesn't like UTF-8 strings with high codepoints.  It actually expects
    // Modified UTF-8 with encoded surrogate pairs.
    RETURN_RESULT_OF(CreateStringFromUtf8, reinterpret_cast<const char*>(sqlite3_column_text(statement, columnIndex)), colSize);
}

OBJ_GETTER(SQLiter_SQLiteConnection_nativeColumnGetBlob, KLong statementPtr, KInt columnIndex) {

    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);
    int colSize = sqlite3_column_bytes(statement, columnIndex);

    if (colSize < 0) {
        throw_sqlite3_exception("Byte array size/type issue");
    }

    ArrayHeader *result = AllocArrayInstance(
            theByteArrayTypeInfo, colSize, OBJ_RESULT)->array();

    //TODO: How to check if array properly created?
    /*if (!byteArray) {
    env->ExceptionClear();
    throw_sqlite3_exception(env, "Native could not create new byte[]");
    RETURN_OBJ(nullptr);
}*/
    memcpy(PrimitiveArrayAddressOfElementAt<KByte>(result, 0),
           sqlite3_column_blob(statement, columnIndex),
           colSize);

    RETURN_OBJ(result->obj());
}

KLong SQLiter_SQLiteConnection_nativePrepareStatement(KLong connectionPtr, KString sqlString) {

    RuntimeAssert(sqlString->type_info() == theStringTypeInfo, "Must use a string");

    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);

    KInt sqlLength = sqlString->count_;

    const KChar* sql = CharArrayAddressOfElementAt(sqlString, 0);

    sqlite3_stmt* statement;
    int err = sqlite3_prepare16_v2(connection->db,
                                   sql, sqlLength * sizeof(KChar), &statement, NULL);

    if (err != SQLITE_OK) {
        // Error messages like 'near ")": syntax error' are not
        // always helpful enough, so construct an error string that
        // includes the query itself.

        std::string str;

        size_t utf8size;
        str.append(", while compiling: ");

        char* hardString = CreateCStringFromStringWithSize(sqlString, &utf8size);
        str.append(const_cast<const char *>(hardString));
        DisposeCStringHelper(hardString);

        throw_sqlite3_exception(connection->db, str.c_str());
        return 0;
    }

    ALOGV("Prepared statement %p on connection %p", statement, connection->db);
    return reinterpret_cast<KLong>(statement);
}

void SQLiter_SQLiteConnection_nativeClose(KLong connectionPtr) {
    SQLiteConnection* connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);

    if (connection) {
        ALOGV("Closing connection %p", connection->db);
        int err = sqlite3_close(connection->db);
        if (err != SQLITE_OK) {
            // This can happen if sub-objects aren't closed first.  Make sure the caller knows.
            ALOGE("sqlite3_close(%p) failed: %d", connection->db, err);
            throw_sqlite3_exception( connection->db, "Count not close db.");
            return;
        }

        delete connection;
    }
}

KLong SQLiter_SQLiteConnection_nativeOpen(KString pathStr, KInt openFlags,
                        KString labelStr, KBoolean enableTrace, KBoolean enableProfile, KInt lookasideSz,
                        KInt lookasideCnt, KInt busyTimeout) {

    RuntimeAssert(pathStr->type_info() == theStringTypeInfo, "Must use a string");
    RuntimeAssert(labelStr->type_info() == theStringTypeInfo, "Must use a string");

    int sqliteFlags;
    if (openFlags & SQLiteConnection::CREATE_IF_NECESSARY) {
        sqliteFlags = SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE;
    } else if (openFlags & SQLiteConnection::OPEN_READONLY) {
        sqliteFlags = SQLITE_OPEN_READONLY;
    } else {
        sqliteFlags = SQLITE_OPEN_READWRITE;
    }

    // This ensures that regardless of how sqlite was compiled it will support uri file paths.
    // this is important for using in memory databases.
    sqliteFlags |= SQLITE_OPEN_URI;

    size_t utf8Size;
    char * path = CreateCStringFromStringWithSize(pathStr, &utf8Size);
    char * label = CreateCStringFromStringWithSize(labelStr, &utf8Size);

    sqlite3* db;
    int err = sqlite3_open_v2(path, &db, sqliteFlags, NULL);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception_errcode(err, "Could not open database");
        return 0;
    }

    if (lookasideSz >= 0 && lookasideCnt >= 0) {
        int err = sqlite3_db_config(db, SQLITE_DBCONFIG_LOOKASIDE, NULL, lookasideSz, lookasideCnt);
        if (err != SQLITE_OK) {
            ALOGE("sqlite3_db_config(..., %d, %d) failed: %d", lookasideSz, lookasideCnt, err);
            throw_sqlite3_exception(db, "Cannot set lookaside");
            sqlite3_close(db);
            return 0;
        }
    }

    // Check that the database is really read/write when that is what we asked for.
    if ((sqliteFlags & SQLITE_OPEN_READWRITE) && sqlite3_db_readonly(db, NULL)) {
        throw_sqlite3_exception( db, "Could not open the database in read/write mode.");
        sqlite3_close(db);
        return 0;
    }

    // Set the default busy handler to retry automatically before returning SQLITE_BUSY.
    err = sqlite3_busy_timeout(db, busyTimeout);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( db, "Could not set busy timeout");
        sqlite3_close(db);
        return 0;
    }

    // Create wrapper object.
    SQLiteConnection* connection = new SQLiteConnection(db, openFlags, path, label);

    // Enable tracing and profiling if requested.
    if (enableTrace) {
        sqlite3_trace(db, &sqliteTraceCallback, connection);
    }
    if (enableProfile) {
        sqlite3_profile(db, &sqliteProfileCallback, connection);
    }

    ALOGV("Opened connection %p with label '%s'", db, label);
    return reinterpret_cast<KLong>(connection);
}

void SQLiter_SQLiteStatement_nativeExecute(KLong connectionPtr, KLong statementPtr) {
    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    executeNonQuery(connection, statement);
}

KInt SQLiter_SQLiteStatement_nativeExecuteForChangedRowCount(KLong connectionPtr, KLong statementPtr) {
    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = executeNonQuery(connection, statement);
    return err == SQLITE_DONE ? sqlite3_changes(connection->db) : -1;
}

KLong SQLiter_SQLiteStatement_nativeExecuteForLastInsertedRowId(KLong connectionPtr, KLong statementPtr) {
    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = executeNonQuery(connection, statement);
    return err == SQLITE_DONE && sqlite3_changes(connection->db) > 0
           ? sqlite3_last_insert_rowid(connection->db) : -1;
}

void SQLiter_SQLiteStatement_nativeBindNull(KLong connectionPtr, KLong statementPtr, KInt index) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = sqlite3_bind_null(statement, index);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteStatement_nativeBindLong(KLong connectionPtr, KLong statementPtr, KInt index, KLong value) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = sqlite3_bind_int64(statement, index, value);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteStatement_nativeBindDouble(KLong connectionPtr, KLong statementPtr, KInt index, KDouble value) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    int err = sqlite3_bind_double(statement, index, value);
    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteStatement_nativeBindString(KLong connectionPtr, KLong statementPtr, KInt index, KString valueString) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    KInt valueLength = valueString->count_;

    const KChar* value = CharArrayAddressOfElementAt(valueString, 0);
    int err = sqlite3_bind_text16(statement, index, value, valueLength * sizeof(KChar),
                                  SQLITE_TRANSIENT);

    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteStatement_nativeBindBlob(KLong connectionPtr, KLong statementPtr, KInt index, KConstRef valueArray) {
    auto * connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto * statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    const ArrayHeader* array = valueArray->array();

    KInt valueLength = array->count_;
    const auto * value = ByteArrayAddressOfElementAt(array, 0);
    int err = sqlite3_bind_blob(statement, index, value, valueLength, SQLITE_TRANSIENT);

    if (err != SQLITE_OK) {
        throw_sqlite3_exception( connection->db, NULL);
    }
}

void SQLiter_SQLiteStatement_nativeFinalizeStatement(KLong connectionPtr, KLong statementPtr) {
    auto connection = reinterpret_cast<SQLiteConnection*>(connectionPtr);
    auto statement = reinterpret_cast<sqlite3_stmt*>(statementPtr);

    // We ignore the result of sqlite3_finalize because it is really telling us about
    // whether any errors occurred while executing the statement.  The statement itself
    // is always finalized regardless.
    ALOGV("Finalized statement %p on connection %p", statement, connection->db);
    sqlite3_finalize(statement);
}
}

} // namespace android
