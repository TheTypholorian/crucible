package net.typho.crucible.script

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget

object ProjectConfigScript : ScriptCompilationConfiguration({
    baseClass(AbstractProjectConfig::class)
    jvm {
        jvmTarget("21")
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
}) {
    private fun readResolve(): Any = ProjectConfigScript
}