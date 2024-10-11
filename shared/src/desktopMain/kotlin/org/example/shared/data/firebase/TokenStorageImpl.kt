package org.example.shared.data.firebase

import org.example.shared.domain.TokenStorage
import java.io.File
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation of the TokenStorage interface for handling token encryption and decryption.
 */
class TokenStorageImpl : TokenStorage {

    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 65536
        private const val KEY_LENGTH = 256
        private const val SALT = "YourSecretSaltHere"
    }

    /**
     * Generates a SecretKeySpec using the provided password.
     *
     * @param password The password used to generate the secret key.
     * @return The generated SecretKeySpec.
     */
    private fun getSecretKey(password: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), SALT.toByteArray(), ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Encrypts the given token and saves it to a file.
     *
     * @param token The token to be encrypted.
     * @param fileName The name of the file where the encrypted token will be saved.
     */
    override fun encryptAndSave(token: String, fileName: String) {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        val secretKey = getSecretKey(System.getProperty("user.name") ?: "defaultPassword")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encrypted = cipher.doFinal(token.toByteArray())
        val combined = iv + encrypted

        val file = File(fileName)
        file.writeBytes(Base64.getEncoder().encode(combined))
    }

    /**
     * Reads and decrypts the token from a file.
     *
     * @param fileName The name of the file from which the token will be read and decrypted.
     * @return The decrypted token as a String.
     */
    override fun readAndDecrypt(fileName: String): String {
        val file = File(fileName)
        if (!file.exists()) return ""

        val combined = Base64.getDecoder().decode(file.readBytes())
        val iv = combined.slice(0 until 16).toByteArray()
        val encrypted = combined.slice(16 until combined.size).toByteArray()

        val ivSpec = IvParameterSpec(iv)
        val secretKey = getSecretKey(System.getProperty("user.name") ?: "defaultPassword")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        return String(cipher.doFinal(encrypted))
    }
}