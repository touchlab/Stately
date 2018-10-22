package sample

import co.touchlab.stately.collections.*

object State{
    val cowList = createCopyOnWriteList<SampleData>()
    val sharedList = SharedLinkedList<SampleData>()
    val coiSharedList = CopyOnIterateLinkedList<SampleData>()
    val sharedMap = SharedHashMap<String, SampleData>()
    val lruCache = SharedLruCache<String, SampleData>(5)
    val workers = Array(4){createWorker()}

    fun putSample(s: String){

        var count = 0
        val futures = ArrayList<MPFuture<Unit>>()
        workers.forEach {
            val workerCount = count++
            futures.add(it.runBackground {
                val sample = SampleData("$workerCount - $s")
                cowList.add(sample)
                sharedList.add(sample)
                coiSharedList.add(sample)
                sharedMap.put("key $workerCount - $s", sample)
                lruCache.put("key $workerCount - $s", sample)
                Unit
            })
        }
    }

    fun printAll(){
        println("--- cowList ---")
        cowList.iterator().forEach { println(it) }
        println("--- sharedList ---")
        sharedList.iterator().forEach { println(it) }
        println("--- coiSharedList ---")
        coiSharedList.iterator().forEach { println(it) }
        println("--- sharedMap ---")
        sharedMap.entries.iterator().forEach { println(it) }
        println("--- lruCache ---")
        lruCache.entries.iterator().forEach { println(it) }
    }
}

data class SampleData(val s:String)
