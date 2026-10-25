package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDEDependency(
    @JvmField
    val name: String,
    @JvmField
    val paths: List<Path>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDEDependency::class.java)
    }

    data class Path(
        @JvmField
        val type: DependencyPathType,
        @JvmField
        val path: String
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect(Path::class.java)
        }
    }
}