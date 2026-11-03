package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec

data class IDEConfigurationData(
    @JvmField
    val data: String,

    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.CONFIGURATION

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEConfigurationData> {
            field(IDEConfigurationData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}