package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDETestData(
    @JvmField
    val testName: String,
    @JvmField
    val testTaskName: String,
    @JvmField
    val sourceFolders: List<Path> = listOf(),

    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
        get() = IDEDataNode.Type.TEST

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDETestData> {
            field(IDETestData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}