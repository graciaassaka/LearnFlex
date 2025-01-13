package org.example.composeApp.presentation.ui.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a long to a date string.
 * @receiver The long to convert.
 * @return The date string.
 */
fun Long.toDateString(): String =
    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(this))
