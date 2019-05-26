/*
 * Copyright (C) 2007 The Android Open Source Project
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

#ifndef _ANDROID_DATABASE_SQLITE_COMMON_H
#define _ANDROID_DATABASE_SQLITE_COMMON_H

#include <sqlite3.h>
#include <stddef.h>
#include "Types.h"
#include "Porting.h"
#include "KString.h"
#include "KonanHelper.h"

// Special log tags defined in SQLiteDebug.java.
#define SQLITE_LOG_TAG "SQLiteLog"
#define SQLITE_TRACE_TAG "SQLiteStatements"
#define SQLITE_PROFILE_TAG "SQLiteTime"
#define ALOGE(...) knarchLog("ERROR", __VA_ARGS__)
#define ALOGW(...) knarchLog("WARN", __VA_ARGS__)
#define ALOGV(...) //knarchLog("VERBOSE", __VA_ARGS__)
#define ALOG(tag, ...) knarchLog(tag, __VA_ARGS__)

extern "C" {
RUNTIME_NORETURN void ThrowSql_IllegalStateException(KString str);
RUNTIME_NORETURN void ThrowSql_SQLiteException(KString exceptionClass, KString str);
};
namespace android {

/* throw a SQLiteException with a message appropriate for the error in handle */
    void throw_sqlite3_exception(sqlite3 *handle);

/* throw a SQLiteException with the given message */
    void throw_sqlite3_exception(const char *message);

/* throw a SQLiteException with a message appropriate for the error in handle
   concatenated with the given message
 */
    void throw_sqlite3_exception(sqlite3 *handle, const char *message);

/* throw a SQLiteException for a given error code */
    void throw_sqlite3_exception_errcode(int errcode, const char *message);

    void throw_sqlite3_exception(int errcode,
                                 const char *sqlite3Message, const char *message);

}

#endif // _ANDROID_DATABASE_SQLITE_COMMON_H
