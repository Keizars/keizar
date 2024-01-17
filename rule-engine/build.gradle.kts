plugins {
    kotlin("jvm")
}

configureFlattenSourceSets()

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
}