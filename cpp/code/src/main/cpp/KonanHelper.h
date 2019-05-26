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

#ifndef TLRUNTIME_KONANHELPER_H
#define TLRUNTIME_KONANHELPER_H

#include "Common.h"
#include "Memory.h"
#include "Types.h"
#include "TypeInfo.h"

#ifdef __cplusplus
extern "C" {
#endif

char* CreateCStringFromStringWithSize(KString kstring, size_t* utf8Size);
void DisposeCStringHelper(char* cstring);

void knarchLog(const char* tag, const char* format, ...);



#ifdef __cplusplus
}
#endif


#endif // TLRUNTIME_KONANHELPER_H
