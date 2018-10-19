package co.touchlab.stately.collections

expect class MPWorker(){
    fun <T> runBackground(backJob:()->T):MPFuture<T>
    fun requestTermination()
}

expect class MPFuture<T>{
    fun consume():T
}

fun createWorker():MPWorker = MPWorker()

expect fun sleep(time:Long)

class ThreadOps<T>(){
    private val exes = mutableListOf<(MutableList<T>)->Unit>()
    private val tests = mutableListOf<(MutableList<T>)->Unit>()
    var lastRunTime = 0L

    fun exe(proc:(MutableList<T>)->Unit){
        exes.add(proc)
    }

    fun test(proc:(MutableList<T>)->Unit){
        tests.add(proc)
    }

    fun run(threads:Int, list:MutableList<T> = createCopyOnWriteList<T>().mpfreeze()):MutableList<T>{
        exes.mpfreeze()

        val start = currentTimeMillis()

        val workers= Array(threads){MPWorker()}
        for(i in 0 until exes.size){
            val ex = exes[i](list)
            workers[i % workers.size]
                .runBackground { ex }
        }
        workers.forEach { it.requestTermination() }

        tests.forEach { it(list) }


        lastRunTime = currentTimeMillis() - start

        return list
    }
}