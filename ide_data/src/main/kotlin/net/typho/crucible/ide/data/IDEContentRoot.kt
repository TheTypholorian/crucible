package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDEContentRoot(
    @JvmField
    val type: SourceSetType,
    @JvmField
    val generated: Boolean,
    @JvmField
    val path: String
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDEContentRoot::class.java)
    }
}