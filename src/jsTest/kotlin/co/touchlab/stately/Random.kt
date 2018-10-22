package co.touchlab.stately

import kotlin.js.Date
import kotlin.random.Random

actual class Random actual constructor() {
    val random = Random(Date().getTime().toLong())
    actual fun nextInt(): Int = random.nextInt()
}