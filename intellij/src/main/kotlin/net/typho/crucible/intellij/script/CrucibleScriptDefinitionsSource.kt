package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import java.io.File
import java.net.URLClassLoader
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.JvmGetScriptingClass
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

@Suppress("DEPRECATION")
class CrucibleScriptDefinitionsSource(
    private val project: Project
) : ScriptDefinitionsSource {
    override val definitions: Sequence<ScriptDefinition> by lazy {
        println("get definitions ${project.name}")
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
                    getScriptingClass(JvmGetScriptingClass())
                }
            },
            Class.forName(
                "net.typho.crucible.script.ProjectConfigScriptDefinition",
                true,
                loader
            ).kotlin
        ))
    }
}