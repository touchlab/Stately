package sample

import co.touchlab.stately.collections.frozenCopyOnWriteList
import co.touchlab.stately.collections.frozenHashMap
import co.touchlab.stately.collections.frozenHashSet
import co.touchlab.stately.collections.frozenLinkedList
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.freeze

fun hello(): String = "Hello, Kotlin/Native!"

fun main() {


    linkedListMemory(true)
    linkedListMemory(false)
    setMemory()
    mapMemory()
    cowListMemory()
    cowListMemoryClear()
}

fun cowListMemory(){
    val l = frozenCopyOnWriteList<HiHi>()
    for (i in 0 until 50){
        l.add(HiHi("hi $i"))
    }
}

fun cowListMemoryClear(){
    val l = frozenCopyOnWriteList<HiHi>()
    for (i in 0 until 50){
        l.add(HiHi("hi $i"))
    }
    l.clear()
}

fun setMemory(){
    val set = frozenHashSet<HiHi>()

    for(i in 0 until 5){
        set.add(HiHi("arst $i"))
    }

    set.clear()
}

fun mapMemory(){
    val map = frozenHashMap<String, HiHi>()

    for(i in 0 until 50){
        map.put("key $i", HiHi("arst"))
    }

    map.clear()
}

private fun linkedListMemory(stableIterator:Boolean) {
    val ll = frozenLinkedList<AtomicReference<HiHi>>(stableIterator)
    for (i in 0 until 20) {
        val hi = HiHi("arst")
        val ar = AtomicReference(hi.freeze())

        ll.add(ar)
    }

    ll.clear()
}

data class HiHi(val s:String)