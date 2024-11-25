import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use { stream ->
        localProperties.load(stream)
    }
}

fun getLocalProperty(key: String, defaultValue: String = ""): String = localProperties.getProperty(key, defaultValue)

tasks.register("generateConstants") {
    doLast {
        val generated = """
            package org.example.shared.data.util
            
            object FirebaseConstants {
                const val APP_ID = "${getLocalProperty("FIREBASE_APP_ID")}"
                const val API_KEY = "${getLocalProperty("FIREBASE_API_KEY")}"
                const val PROJECT_ID = "${getLocalProperty("FIREBASE_PROJECT_ID")}"
            }
            
            object OpenAIConstants {
                const val API_KEY = "${getLocalProperty("OPENAI_API_KEY")}"
                const val STYLE_ASSISTANT_ID = "${getLocalProperty("OPENAI_STYLE_ASSISTANT_ID")}"
            }
        """.trimIndent()

        val outputDir = project.layout.buildDirectory.get().asFile
            .resolve("generated/kotlin/org/example/shared/data/util")
        outputDir.mkdirs()

        val outputFile = outputDir.resolve("Constants.kt")
        outputFile.writeText(generated)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateConstants")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        compilations["main"].compileTaskProvider.configure {
            dependsOn("generateConstants")
        }
    }

    sourceSets {
        all {
            applyOptIns()
        }
        val commonMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin"))
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)
                implementation(libs.ktor.client.plugins)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.log)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.gitlive.firestore)
                implementation(libs.room.runtime)
                implementation(libs.coil)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.test.junit)
                implementation(libs.mockk)
                implementation(libs.mockito.core)
                implementation(libs.mockito.kotlin)
                implementation(libs.mockito.inline)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.slf4j)
                implementation(libs.wiremock)
                implementation(libs.mockk.agent.jvm)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.koin.android.compat)
                implementation(project.dependencies.platform(libs.firebase.boom))
                implementation(libs.firebase.auth)
                implementation(libs.firebase.storage)
                implementation(libs.ktor.client.android)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.test.junit)
                implementation(libs.mockk)
                implementation(libs.mockito.core)
                implementation(libs.mockito.kotlin)
                implementation(libs.mockito.inline)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.slf4j)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.espresso.core)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.rules)
                implementation(libs.mockk.android)
                implementation(libs.robolectric)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp.jvm)
                implementation(libs.gitlive.java)
                implementation(libs.sqlite)
                implementation(libs.ktor.client.jvm)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.test.junit)
                implementation(libs.mockk)
                implementation(libs.mockito.core)
                implementation(libs.mockito.kotlin)
                implementation(libs.mockito.inline)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.slf4j)
            }
        }
    }
}

android {
    namespace = "org.example.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mapOf(
            "clearPackageData" to "true"
        )
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        android.buildFeatures.buildConfig = true
        getByName("debug") {
            buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", "true")
        }
        getByName("release") {
            buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", "false")
        }
    }
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/io.netty.versions.properties"
            // Additional exclusions or pickFirsts if needed
        }
    }
}

dependencies {
    implementation(libs.ktor.client.okhttp.jvm)
    androidTestImplementation(libs.androidx.test.junit)
    add("kspCommonMainMetadata", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    androidTestImplementation(libs.room.testing)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xexpect-actual-classes")
    }
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.applyOptIns() {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

