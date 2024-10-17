package org.example.shared

@Suppress("unused")
actual fun getPlatform(): Platform {
    return object : Platform {
        override val name: String = "Desktop"
    }
}