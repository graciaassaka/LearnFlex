package org.example.shared.util.validation

enum class InvalidInputMessage(val message: String)
{
    EMPTY_FIELD("Field cannot be empty"),
    EMAIL_FORMAT("Invalid email format"),
    PASSWORD_CONTAINS_SPACES("Password cannot contain spaces"),
    PASSWORD_NO_LETTER("Password must contain at least one letter"),
    PASSWORD_NO_NUMBER("Password must contain at least one number"),
    PASSWORD_NO_UPPERCASE_LETTER("Password must contain at least one uppercase letter"),
    PASSWORD_NO_LOWER_CASE_LETTER("Password must contain at least one lowercase letter"),
    PASSWORD_NO_SPECIAL_CHARACTER("Password must contain at least one special character"),
    PASSWORD_LENGTH("Password must be between 8 and 20 characters"),
    PASSWORD_CONFIRMATION_DOES_NOT_MATCH("Password confirmation does not match")
}