@file:Suppress("DEPRECATION")
package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import java.io.ObjectInputStream
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

@Suppress("UNCHECKED_CAST")
class CrucibleScriptDefinitionsSource(
    private val project: Project
) : ScriptDefinitionsSource {
    override val definitions: Sequence<ScriptDefinition>
        get() = project.basePath?.let {
            val file = Path(it).resolve(".crucible").resolve("scripts.bin")

            if (file.exists()) {
                ObjectInputStream(file.inputStream()).use {
                    (it.readObject() as List<ScriptCompilationConfiguration>).map {
                        ScriptDefinition.FromConfigurations(
                            ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration),
                            it,
                            ScriptEvaluationConfiguration()
                        )
                    }.asSequence()
                }
            } else null
        } ?: sequenceOf()
}