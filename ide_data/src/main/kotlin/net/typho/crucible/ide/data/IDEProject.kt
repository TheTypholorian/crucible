package net.typho.crucible.ide.data

import net.typho.data_util.DataReadException
import net.typho.data_util.codec.Codec
import net.typho.data_util.codec.VersionedCodec

data class IDEProject(
    @JvmField
    val projectName: String,
    @JvmField
    val modules: List<IDEModule>,
    @JvmField
    val scriptFiles: List<String>
) {
    companion object {
        const val VERSION = "1.6.1"
        private val REFLECTED_CODEC = Codec.reflect(IDEProject::class.java)
        @JvmField
        val CODEC = VersionedCodec.of(
            codec = Codec.STRING,
            versionGetter = { VERSION },
            codecFunction = { version ->
                if (version == VERSION) {
                    REFLECTED_CODEC
                } else {
                    throw DataReadException("Unsupported IDE data version '$version', expected '$VERSION'")
                }
            },
            fallback = VERSION
        )
    }

}
