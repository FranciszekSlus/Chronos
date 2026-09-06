package com.tasker.chronos.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Simple password-based note encryption (AES-GCM + PBKDF2).
 * Format: ENC1:<base64(salt|iv|ciphertext)>
 */
object NoteCrypto {
    private const val PREFIX = "ENC1:"
    private const val ITERATIONS = 12_000
    private const val KEY_BITS = 256
    private const val GCM_TAG_BITS = 128
    private const val SALT_LEN = 16
    private const val IV_LEN = 12

    fun isEncrypted(content: String): Boolean = content.startsWith(PREFIX)

    fun encrypt(plain: String, password: String): String {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val payload = salt + iv + encrypted
        return PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(encryptedContent: String, password: String): String? {
        if (!isEncrypted(encryptedContent)) return encryptedContent
        return try {
            val raw = Base64.decode(encryptedContent.removePrefix(PREFIX), Base64.NO_WRAP)
            if (raw.size < SALT_LEN + IV_LEN + 1) return null
            val salt = raw.copyOfRange(0, SALT_LEN)
            val iv = raw.copyOfRange(SALT_LEN, SALT_LEN + IV_LEN)
            val cipherBytes = raw.copyOfRange(SALT_LEN + IV_LEN, raw.size)
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
