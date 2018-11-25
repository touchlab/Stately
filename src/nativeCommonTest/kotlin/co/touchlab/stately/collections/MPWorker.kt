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

package co.touchlab.stately.collections

import platform.Foundation.NSThread
import kotlin.native.concurrent.Future
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import kotlin.system.getTimeMillis

actual class MPWorker actual constructor(){
    val worker = Worker.start()
    actual fun <T> runBackground(backJob: () -> T): MPFuture<T> {
        return MPFuture(worker.execute(TransferMode.SAFE, {backJob.freeze()}){
            it()
        })
    }

    actual fun requestTermination() {
        worker.requestTermination().result
    }
}

actual class MPFuture<T>(private val future:Future<T>) {
    actual fun consume():T = future.result
}

actual fun sleep(time: Long) {
    NSThread.sleepForTimeInterval(time.toDouble()/1000.toDouble())
}

actual fun currentTimeMillis(): Long = getTimeMillis()