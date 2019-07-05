package co.touchlab.stately.concurrency

import co.touchlab.stately.isMultithreaded
import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.concurrency.sleep
import kotlin.test.Test
import kotlin.test.assertEquals

class LockTest{

    /**
     * Just making sure locks are created. Not really testing the locks.
     */
    @Test
    fun lockWorks(){
        //Don't care about JS
        if(!isMultithreaded)
            return

        val lock = Lock()
        val aint = AtomicInt(0)

        val ops = ThreadOperations { }
        ops.exe {
            lock.withLock {
                sleep(3000)
                aint.value = 1
            }
        }

        ops.run(1)
        sleep(1000)
        lock.withLock {
            assertEquals(1, aint.value)
            aint.value = 2
        }

        sleep(3000)

        lock.withLock {
            assertEquals(2, aint.value)
        }

        lock.close()
    }

    @Test
    fun lockReentrant(){
        val lock = Lock()

        lock.withLock {
            lock.lock()
            sleep(1000)
            lock.unlock()
        }
    }
}