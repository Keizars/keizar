plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(project(":rule-engine"))
    implementation(projects.protocol)
    implementation(projects.utils.coroutines)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    api(libs.koin.core)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.serialization.kotlinx.json)
    runtimeOnly(libs.slf4j.simple)
    implementation(projects.utils.slf4jKt)
}
