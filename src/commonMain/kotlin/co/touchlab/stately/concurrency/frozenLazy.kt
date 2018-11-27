package co.touchlab.stately.concurrency

import co.touchlab.stately.freeze
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class frozenLazy<T, R>(private val producer: () -> T) : ReadWriteProperty<R, T> {

    override fun getValue(thisRef: R, property: KProperty<*>): T =
        lock.withLock {
            var value = valAtomic.value
            if (value == null) {
                value = producer()
                valAtomic.value = value.freeze()
            }
            value!!
        }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) = lock.withLock {
        valAtomic.value = value.freeze()
    }

    private val valAtomic = AtomicReference<T?>(null)
    private val lock = SingleLock()
}