package net.typho.crucible.intellij.script

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.util.io.delete
import kotlinx.io.IOException
import net.typho.crucible.intellij.SYSTEM_ID
import java.io.File
import java.io.ObjectInputStream
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptDefinition
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.intellij.ScriptDefinitionsProvider

@Suppress("UNCHECKED_CAST")
class CrucibleScriptDefinitionsProvider(
    private val project: Project
) : ScriptDefinitionsProvider {
    override val id: String = "crucible"

    override fun getDefinitionClasses() = listOf<String>()

    override fun getDefinitionsClassPath() = listOf<File>()

    override fun useDiscovery() = false

    override fun provideDefinitions(
        baseHostConfiguration: ScriptingHostConfiguration,
        loadedScriptDefinitions: List<ScriptDefinition>
    ): Iterable<ScriptDefinition> {
        val ret = loadedScriptDefinitions.toMutableList()

        project.basePath?.let { projectPath ->
            val file = Path(projectPath).resolve(".crucible").resolve("scripts.bin")

            if (file.exists()) {
                try {
                    ObjectInputStream(file.inputStream()).use {
                        (it.readObject() as List<ScriptCompilationConfiguration>).mapTo(ret) {
                            ScriptDefinition(it, ScriptEvaluationConfiguration())
                        }
                    }
                } catch (e: IOException) {
                    file.delete()
                    ApplicationManager.getApplication().invokeLater({
                        if (!project.isDisposed) {
                            ExternalSystemUtil.refreshProject(
                                projectPath,
                                ImportSpecBuilder(project, SYSTEM_ID)
                                    .activateBuildToolWindowOnStart()
                                    .navigateToError()
                            )
                        }
                    }, ModalityState.nonModal())
                }
            }
        }

        return ret
    }
}