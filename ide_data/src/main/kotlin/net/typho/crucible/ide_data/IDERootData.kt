package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import net.typho.data_util.error.DataReadException
import java.nio.file.Path

data class IDERootData(
    @JvmField
    val project: IDEProjectData,
    @JvmField
    val scriptFiles: List<Path>
) {
    companion object {
        const val VERSION = "2.0.0"
        private val REFLECTED_CODEC = Codec.reflect<IDERootData> {
            field(IDERootData::project, IDEProjectData.CODEC)
        }
        @JvmField
        val CODEC = Codec.versioned(
            versionGetter = { VERSION },
            dataCodec = { version ->
                if (version == VERSION) {
                    REFLECTED_CODEC
                } else {
                    throw DataReadException("Unsupported IDE data version '$version', expected '$VERSION'")
                }
            }
        )
    }
}
