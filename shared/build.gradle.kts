plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
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
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.auth)
                implementation(libs.firebase.common)
                implementation(libs.firebase.storage)
                implementation(libs.firebase.database)
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.functions)
                implementation(libs.androidx.lifecycle.viewmodel)
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
            dependencies {
                implementation(libs.firebase.admin)
                implementation(libs.ktor.client.okhttp.jvm)
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xexpect-actual-classes")
    }
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.applyOptIns()
{
    languageSettings.optIn("kotlin.RequiresOptIn")
}

