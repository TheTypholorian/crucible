import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JavaExecTask

repositories {
    mavenCentral()
    typhoNet()
}

dependencies {
    kotlin("stdlib")
    add("net.typho:data_util:1.3.5")
}

registerTask("run", JavaExecTask(
    classpath = { classpath + CompileTask() },
    mainClass = { "Test" }
))