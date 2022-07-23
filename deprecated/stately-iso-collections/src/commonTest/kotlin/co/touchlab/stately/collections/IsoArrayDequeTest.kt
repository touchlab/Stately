package co.touchlab.stately.collections

class IsoArrayDequeTest : IsoMutableListTest() {
    override fun defaultCollection(): IsoMutableCollection<SomeData> = IsoArrayDeque<SomeData>()
}
