plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.dotenv)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinx.parcelize) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
}