package net.typho.crucible.intellij

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Paths
import kotlin.io.path.name

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
    ) = Paths.get(externalProjectPath).name == "crucible.kts"

    override fun subscribe(
        project: Project,
        listener: ExternalSystemProjectLinkListener,
        parentDisposable: Disposable
    ) {
    }

    override suspend fun unlinkProject(
        project: Project,
        externalProjectPath: String
    ) {
    }

    override suspend fun linkAndLoadProjectAsync(
        project: Project,
        externalProjectPath: String
    ) {
        ExternalSystemUtil.refreshProject(
            externalProjectPath,
            ImportSpecBuilder(project, SYSTEM_ID)
        )
    }
}