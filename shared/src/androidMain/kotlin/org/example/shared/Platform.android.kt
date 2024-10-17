package org.example.shared

import android.os.Build

class AndroidPlatform : Platform
{
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

@Suppress("unused")
actual fun getPlatform(): Platform = AndroidPlatform()