package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEModuleData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String?,

    @JvmField
    val compileOutputPaths: Map<IDESourceType, Path>,
    @JvmField
    val externalCompilerOutputPaths: Map<IDESourceType, Path>,
    @JvmField
    val properties: Map<String, String>,
    @JvmField
    val id: String,
    @JvmField
    val moduleTypeId: String,
    @JvmField
    val externalConfigPath: Path,
    @JvmField
    val moduleFileDirectoryPath: Path,
    @JvmField
    val group: String?,
    @JvmField
    val version: String?,
    @JvmField
    val description: String?,
    @JvmField
    val artifacts: List<Path>,
    @JvmField
    val ideModuleGroup: List<String>?,
    @JvmField
    val productionModuleId: String?,
    @JvmField
    val moduleNameDeduplicationStrategy: IDEModuleNameDeduplicationStrategy?,
    override val children: List<IDEDataNode>
) : IDEDataNode {
    override val dataType: IDEDataNode.Type
        get() = IDEDataNode.Type.MODULE

    companion object {
        @JvmField
        val CODEC = Codec.reflect<IDEModuleData> {
            field(IDEModuleData::compileOutputPaths, Codec.dynamicMap(
                { IDESourceType.valueOf(it) },
                { it.name },
                Codec.PATH
            ).optional(mapOf()))
            field(IDEModuleData::externalCompilerOutputPaths, Codec.dynamicMap(
                { IDESourceType.valueOf(it) },
                { it.name },
                Codec.PATH
            ).optional(mapOf()))
            field(IDEModuleData::properties, Codec.dynamicMap(Codec.STRING).optional(mapOf()))
            field(IDEModuleData::artifacts, Codec.PATH.listOf().optional(listOf()))
            field(IDEModuleData::children, IDEDataNode.CODEC.listOf().optional(listOf()))
        }
    }
}