package net.typho.crucible

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

abstract class ProjectConfig {
    object ScriptConfig : ScriptCompilationConfiguration({
        baseClass(ProjectConfig::class)
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }) {
        private fun readResolve(): Any = ScriptConfig
    }
}