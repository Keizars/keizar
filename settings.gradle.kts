rootProject.name = "keizar"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Compose Multiplatform pre-release versions
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-atomicfu") { // atomicfu is not on Gradle Plugin Portal
                useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

includeProject(":app", "app")
includeProject(":ci-helper", "ci-helper") 
includeProject(":rule-engine", "rule-engine")
includeProject(":utils:slf4j-kt", "utils/slf4j-kt")
includeProject(":ai-engine", "ai-engine")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
