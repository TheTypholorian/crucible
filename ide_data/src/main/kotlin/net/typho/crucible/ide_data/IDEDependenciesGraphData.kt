package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEDependenciesGraphData(
    @JvmField
    val componentsDependencies: List<ComponentDependencies>,

    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.DEPENDENCIES_GRAPH

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEDependenciesGraphData> {
            field(IDEDependenciesGraphData::componentsDependencies, ComponentDependencies.CODEC.listOf())
            field(IDEDependenciesGraphData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }

    data class DependencyScope(
        @JvmField
        val scope: String,
        @JvmField
        val displayName: String,
        @JvmField
        val description: String?
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyScope>()
        }
    }

    data class ComponentDependencies(
        @JvmField
        val componentName: String,
        @JvmField
        val compileDependenciesGraph: DependencyScope,
        @JvmField
        val runtimeDependenciesGraph: DependencyScope
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<ComponentDependencies> {
                field(ComponentDependencies::compileDependenciesGraph, DependencyScope.CODEC)
                field(ComponentDependencies::runtimeDependenciesGraph, DependencyScope.CODEC)
            }
        }
    }
}