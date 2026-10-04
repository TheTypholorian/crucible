package net.typho.crucible.deps

import net.typho.crucible.Crucible

class Dependencies : ArrayList<DependencyName>() {
    fun add(
        group: String,
        artifact: String,
        version: String
    ) = add(DependencyName("$group:$artifact:$version"))

    fun add(coordinates: String) = add(DependencyName(coordinates))

    fun kotlin(module: String, version: String = Crucible.kotlinVersion) = add(DependencyName("org.jetbrains.kotlin:kotlin-$module:$version"))
}