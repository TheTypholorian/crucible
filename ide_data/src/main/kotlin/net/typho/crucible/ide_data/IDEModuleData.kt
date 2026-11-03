package net.typho.crucible.ide_data

import net.typho.data_util.codec.Codec
import java.nio.file.Path

data class IDEModuleData(
    @JvmField
    val externalName: String,
    @JvmField
    val internalName: String? = null,

    @JvmField
    val compileOutputPaths: Map<IDESourceType, Path> = mapOf(),
    @JvmField
    val externalCompilerOutputPaths: Map<IDESourceType, Path> = mapOf(),
    @JvmField
    val properties: Map<String, String> = mapOf(),
    @JvmField
    val id: String,
    @JvmField
    val moduleTypeId: String,
    @JvmField
    val externalConfigPath: Path,
    @JvmField
    val moduleFileDirectoryPath: Path,
    @JvmField
    val group: String? = null,
    @JvmField
    val version: String? = null,
    @JvmField
    val description: String? = null,
    @JvmField
    val artifacts: List<Path> = listOf(),
    @JvmField
    val ideModuleGroup: List<String>? = null,
    @JvmField
    val productionModuleId: String? = null,
    @JvmField
    val moduleNameDeduplicationStrategy: IDEModuleNameDeduplicationStrategy? = null,
    override val children: List<IDEDataNode> = listOf()
) : IDEDataNode {
    override val nodeType: IDEDataNode.Type
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