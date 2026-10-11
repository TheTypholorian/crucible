repositories {
    mavenCentral()
    typhoNet()
}

dependencies {
    kotlin("stdlib")
    add("net.typho:data_util:1.3.5")
    add("net.typho:asm_util:1.3.6")
}

registerTask<JavaExecTask>("run") {
    classpath.addAll { dependencies.paths }
    classpath.addAll(CompileTask.Main)
    mainClass.set("Test")
}