package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDEDependency(
    @JvmField
    val name: String,
    @JvmField
    val paths: List<IDEDependencyPath>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEDependency> {
            field(IDEDependency::paths, IDEDependencyPath.CODEC.listOf())
        }
    }
}