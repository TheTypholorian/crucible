package net.typho.crucible.plugins

import net.typho.crucible.deps.Repository

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Plugins(
    val repositories: Array<String> = [Repository.MAVEN_CENTRAL, Repository.TYPHO_NET],
    val plugins: Array<String>
)
