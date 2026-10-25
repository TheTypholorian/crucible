package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDESourceSet(
    @JvmField
    val type: SourceSetType,
    @JvmField
    val dependencies: List<IDEDependency>,
    @JvmField
    val generated: Boolean,
    @JvmField
    val path: String
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDESourceSet::class.java)
    }
}