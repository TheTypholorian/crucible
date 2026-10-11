package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
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

        project.basePath?.let {
            val file = Path(it).resolve(".crucible").resolve("scripts.bin")

            if (file.exists()) {
                ObjectInputStream(file.inputStream()).use {
                    (it.readObject() as List<ScriptCompilationConfiguration>).mapTo(ret) {
                        ScriptDefinition(it, ScriptEvaluationConfiguration())
                    }
                }
            }
        }

        return ret
    }
}