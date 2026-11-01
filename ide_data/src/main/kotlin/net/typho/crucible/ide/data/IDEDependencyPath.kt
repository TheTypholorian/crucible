package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEDependencyPath(
    @JvmField
    val type: DependencyPathType,
    @JvmField
    val path: Path
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDEDependencyPath::class.java) {
            field(IDEDependencyPath::path, Codec.PATH)
        }
    }
}