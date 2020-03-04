package co.touchlab.stately.collections

fun <T> sharedMutableListOf():IsoMutableList<T> = IsoMutableList()
fun <T> sharedMutableListOf(vararg items:T):IsoMutableList<T> = IsoMutableList { mutableListOf(*items)}

fun <T> sharedMutableSetOf():IsoMutableSet<T> = IsoMutableSet()
fun <T> sharedMutableSetOf(vararg items:T):IsoMutableSet<T> = IsoMutableSet { mutableSetOf(*items) }

fun <K, V> sharedMutableMapOf():IsoMutableMap<K, V> = IsoMutableMap()
fun <K, V> sharedMutableMapOf(vararg items:Pair<K, V>):IsoMutableMap<K, V> = IsoMutableMap { mutableMapOf(*items) }