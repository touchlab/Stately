/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdarg.h>

#include <iterator>
#include <string>

#include <pthread.h>

#include "Assert.h"
#include "Alloc.h"
#include "Exceptions.h"
#include "Memory.h"
#include "Natives.h"
#include "KonanHelper.h"


#include "Runtime.h"

#include "Porting.h"
#include "Types.h"

#include "utf8.h"

extern "C" {
//TODO: Review everything that uses this and make sure we need
char *CreateCStringFromStringWithSize(KString kstring, size_t *utf8Size) {
    const KChar *utf16 = CharArrayAddressOfElementAt(kstring, 0);
    KStdString utf8;
    utf8::unchecked::utf16to8(utf16, utf16 + kstring->count_, back_inserter(utf8));
    char *result = reinterpret_cast<char *>(konan::calloc(1, utf8.size() + 1));
    ::memcpy(result, utf8.c_str(), utf8.size());
    result[utf8.size()] = 0;
    *utf8Size = utf8.size();

    return result;
}

void DisposeCStringHelper(char *cstring) {
    if (cstring) konan::free(cstring);
}

void knarchLog(const char* tag, const char* format, ...){
    printf(tag);
    printf(" - ");
    va_list args;
    va_start(args, format);
    vprintf(format, args);
    va_end(args);
    printf("\n");
}

} // extern "C"
