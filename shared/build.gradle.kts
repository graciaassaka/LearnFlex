import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
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
        """.trimIndent()

        val outputDir = project.layout.buildDirectory.get().asFile
            .resolve("generated/kotlin/org/example/shared/data/util")
        outputDir.mkdirs()

        val outputFile = outputDir.resolve("FirebaseConstants.kt")
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
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
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
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.log)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.gitlive.auth)
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
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.koin.android.compat)
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
            }
        }

        val desktopMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin"))
            dependencies {
                implementation(libs.ktor.client.okhttp.jvm)
                implementation(libs.gitlive.java)
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xexpect-actual-classes")
    }
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.applyOptIns()
{
    languageSettings.optIn("kotlin.RequiresOptIn")
}

