package net.typho.crucible.script

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Crucible Project Config",
    fileExtension = "crucible.kts",
    compilationConfiguration = ProjectConfigScript::class
)
abstract class ProjectConfigScriptDefinition : AbstractProjectConfig()