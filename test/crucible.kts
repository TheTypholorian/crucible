import net.typho.crucible.source.CompileTask
import net.typho.crucible.source.JavaExecTask

repositories {
    mavenCentral()
    typhoNet()
}

dependencies {
    kotlin("stdlib")
    add("net.typho:data_util:1.3.5")
    add("net.typho:asm_util:1.3.6")
}

registerTask("run", JavaExecTask(
    classpath = { dependencies.classpath + CompileTask() },
    mainClass = { "Test" }
))