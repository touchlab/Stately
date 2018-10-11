package co.touchlab.stately

import platform.Foundation.NSLock
import platform.posix.pthread_self
import kotlin.native.concurrent.*

fun printThreadInfo(prefix:String){
    print("$prefix: ${pthread_self()}")
}

class SharedMap<K, V> {
    private val stateBox:AtomicReference<DetachedObjectGraph<Any>>
            = AtomicReference(DetachedObjectGraph(mode = TransferMode.SAFE, producer = {HashMap<K, V>() as Any}))
    private val lock = NSLock()

    fun put(k:K, v:V){
        if(!k.isFrozen || !v.isFrozen)
            throw IllegalStateException("Map key and value must be frozen")

        lock.lock()
        try {
            stateBox.value = DetachedObjectGraph(mode = TransferMode.SAFE, producer = {
                val dataMap = stateBox.value.attach() as MutableMap<K, V>
                dataMap.put(k, v)
                dataMap as Any
            })
        } finally {
            lock.unlock()
        }
    }

    fun get(k:K):V?{
        var res:V? = null
        lock.lock()
        try {
            stateBox.value = DetachedObjectGraph(mode = TransferMode.SAFE, producer = {
                val dataMap = stateBox.value.attach() as MutableMap<K, V>
                res = dataMap.get(k)
                dataMap as Any
            })
        } finally {
            lock.unlock()
        }

        return res
    }

    fun close(){
        lock.lock()
        try {
            //Once attached, memory should be cleaned up
            stateBox.value.attach()
        } finally {
            lock.unlock()
        }
    }
}

class SharedState<T>(tMaker:() -> T){

    private val stateBox:AtomicReference<DetachedObjectGraph<Any>> = AtomicReference(DetachedObjectGraph(mode = TransferMode.SAFE, producer = {tMaker() as Any}))
    private val lock = NSLock()

    /*public fun access(proc:(T) -> T){
        lock.lock()
        try {
            val t = stateBox.value.attach() as T
            val holder = proc(t)
            stateBox.value = DetachedObjectGraph(mode = TransferMode.UNSAFE, producer = {
                holder as Any
            })
        } finally {
            lock.unlock()
        }
    }*/

    public fun access2(proc:(T) -> T){
        lock.lock()
        try {
            stateBox.value = DetachedObjectGraph(mode = TransferMode.SAFE, producer = {
                proc(stateBox.value.attach() as T) as Any
            })
        } finally {
            lock.unlock()
        }
    }



    fun get():T = stateBox.value.attach() as T

    private inline fun <T> withLock(proc:() -> T):T{
        lock.lock()
        try {
            return proc.invoke()
        } finally {
            lock.unlock()
        }
    }
}