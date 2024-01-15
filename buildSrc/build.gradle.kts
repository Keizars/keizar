plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

kotlin.sourceSets.all {
    languageSettings {
        enableLanguageFeature("ContextReceivers")
    }
}

sourceSets.main {
    kotlin.srcDir("src")
    resources.srcDir("resources")
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())

    api(libs.kotlin.gradle.plugin) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    api(libs.android.gradle.plugin)
    api(libs.atomicfu.gradle.plugin)
    api(libs.android.application.gradle.plugin)
    api(libs.android.library.gradle.plugin)
    api(libs.compose.multiplatfrom.gradle.plugin)
}
