plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(project(":rule-engine"))
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.server.test.host)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.ktor.server.call.logging)
}
