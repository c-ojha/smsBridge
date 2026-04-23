package com.smsbridge.util

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts/decrypts sensitive values (auth tokens, API keys) using AES-GCM backed
 * by Android Keystore. Encrypted values are safe to store in Room.
 */
class SecureStorage(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "smsbridge_secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun encrypt(plainText: String): String {
        val keyAlias = "smsbridge_header_key"
        val keystore = java.security.KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

        val secretKey: SecretKey = if (keystore.containsAlias(keyAlias)) {
            (keystore.getEntry(keyAlias, null) as java.security.KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGen = javax.crypto.KeyGenerator.getInstance(
                android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGen.init(
                android.security.keystore.KeyGenParameterSpec.Builder(
                    keyAlias,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGen.generateKey()
        }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + cipherText
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            val keyAlias = "smsbridge_header_key"
            val keystore = java.security.KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
            if (!keystore.containsAlias(keyAlias)) {
                Log.e("SecureStorage", "Keystore key not found — was the app reinstalled?")
                return ""
            }
            val secretKey = (keystore.getEntry(keyAlias, null) as java.security.KeyStore.SecretKeyEntry).secretKey

            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size < 13) {
                Log.e("SecureStorage", "Encrypted data too short (${combined.size} bytes), skipping")
                return ""
            }
            val iv = combined.copyOfRange(0, 12)
            val cipherText = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("SecureStorage", "Decryption failed: ${e.message}")
            ""
        }
    }

    fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun getString(key: String, default: String = "") = prefs.getString(key, default) ?: default
    fun remove(key: String) = prefs.edit().remove(key).apply()
}
