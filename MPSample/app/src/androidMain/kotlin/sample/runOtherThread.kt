package sample

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

internal actual fun <B> backgroundTask(backJob: () -> B, mainJob: (B) -> Unit) {
    executor.execute {
        val aref = AtomicReference<B>()
        try {
            aref.set(backJob())
            val h = btfHandler
            h.post {
                mainJob(aref.get())
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

internal val btfHandler: Handler = Handler(Looper.getMainLooper())
internal val executor = Executors.newSingleThreadExecutor()
