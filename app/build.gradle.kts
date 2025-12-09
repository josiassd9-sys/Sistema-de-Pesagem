// kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    // Adicionado: plugin do Kotlin para Compose
    id("org.jetbrains.kotlin.plugin.compose")
}

apply(plugin = "kotlin-kapt")

kapt {
    // Inclui classpath de compilação no classpath do KAPT (ajuda processors a encontrar dependências)
    includeCompileClasspath = true
    // Exportar schema do Room durante o processamento de anotações
    arguments {
        arg("room.schemaLocation", "${project.projectDir}/schemas")
    }
}

android {
    namespace = "com.josias.pesagempaginainicial"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.josias.pesagempaginainicial"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Novo DSL para opções do compilador Kotlin (recomenda-se para Kotlin 2.x)
    kotlin {
        // Define o toolchain para JVM 11
        jvmToolchain(11)

        compilerOptions {
            // Usa JvmTarget enumerado para garantir compatibilidade com o plugin Kotlin 2.x
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        // Compose compiler alinhado com Kotlin 2.0.0
        kotlinCompilerExtensionVersion = "1.5.3-2.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Forçar versão do sqlite-jdbc para evitar conflitos transientes
configurations.all {
    resolutionStrategy.force("org.xerial:sqlite-jdbc:3.36.0.3")
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Native SQLite for Room annotation processing (fixes KAPT error on Windows)
    // Use a version known to work with Room's annotation processor on Windows
    kapt("org.xerial:sqlite-jdbc:3.36.0.3")
    // Também adiciona como compileOnly para garantir visibilidade no classpath do processor
    compileOnly("org.xerial:sqlite-jdbc:3.36.0.3")

    // Compose mínimas
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")

    // Testes
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}