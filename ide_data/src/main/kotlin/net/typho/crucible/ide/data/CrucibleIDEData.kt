package net.typho.crucible.ide.data

import net.typho.data_util.DataReadException
import net.typho.data_util.codec.Codec
import net.typho.data_util.codec.VersionedCodec

data class CrucibleIDEData(
    @JvmField
    val projectName: String,
    @JvmField
    val modules: List<Module>,
    @JvmField
    val scriptFiles: List<String>
) {
    companion object {
        const val VERSION = "1.6.0"
        private val REFLECTED_CODEC = Codec.reflect(CrucibleIDEData::class.java)
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

    data class Module(
        @JvmField
        val name: String,
        @JvmField
        val path: String,
        @JvmField
        val sourceSets: List<SourceSet>,
        @JvmField
        val tasks: List<Task>
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect(Module::class.java)
        }
    }

    data class SourceSet(
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
            val CODEC = Codec.reflect(SourceSet::class.java)
        }
    }

    data class Task(
        @JvmField
        val id: String,
        @JvmField
        val group: String,
        @JvmField
        val description: String
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect(Task::class.java)
        }
    }
}
