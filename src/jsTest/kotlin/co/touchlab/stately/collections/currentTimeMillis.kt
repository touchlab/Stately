package co.touchlab.stately.collections

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date().getTime().toLong()