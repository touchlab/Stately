/*
 * Copyright (C) 2018 Touchlab, Inc.
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

@file:OptIn(FreezingIsDeprecated::class)

package co.touchlab.stately.strict

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.freeze

@OptIn(ExperimentalNativeApi::class)
actual fun <T> T.maybeFreeze(): T = if (Platform.memoryModel == MemoryModel.STRICT) {
    this.freeze()
} else {
    this
}

@OptIn(ExperimentalNativeApi::class)
actual val strictMemoryModel: Boolean
    get() = Platform.memoryModel == MemoryModel.STRICT