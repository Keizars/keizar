plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(project(":rule-engine"))
    implementation(projects.protocol)
    implementation(projects.utils.slf4jKt)
    implementation(kotlin("reflect"))
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.websocket.jvm)
    implementation(libs.postgresql)
    implementation(libs.h2database)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    testImplementation("org.testng:testng:6.9.6")
    runtimeOnly(libs.slf4j.simple)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(project(":client"))
    implementation(libs.aws.s3)
    implementation(libs.aws.s3control)
    implementation(libs.aws.sts)
    implementation(libs.aws.secretsmanager)
}

application {
    mainClass.set("org.keizar.server.ApplicationKt")
}