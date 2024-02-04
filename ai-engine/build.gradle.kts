plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":rule-engine"))
    implementation(project(":utils:communication"))
    implementation(project(":client"))
    implementation(project(":server"))
    runtimeOnly(libs.slf4j.simple)
}
