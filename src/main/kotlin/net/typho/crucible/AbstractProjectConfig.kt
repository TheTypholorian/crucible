package net.typho.crucible

import net.typho.crucible.deps.Dependencies
import net.typho.crucible.deps.Repositories

abstract class AbstractProjectConfig {
    val repositories by Crucible::repositories
    val dependencies by Crucible::dependencies
    var mainClass by Crucible::mainClass

    fun repositories(action: Repositories.() -> Unit) = action(repositories)

    fun dependencies(action: Dependencies.() -> Unit) = action(dependencies)
}