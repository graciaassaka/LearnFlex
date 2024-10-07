package org.example.shared

actual fun getPlatform(): Platform {
    return object : Platform {
        override val name: String = "Desktop"
    }
}