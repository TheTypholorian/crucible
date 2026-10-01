package net.typho.crucible.deps

class Repositories : ArrayList<Repository>() {
    fun maven(url: String) = add(Repository.Maven(url))

    fun mavenCentral() = maven(Repository.MAVEN_CENTRAL)

    fun typhoNet() = maven(Repository.TYPHO_NET)
}