package co.touchlab.stately.collections

import co.touchlab.stately.freeze

expect class MPWorker(){
    fun <T> runBackground(backJob:()->T):MPFuture<T>
    fun requestTermination()
}

expect class MPFuture<T>{
    fun consume():T
}

fun createWorker():MPWorker = MPWorker()

expect fun sleep(time:Long)

class ThreadOps<C>(val producer:()->C){
    private val exes = mutableListOf<(C)->Unit>()
    private val tests = mutableListOf<(C)->Unit>()
    var lastRunTime = 0L

    fun exe(proc:(C)->Unit){
        exes.add(proc)
    }

    fun test(proc:(C)->Unit){
        tests.add(proc)
    }
    
    fun run(threads:Int, collection:C = producer(), randomize:Boolean = false):C{
        
        if(randomize){
            exes.shuffle()
            tests.shuffle()
        }

        exes.freeze()

        val start = currentTimeMillis()

        val workers= Array(threads){MPWorker()}
        for(i in 0 until exes.size){
            val ex = exes[i](collection)
            workers[i % workers.size]
                .runBackground { ex }
        }
        workers.forEach { it.requestTermination() }

        tests.forEach { it(collection) }

        lastRunTime = currentTimeMillis() - start

        return collection
    }
}