import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


fun Project.optInForAllSourceSets(qualifiedClassname: String) {
    kotlinSourceSets!!.all {
        languageSettings {
            optIn(qualifiedClassname)
        }
    }
}

fun Project.optInForTestSourceSets(qualifiedClassname: String) {
    kotlinSourceSets!!.matching { it.name.contains("test", ignoreCase = true) }.all {
        languageSettings {
            optIn(qualifiedClassname)
        }
    }
}

fun Project.enableLanguageFeatureForAllSourceSets(qualifiedClassname: String) {
    kotlinSourceSets!!.all {
        languageSettings {
            this.enableLanguageFeature(qualifiedClassname)
        }
    }
}

fun Project.enableLanguageFeatureForTestSourceSets(name: String) {
    allTestSourceSets {
        languageSettings {
            this.enableLanguageFeature(name)
        }
    }
}

fun Project.allTestSourceSets(action: KotlinSourceSet.() -> Unit) {
    kotlinSourceSets!!.all {
        if (this.name.contains("test", ignoreCase = true)) {
            action()
        }
    }
}