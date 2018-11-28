package co.touchlab.stately.concurrency

expect class ThreadRef(){
    fun reset()
    fun same():Boolean
}