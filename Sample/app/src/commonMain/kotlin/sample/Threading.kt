package sample

expect class MPWorker(){
    fun <T> runBackground(backJob:()->T):MPFuture<T>
    fun requestTermination()
}

expect class MPFuture<T>{
    fun consume():T
}

fun createWorker():MPWorker = MPWorker()