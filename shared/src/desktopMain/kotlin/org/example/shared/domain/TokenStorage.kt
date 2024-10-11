package org.example.shared.domain

/**
 * An interface for storing tokens in an encrypted form.
 */
interface TokenStorage
{
    /**
     * Encrypts the provided token and saves it to the specified file.
     *
     * @param token The token to be encrypted.
     * @param fileName The name of the file where the encrypted token will be saved.
     */
    fun encryptAndSave(token: String, fileName: String)
    /**
     * Reads the contents of the specified file, decrypts it, and returns the decrypted content as a string.
     *
     * @param fileName The name of the file to be read and decrypted.
     * @return The decrypted content as a string, or `null` if the file could not be read or decrypted.
     */
    fun readAndDecrypt(fileName: String): String?
}