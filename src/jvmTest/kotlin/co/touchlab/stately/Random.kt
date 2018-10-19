package co.touchlab.stately

import java.util.Random

actual class Random actual constructor() {
    val random = Random()
    actual fun nextInt() = random.nextInt()
}