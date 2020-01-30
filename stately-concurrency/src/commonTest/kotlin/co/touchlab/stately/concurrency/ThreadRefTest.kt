package co.touchlab.stately.concurrency

import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.isMultithreaded
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThreadRefTest {
  @Test
  fun threadRefTest() {
    //Don't care about JS
    if(!isMultithreaded)
      return

    val ref = ThreadRef()
    assertTrue(ref.same())
    val ops = ThreadOperations { }
    ops.exe {
      assertFalse(ref.same())
    }

    ops.run(1)
  }
}