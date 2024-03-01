plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(project(":rule-engine"))
    implementation(project(":utils:communication"))
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.server.test.host)
    implementation(libs.ktor.server.websocket.jvm)
    implementation(libs.postgresql)
    implementation(libs.h2database)
    implementation(libs.ktor.server.call.logging)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.mongodb.driver.kotlin.coroutine)

    implementation(libs.aws.s3)
    implementation(libs.aws.s3control)
    implementation(libs.aws.sts)
    implementation(libs.aws.secretsmanager)
}

application {
    mainClass.set("org.keizar.server.ApplicationKt")
}