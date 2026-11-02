package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEContentRoot(
    @JvmField
    val type: SourceSetType,
    @JvmField
    val generated: Boolean,
    @JvmField
    val path: Path
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEContentRoot>()
    }
}