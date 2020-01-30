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

package co.touchlab.stately

/**
 * Method to freeze state. Calls the platform implementation of 'freeze' on native, and is a noop on other platforms.
 */
expect fun <T> T.freeze(): T

/**
 * Determine if object is frozen. Will return false on non-native platforms.
 */
expect val <T> T.isFrozen: Boolean

/**
 * Call on an object which should never be frozen. Will help debug when something inadvertently is.
 */
expect fun Any.ensureNeverFrozen()