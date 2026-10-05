package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class CrucibleIDEData(
    @JvmField
    val projectName: String,
    @JvmField
    val sourceSets: List<SourceSet>,
    @JvmField
    val dependencies: List<Dependency>,
    @JvmField
    val tasks: List<Task>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(CrucibleIDEData::class.java)
    }

    data class SourceSet(
        @JvmField
        val type: SourceSetType,
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
