@file:Suppress("DEPRECATION")
package net.typho.crucible.intellij.script

import com.intellij.openapi.project.Project
import net.typho.crucible.ide.data.CrucibleIDEData
import net.typho.data_util.impl.JsonFormat
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import java.io.File
import java.io.ObjectInputStream
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

@Suppress("UNCHECKED_CAST")
class CrucibleScriptDefinitionsSource(
    private val project: Project
) : ScriptDefinitionsSource {
    override val definitions: Sequence<ScriptDefinition>
        get() = project.basePath?.let {
            ObjectInputStream(Path(it).resolve(".crucible").resolve("scripts.bin").inputStream()).use {
                (it.readObject() as List<ScriptCompilationConfiguration>).map {
                    ScriptDefinition.FromConfigurations(
                        ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration),
                        it,
                        ScriptEvaluationConfiguration()
                    )
                }.asSequence()
            }
        } ?: sequenceOf()
}