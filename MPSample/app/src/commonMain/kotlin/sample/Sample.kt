package sample

import co.touchlab.stately.concurrency.AtomicInt

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

fun hello(): String = "Hello from ${Platform.name}"

class Proxy {
    fun proxyHello() = hello()
    companion object{
        val myCount = AtomicInt(0)
        fun heyo(){
            myCount.incrementAndGet()
            println("myCount: ${myCount.get()}")
        }
    }
}

fun main() {
    println(hello())
}