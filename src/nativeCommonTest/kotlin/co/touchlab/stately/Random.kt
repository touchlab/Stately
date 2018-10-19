package co.touchlab.stately

import kotlin.random.NativeRandom

actual class Random actual constructor() {
    val random = NativeRandom
    actual fun nextInt(): Int = random.nextInt()
}