pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        // Rebaixado para Kotlin 2.0.0 para compatibilidade com Room (kotlinx-metadata)
        id("org.jetbrains.kotlin.android") version "2.0.0" apply false
        id("org.jetbrains.kotlin.kapt") version "2.0.0" apply false

        // Compose Compiler Gradle plugin compatível com Kotlin 2.0
        id("androidx.compose.compiler") version "1.5.3-2.0.0" apply false

        // Android Gradle Plugin
        id("com.android.application") version "8.13.1" apply false
        id("com.android.library") version "8.13.1" apply false
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "androidx.compose.compiler") {
                useModule("androidx.compose.compiler:androidx.compose.compiler.gradle.plugin:1.5.3-2.0.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pesagem.Pagina.Inicial"
include(":app")
