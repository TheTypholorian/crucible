@file:Suppress("DEPRECATION")
package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

class CrucibleScriptDefinitionsSource(
    private val project: Project
) : ScriptDefinitionsSource {
    override val definitions: Sequence<ScriptDefinition>
        get() = project.basePath?.let { sequenceOf(ScriptDefinition.FromConfigurations(
            ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration),
            ScriptCompilationConfiguration {
                displayName("Crucible Project Config")
                fileExtension("kts")
                filePathPattern("(.*/)?([^/]*\\.)?crucible\\.kts")
                baseClass(KotlinType("net.typho.crucible.script.AbstractProjectConfig"))
                jvm { updateClasspath(listOf(File(it).resolve("crucible.jar"))) }
                ide { acceptedLocations(ScriptAcceptedLocation.Everywhere) }
            },
            ScriptEvaluationConfiguration()
        )) } ?: sequenceOf()
}