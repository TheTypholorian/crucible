@file:Suppress("DEPRECATION", "UnstableApiUsage")
package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.model.ConfigurationDataImpl
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.TaskData
import com.intellij.openapi.roots.DependencyScope
import net.typho.crucible.ide_data.*

fun IDESourceType.toIntellij() = when (this) {
    IDESourceType.SOURCE -> ExternalSystemSourceType.SOURCE
    IDESourceType.TEST -> ExternalSystemSourceType.TEST
    IDESourceType.EXCLUDED -> ExternalSystemSourceType.EXCLUDED
    IDESourceType.SOURCE_GENERATED -> ExternalSystemSourceType.SOURCE_GENERATED
    IDESourceType.TEST_GENERATED -> ExternalSystemSourceType.TEST_GENERATED
    IDESourceType.RESOURCE -> ExternalSystemSourceType.RESOURCE
    IDESourceType.TEST_RESOURCE -> ExternalSystemSourceType.TEST_RESOURCE
    IDESourceType.RESOURCE_GENERATED -> ExternalSystemSourceType.RESOURCE_GENERATED
    IDESourceType.TEST_RESOURCE_GENERATED -> ExternalSystemSourceType.TEST_RESOURCE_GENERATED
}

fun IDEModuleNameDeduplicationStrategy.toIntellij() = when (this) {
    IDEModuleNameDeduplicationStrategy.PARENT_PATH_NAME -> ModuleNameDeduplicationStrategy.PARENT_PATH_NAME
    IDEModuleNameDeduplicationStrategy.NUMBER_SUFFIX -> ModuleNameDeduplicationStrategy.NUMBER_SUFFIX
    IDEModuleNameDeduplicationStrategy.DEFAULT -> ModuleNameDeduplicationStrategy.DEFAULT
}

fun IDELibraryPathType.toIntellij() = when (this) {
    IDELibraryPathType.BINARY -> LibraryPathType.BINARY
    IDELibraryPathType.SOURCE -> LibraryPathType.SOURCE
    IDELibraryPathType.DOC -> LibraryPathType.DOC
    IDELibraryPathType.ANNOTATION -> LibraryPathType.ANNOTATION
    IDELibraryPathType.EXCLUDED -> LibraryPathType.EXCLUDED
}

fun IDEDependencyScope.toIntellij() = when (this) {
    IDEDependencyScope.COMPILE -> DependencyScope.COMPILE
    IDEDependencyScope.TEST -> DependencyScope.TEST
    IDEDependencyScope.RUNTIME -> DependencyScope.RUNTIME
    IDEDependencyScope.PROVIDED -> DependencyScope.PROVIDED
}

fun IDELibraryLevel.toIntellij() = when (this) {
    IDELibraryLevel.PROJECT -> LibraryLevel.PROJECT
    IDELibraryLevel.MODULE -> LibraryLevel.MODULE
}

fun IDEModuleData.toIntellij() = ModuleData(
    id,
    SYSTEM_ID,
    moduleTypeId,
    externalName,
    moduleFileDirectoryPath.toString(),
    externalConfigPath.toString()
).also { node ->
    internalName?.let { node.internalName = it }
    compileOutputPaths.forEach { (type, path) -> node.setCompileOutputPath(type.toIntellij(), path.toString()) }
    properties.forEach { (key, value) -> node.setProperty(key, value) }
    group?.let { node.group = it }
    version?.let { node.version = it }
    description?.let { node.description = it }
    node.artifacts.addAll(artifacts.map { it.toFile() })
    ideModuleGroup?.let { node.ideModuleGroup = it.toTypedArray() }
    productionModuleId?.let { node.productionModuleId = it }
    moduleNameDeduplicationStrategy?.let { node.moduleNameDeduplicationStrategy = it.toIntellij() }
}

fun IDELibraryData.toIntellij() = LibraryData(
    SYSTEM_ID,
    externalName,
    unresolved ?: false
).also { node ->
    internalName?.let { node.internalName = it }
    paths.forEach { (type, paths) -> paths.forEach { node.addPath(type.toIntellij(), it.toString()) } }
    groupId?.let { node.setGroup(it) }
    artifactId?.let { node.artifactId = it }
    version?.let { node.version = it }
}

fun IDEDataNode.toIntellij(): DataNode<*> {
    val node = when (this) {
        is IDEModuleData -> DataNode(
            ProjectKeys.MODULE,
            toIntellij(),
            null
        )
        is IDEProjectData -> DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                SYSTEM_ID,
                externalName,
                ideProjectFileDirectoryPath.toString(),
                linkedExternalProjectPath.toString()
            ).also { node ->
                internalName?.let { node.internalName = it }
                ideGrouping?.let { node.ideGrouping = it }
                group?.let { node.group = it }
                version?.let { node.version = it }
                description?.let { node.description = it }
            },
            null
        )
        is IDELibraryData -> DataNode(
            ProjectKeys.LIBRARY,
            toIntellij(),
            null
        )
        is IDEContentRootData -> DataNode(
            ProjectKeys.CONTENT_ROOT,
            ContentRootData(
                SYSTEM_ID,
                rootPath.toString()
            ).also { node ->
                data.forEach { (type, roots) -> roots.forEach { node.storePath(type.toIntellij(), it.path.toString(), it.packagePrefix) } }
            },
            null
        )
        is IDEModuleDependencyData -> DataNode(
            ProjectKeys.MODULE_DEPENDENCY,
            ModuleDependencyData(
                ownerModule.toIntellij(),
                target.toIntellij()
            ).also { node ->
                scope?.let { node.scope = it.toIntellij() }
                exported?.let { node.isExported = it }
                order?.let { node.order = it }
                productionOnTestDependency?.let { node.isProductionOnTestDependency = it }
                node.moduleDependencyArtifacts = moduleDependencyArtifacts.map { it.toString() }
            },
            null
        )
        is IDELibraryDependencyData -> DataNode(
            ProjectKeys.LIBRARY_DEPENDENCY,
            LibraryDependencyData(
                ownerModule.toIntellij(),
                target.toIntellij(),
                level.toIntellij()
            ).also { node ->
                scope?.let { node.scope = it.toIntellij() }
                exported?.let { node.isExported = it }
                order?.let { node.order = it }
            },
            null
        )
        is IDETaskData -> DataNode(
            ProjectKeys.TASK,
            TaskData(
                SYSTEM_ID,
                name,
                linkedExternalProjectPath.toString(),
                description
            ).also { node ->
                group?.let { node.group = it }
                type?.let { node.type = it }
                inherited?.let { node.isInherited = it }
                isJvm?.let { node.isJvm = it }
                isTest?.let { node.isTest = it }
                isJvmTest?.let { node.isJvmTest = it }
            },
            null
        )
        is IDEConfigurationData -> DataNode(
            ProjectKeys.CONFIGURATION,
            ConfigurationDataImpl(
                SYSTEM_ID,
                data
            ),
            null
        )
        is IDETestData -> DataNode(
            ProjectKeys.TEST,
            TestData(
                SYSTEM_ID,
                testName,
                testTaskName,
                sourceFolders.map { it.toString() }.toSet()
            ),
            null
        )
    }
    children.forEach { node.addChild(it.toIntellij()) }
    return node
}