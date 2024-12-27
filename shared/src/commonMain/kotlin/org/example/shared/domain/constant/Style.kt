package org.example.shared.domain.constant

import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * Enum representing different styles.
 *
 * @property value The style of learning.
 */
@Suppress("unused")
enum class Style(override val value: String) : ValuableEnum<String> {
    READING("Reading"),
    KINESTHETIC("Kinesthetic")
}