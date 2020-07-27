package co.touchlab.stately.collections

@SymbolName("Kotlin_Any_share")
private external fun Any.share()

fun <T> sharedList(): MutableList<T> {
    val l = mutableListOf<T>()
    l.share()
    return l
}
