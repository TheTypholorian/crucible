package net.typho.crucible.ide_data

enum class IDESourceType(
    @JvmField
    val isTest: Boolean,
    @JvmField
    val generated: Boolean,
    @JvmField
    val resource: Boolean,
    @JvmField
    val excluded: Boolean,
) {
    SOURCE(false, false, false, false),
    TEST(true, false, false, false),
    EXCLUDED(false, false, false, true),
    SOURCE_GENERATED(false, true, false, false),
    TEST_GENERATED(true, true, false, false),
    RESOURCE(false, false, true, false),
    TEST_RESOURCE(true, false, true, false),
    RESOURCE_GENERATED(false, true, true, false),
    TEST_RESOURCE_GENERATED(true, true, true, false)
}