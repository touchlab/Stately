package co.touchlab.stately.collections

import kotlin.native.internal.ExportForCppRuntime

@ExportForCppRuntime
internal fun generalHash(a: Any): Int = a.hashCode()

@ExportForCppRuntime
internal fun generalEquals(a: Any?, b: Any?): Boolean = if (a == null) {
    b == null
} else {
    a.equals(b)
}

typealias NativeMemory = ByteArray

@ExportForCppRuntime
internal fun makeByteMemory(size:Int): NativeMemory {
    val nativeMemory = NativeMemory(size)
    return nativeMemory
}

@ExportForCppRuntime
internal fun cppTrace(traceVal:Int){
    println("cpp trace $traceVal")
}

@ExportForCppRuntime
internal fun Throw_IndexOutOfBoundsException(){
    throw IndexOutOfBoundsException()
}

@ExportForCppRuntime
internal fun Throw_NoSuchElementException(){
    throw NoSuchElementException()
}

