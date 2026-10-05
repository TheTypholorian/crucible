package net.typho.crucible.intellij

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CrucibleUnlinkedProjectAware : ExternalSystemUnlinkedProjectAware {
    override val systemId = SYSTEM_ID

    override fun buildFileExtensions() = arrayOf("crucible.kts")

    override fun isBuildFile(
        project: Project,
        buildFile: VirtualFile
    ) = buildFile.name == "crucible.kts"

    override fun isLinkedProject(
        project: Project,
        externalProjectPath: String
    ) = ExternalSystemApiUtil.getSettings(project, SYSTEM_ID).linkedProjectsSettings.isNotEmpty()

    override fun subscribe(
        project: Project,
        listener: ExternalSystemProjectLinkListener,
        parentDisposable: Disposable
    ) {
        project.getService(CrucibleSystemManager.SystemSettings::class.java).subscribe(object : CrucibleSystemManager.SettingsListener {
            override fun onProjectsLinked(settings: Collection<CrucibleSystemManager.ProjectSettings>) {
                settings.forEach { listener.onProjectLinked(it.externalProjectPath) }
            }

            override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) {
                linkedProjectPaths.forEach { listener.onProjectUnlinked(it) }
            }
        }, parentDisposable)
    }

    override suspend fun unlinkProject(
        project: Project,
        externalProjectPath: String
    ) {
        // TODO ?
    }

    override suspend fun linkAndLoadProjectAsync(
        project: Project,
        externalProjectPath: String
    ) {
        ExternalSystemApiUtil.getSettings(project, SYSTEM_ID).linkProject(
            CrucibleSystemManager.ProjectSettings().also {
                it.externalProjectPath = externalProjectPath
            }
        )
        ExternalSystemUtil.refreshProject(
            externalProjectPath,
            ImportSpecBuilder(project, SYSTEM_ID)
        )
    }
}