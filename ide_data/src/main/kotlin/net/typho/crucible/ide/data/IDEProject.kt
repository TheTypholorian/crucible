package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec
import net.typho.data_util.error.DataReadException

data class IDEProject(
    @JvmField
    val projectName: String,
    @JvmField
    val modules: List<IDEModule>,
    @JvmField
    val tasks: List<IDETask>,
    @JvmField
    val scriptFiles: List<String>
) {
    companion object {
        const val VERSION = "1.9.1"
        private val REFLECTED_CODEC = Codec.reflect<IDEProject> {
            field(IDEProject::modules, IDEModule.CODEC.listOf())
            field(IDEProject::tasks, IDETask.CODEC.listOf())
            field(IDEProject::scriptFiles, Codec.STRING.listOf())
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
