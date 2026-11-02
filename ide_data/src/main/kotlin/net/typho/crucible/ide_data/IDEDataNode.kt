package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec

sealed interface IDEDataNode {
    enum class Type(
        @JvmField
        val codec: Codec<out IDEDataNode>
    ) {
        MODULE(IDEModuleData.CODEC),
        PROJECT(IDEProjectData.CODEC),
        LIBRARY(IDELibraryData.CODEC),
        CONTENT_ROOT(IDEContentRootData.CODEC),
        MODULE_DEPENDENCY(IDEModuleDependencyData.CODEC),
        LIBRARY_DEPENDENCY(IDELibraryDependencyData.CODEC),
        TASK(IDETaskData.CODEC),
        CONFIGURATION(IDEConfigurationData.CODEC),
        TEST(IDETestData.CODEC),
        DEPENDENCIES_GRAPH(IDEDependenciesGraphData.CODEC)
    }

    val dataType: Type
    val children: List<IDEDataNode>

    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmField
        val CODEC = Codec.versioned(
            Codec.enumCodec(Type::class.java),
            IDEDataNode::dataType,
            { it.codec as Codec<IDEDataNode> },
            "type"
        )
    }
}