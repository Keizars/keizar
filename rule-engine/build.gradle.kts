plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

configureFlattenSourceSets()

dependencies {
    implementation(project(":utils:communication"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.serialization.json)
}