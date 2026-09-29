package net.typho.crucible.scripts

import net.typho.crucible.AbstractProjectConfig
import net.typho.crucible.ProjectConfigScript
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Crucible Project Config",
    fileExtension = "crucible.kts",
    compilationConfiguration = ProjectConfigScript::class
)
abstract class ProjectConfigScriptDefinition : AbstractProjectConfig()