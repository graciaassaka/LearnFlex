plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    androidTarget()

    // Add JVM (desktop) target
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.server.tests)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.logback)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.espresso.core)
            }
        }

        val desktopMain by getting {
            dependencies {
                // Add desktop-specific dependencies here if any
            }
        }

        val desktopTest by getting {
            dependencies {
                // Add desktop-specific test dependencies here if any
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
}
