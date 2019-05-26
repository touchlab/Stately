package co.touchlab.stately

import kotlin.native.concurrent.ThreadLocal
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

class WorkerStorage {
  private val worker = Worker.start()

  fun get(key: String): SomeData? {
    return worker.execute(TransferMode.SAFE, { key }) {
      Storage.map.get(it)
    }.result
  }

  fun put(key: String, value: SomeData) {
    worker.execute(TransferMode.SAFE, { Pair(key, value).freeze() }) {
      Storage.map.set(it.first, it.second)
    }
  }
}

@ThreadLocal
private object Storage {
  val map = HashMap<String, SomeData>()
}

data class SomeData(val s: String)