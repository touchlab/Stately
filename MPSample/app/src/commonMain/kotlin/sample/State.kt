package sample

import co.touchlab.stately.collections.*
import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.concurrency.withLock

internal expect fun <B> backgroundTask(backJob: () -> B, mainJob: (B) -> Unit)

interface ViewUpdater {
    fun dataUpdate(t:String)
}

internal object EmptyView:ViewUpdater {
    override fun dataUpdate(t: String) {
        throw UnsupportedOperationException("Should never call this")
    }
}

var viewUpdater:ViewUpdater = EmptyView
var count = 0

object Collections{

    val cowList = frozenCopyOnWriteList<SampleData>()
    val sharedList = frozenLinkedList<SampleData>()
    val sharedMap = frozenHashMap<String, SampleData>()

    val lruRemovedCount = AtomicInt(0)
    val lruCache = frozenLruCache<String, SampleData>(5) {
        lruRemovedCount.incrementAndGet()
    }

    val lock = Lock()

    fun putSample(s: String){
        count++

        repeat(4) { workerCount ->
            backgroundTask({
                val sample = SampleData("$workerCount - $s")
                cowList.add(sample)
                sharedList.add(sample)
                sharedMap.put("key $workerCount - $s", sample)

                lock.withLock {
                    //This doesn't need to be locked, but that's how locks work
                    lruCache.put("key $workerCount - $s", sample)
                }
            }){
                printAll()
                viewUpdater.dataUpdate("Total runs: ${count}, list size: ${sharedList.size}")
            }
        }
    }

    fun printAll(){
        println("--- cowList ---")
        cowList.iterator().forEach { println(it) }
        println("--- sharedList ---")
        sharedList.iterator().forEach { println(it) }
        println("--- sharedMap ---")
        sharedMap.entries.iterator().forEach { println(it) }
        println("--- lruCache ---")
        println("Removed count: ${lruRemovedCount.value}")
        lruCache.entries.iterator().forEach { println(it) }
    }

    /**
     * If collections or locks aren't going to live "forever", you need to close them.
     * Lock doesn't *need* to be closed on iOS/JVM, but it does on other native platforms.
     */
    fun cleanUp(){
        cowList.clear()
        sharedList.clear()
        sharedMap.clear()
        lock.close()
    }
}

data class SampleData(val s:String)
