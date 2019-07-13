package sample

import co.touchlab.stately.concurrency.ThreadLocalRef
import co.touchlab.stately.concurrency.value
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import platform.darwin.dispatch_async_f
import platform.darwin.dispatch_get_main_queue
import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.attach
import kotlin.native.concurrent.freeze

internal actual fun <B> backgroundTask(backJob: () -> B, mainJob: (B) -> Unit) {
    val mainJobHolder = ThreadLocalRef<(B) -> Unit>()
    mainJobHolder.value = mainJob

    worker.execute(TransferMode.SAFE, { JobWrapper(backJob, mainJobHolder).freeze() }) { wrapper ->
        backToFront(wrapper.backJob, {
            wrapper.mainJobLocal.get()!!.invoke(it)
        })
    }
}

internal fun <B> backToFront(b: () -> B, job: (B) -> Unit) {
    dispatch_async_f(dispatch_get_main_queue(), DetachedObjectGraph {
        JobAndThing(job.freeze(), b())
    }.asCPointer(), staticCFunction { it: COpaquePointer? ->
        initRuntimeIfNeeded()
        val result = DetachedObjectGraph<Any>(it).attach() as JobAndThing<B>
        result.job(result.thing)
    })
}

internal val worker = Worker.start()
internal data class JobWrapper<B>(val backJob: () -> B, val mainJobLocal: ThreadLocalRef<(B) -> Unit>)
internal data class JobAndThing<B>(val job: (B) -> Unit, val thing: B)
