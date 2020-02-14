package sample

import co.touchlab.stately.collections.IsoMutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class CountModel {
    private val numberCollector = NumberCollector(10)
    private val mainScope = MainScope(Dispatchers.Main)

    fun generateNumber(){
        generateNumber(numberCollector, Random.nextInt())
        printValues()
    }

    private fun printValues() {
        numberCollector.access { list ->
            println("values: ${list.joinToString(", ")}")
        }
    }

    private fun generateNumber(numberCollector:NumberCollector, newVal:Int) = mainScope.launch(Dispatchers.Default) {
        numberCollector.addNumber(newVal)
    }
}

class NumberCollector(private val maxCount: Int) : IsoMutableList<Int>() {
    fun addNumber(i: Int) = access {
        add(i)
        if (size > maxCount)
            removeAt(0)
    }
}

internal val maxCount = 10

internal fun IsoMutableList<Int>.addNumber(i: Int) = access {
    add(i)
    if (size > maxCount)
        removeAt(0)
}


