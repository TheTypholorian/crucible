package net.typho.crucible.ide.data

import net.typho.data_util.codec.Codec

data class IDETask(
    @JvmField
    val id: String,
    @JvmField
    val group: String,
    @JvmField
    val description: String
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(IDETask::class.java)
    }
}