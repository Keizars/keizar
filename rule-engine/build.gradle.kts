plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

configureFlattenSourceSets()

dependencies {
    implementation(projects.utils.protocol)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.serialization.json)
}