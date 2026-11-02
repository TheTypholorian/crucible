package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEConfigurationData(
    @JvmField
    val data: String,

    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.CONFIGURATION

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEConfigurationData> {
            field(IDEConfigurationData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}