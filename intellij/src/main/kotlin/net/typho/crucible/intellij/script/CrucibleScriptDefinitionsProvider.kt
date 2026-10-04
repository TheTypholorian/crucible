package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionProvider
import java.io.File
import java.net.URLClassLoader
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

class CrucibleScriptDefinitionsProvider(
    private val project: Project
) : ScriptDefinitionProvider {
    init {
        throw NullPointerException("create provider $project")
    }

    override val currentDefinitions: Sequence<ScriptDefinition> by lazy {
        println("get definitions")
        // TODO
        val scriptsJar = File("C:\\Users\\evan\\IdeaProjects\\crucible\\scripts\\build\\libs\\scripts.jar")
        val crucibleJar = File("C:\\Users\\evan\\IdeaProjects\\crucible\\build\\libs\\crucible-1.0.0.jar")

        val loader = URLClassLoader(
            arrayOf(
                scriptsJar.toURI().toURL(),
                crucibleJar.toURI().toURL()
            ),
            javaClass.classLoader
        )

        sequenceOf(ScriptDefinition.FromTemplate(
            ScriptingHostConfiguration {
                jvm {
                    baseClassLoader(loader)
                }
            },
            Class.forName(
                "net.typho.crucible.script.AbstractProjectConfig",
                true,
                loader
            ).kotlin
        ))
    }

    override fun findDefinition(script: SourceCode) = currentDefinitions.firstOrNull { it.isScript(script) }

    override fun getDefaultDefinition() = currentDefinitions.single()

    override fun getKnownFilenameExtensions() = currentDefinitions.map { it.fileExtension }

    override fun isScript(script: SourceCode) = findDefinition(script) != null
}