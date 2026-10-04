package net.typho.crucible.intellij

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalProjectInfo
import com.intellij.openapi.externalSystem.model.project.ProjectData

data class CrucibleProjectInfo(
    private val externalProjectPath: String
) : ExternalProjectInfo {
    override fun getProjectSystemId() = SYSTEM_ID

    override fun getExternalProjectPath() = externalProjectPath

    override fun getExternalProjectStructure(): DataNode<ProjectData?>? {
        TODO("Not yet implemented")
    }

    override fun getLastSuccessfulImportTimestamp(): Long {
        TODO("Not yet implemented")
    }

    override fun getLastImportTimestamp(): Long {
        TODO("Not yet implemented")
    }

    override fun getBuildNumber(): String? {
        TODO("Not yet implemented")
    }

    override fun copy() = CrucibleProjectInfo(externalProjectPath)
}