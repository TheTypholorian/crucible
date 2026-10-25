package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDEModule(
    @JvmField
    val name: String,
    @JvmField
    val path: String,
    @JvmField
    val sourceSets: List<IDESourceSet>,
    @JvmField
    val tasks: List<IDETask>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDEModule::class.java)
    }
}