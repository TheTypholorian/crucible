package net.typho.crucible.scripts

import net.typho.crucible.script.AbstractProjectConfig
import net.typho.crucible.script.ProjectConfigScript
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Crucible Project Config",
    fileExtension = "crucible.kts",
    compilationConfiguration = ProjectConfigScript::class
)
abstract class ProjectConfigScriptDefinition : AbstractProjectConfig()