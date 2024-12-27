package org.example.shared.domain.constant.interfaces

/**
 * Interface for enums that have a value.
 */
interface ValuableEnum<T> {

    /**
     * The value of the enumeration.
     */
    val value: T
}