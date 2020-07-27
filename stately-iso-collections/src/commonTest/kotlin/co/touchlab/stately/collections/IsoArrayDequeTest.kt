package co.touchlab.stately.collections

class IsoArrayDequeTest : IsoMutableListTest() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override fun defaultCollection(): IsoMutableCollection<SomeData> = IsoArrayDeque<SomeData>()
}
