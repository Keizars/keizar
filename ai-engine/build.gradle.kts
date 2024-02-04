plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":rule-engine"))
    implementation(project(":utils:communication"))
    runtimeOnly(libs.slf4j.simple)
}
