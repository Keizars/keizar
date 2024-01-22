plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

configureFlattenSourceSets()

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.serialization.json)
}