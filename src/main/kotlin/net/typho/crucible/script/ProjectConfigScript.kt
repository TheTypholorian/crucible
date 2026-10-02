package net.typho.crucible.script

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object ProjectConfigScript : ScriptCompilationConfiguration({
    baseClass(AbstractProjectConfig::class)
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
}) {
    private fun readResolve(): Any = ProjectConfigScript
}