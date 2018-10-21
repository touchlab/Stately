package co.touchlab.stately.annotation

import co.touchlab.stately.collections.isNative
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.isFrozen
import co.touchlab.stately.isNativeFrozen
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnnotationsTest{
    @Test
    fun testSharedImmutable(){
        assertTrue(topLevel.isNativeFrozen())
    }

    @Test
    fun testThreadLocal(){
        if(isNative){
            assertFalse(WillBeLocal.isFrozen())
            assertTrue(WontBeLocal.isFrozen())
        }
    }
}

@ThreadLocal
object WillBeLocal{
    val s = "local"
}

object WontBeLocal{
    val s = "not local"
}

@SharedImmutable
val topLevel = TopLevel(AtomicReference("hello"))

data class TopLevel(val atom:AtomicReference<String>)