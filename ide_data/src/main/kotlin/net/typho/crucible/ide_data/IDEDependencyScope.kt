package net.typho.crucible.ide_data

enum class IDEDependencyScope(
    @JvmField
    val forProdCompile: Boolean,
    @JvmField
    val forProdRuntime: Boolean,
    @JvmField
    val forTestCompile: Boolean,
    @JvmField
    val forTestRuntime: Boolean
) {
    COMPILE(true, true, true, true),
    TEST(false, false, true, true),
    RUNTIME(false, true, false, true),
    PROVIDED(true, false, true, true)
}