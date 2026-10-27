package net.typho.crucible.plugins

import net.typho.crucible.deps.Repositories

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Plugins(
    val repositories: Array<String> = [Repositories.MAVEN_CENTRAL, Repositories.TYPHO_NET],
    val plugins: Array<String>
)
