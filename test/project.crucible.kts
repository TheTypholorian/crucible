import net.typho.crucible.deps.DependencyCoordinates

log.debug("Loading config script")

repositories.add(mavenCentral())
repositories.add(typhoNet())

dependencies.add(kotlin("stdlib"))
dependencies.add(DependencyCoordinates("net.typho:data_util:1.3.4"))

mainClass = "Test"