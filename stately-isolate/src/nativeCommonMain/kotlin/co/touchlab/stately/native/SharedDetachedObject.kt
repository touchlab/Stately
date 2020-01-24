package co.touchlab.stately.native

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.FreezableAtomicReference
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.attach
import kotlin.native.concurrent.freeze

class SharedDetachedObject<T:Any>(producer: () -> T) {
    private val adag :AtomicReference<DetachedObjectGraph<Any>?>
    private val lock = Lock()

    init {
        val detachedObjectGraph = DetachedObjectGraph { producer() as Any }.freeze()
        adag = AtomicReference(detachedObjectGraph.freeze())
    }

    fun <R> access(block: (T) -> R): R = lock.withLock{
        val holder = FreezableAtomicReference<Any?>(null)
        val producer = { grabAccess(holder, block) as Any }
        adag.value = DetachedObjectGraph(TransferMode.SAFE, producer).freeze()
        val retult = holder.value!!
        holder.value = null
        retult as R
    }

    private fun <R> grabAccess(holder:FreezableAtomicReference<Any?>, block: (T) -> R):T{
        val attach = adag.value!!.attach()
        val t = attach as T
        holder.value = block(t)
        return t
    }

    fun clear(){
        adag.value?.attach()
    }
}