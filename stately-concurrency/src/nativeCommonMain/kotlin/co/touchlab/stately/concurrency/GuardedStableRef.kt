package co.touchlab.stately.concurrency

import kotlinx.cinterop.StableRef

class GuardedStableRef<T:Any>(t:T) {
    private val stableRef : StableRef<T> = StableRef.create(t)
    private val threadRef = ThreadRef()
    internal val disposed = AtomicBoolean(false)
    val state:T
        get() {
            checkStateAccessValid()
            return stableRef.get()
        }

    fun dispose(){
        checkStateAccessValid()
        stableRef.dispose()
        disposed.value = true
    }

    private fun checkStateAccessValid() {
        if (!threadRef.same())
            throw IllegalStateException("StableRef can only be accessed from the thread it was created with")

        if (disposed.value)
            throw IllegalStateException("StableRef already disposed")
    }
}