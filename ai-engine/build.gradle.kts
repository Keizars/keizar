plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":rule-engine"))
    runtimeOnly(libs.slf4j.simple)
}
