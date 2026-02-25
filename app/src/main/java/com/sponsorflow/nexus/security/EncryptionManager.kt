/*
 * SponsorFlow Nexus v1.0 - Encryption Manager (AES-256-GCM)
 */
package com.sponsorflow.nexus.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.sponsorflow.nexus.core.contracts.security.IEncryptionService
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionManager : IEncryptionService {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    @Volatile
    private var secretKey: SecretKey? = null

    init {
        secretKey = getOrCreateKey()
    }

    override fun encrypt(data: ByteArray, key: ByteArray?): AppResult<ByteArray> = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey ?: getOrCreateKey())
        val encrypted = cipher.doFinal(data)
        val iv = cipher.iv
        AppResult.Success(iv + encrypted)
    } catch (e: SecurityException) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
    } catch (e: Exception) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Encryption failed"))
    }

    override fun decrypt(data: ByteArray, key: ByteArray?): AppResult<ByteArray> = try {
        val iv = data.copyOfRange(0, IV_SIZE)
        val encrypted = data.copyOfRange(IV_SIZE, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey ?: getOrCreateKey(), GCMParameterSpec(TAG_SIZE, iv))
        AppResult.Success(cipher.doFinal(encrypted))
    } catch (e: SecurityException) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
    } catch (e: Exception) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Decryption failed"))
    }

    override fun generateKey(): ByteArray {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGen.init(KEY_SIZE * 8)
        return keyGen.generateKey().encoded
    }

    override fun hashSHA256(input: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    override fun encryptString(text: String): AppResult<String> = try {
        val result = encrypt(text.toByteArray())
        result.map { Base64.getEncoder().encodeToString(it) }
    } catch (e: SecurityException) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
    } catch (e: Exception) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Encrypt string failed"))
    }

    override fun decryptString(encryptedText: String): AppResult<String> = try {
        val data = Base64.getDecoder().decode(encryptedText)
        decrypt(data).map { String(it) }
    } catch (e: SecurityException) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
    } catch (e: Exception) {
        AppResult.Error(AppError.SecurityError(e.message ?: "Decrypt string failed"))
    }

    override fun isReady(): Boolean = secretKey != null

    override fun clearKeys() {
        keyStore.deleteEntry(KEY_ALIAS)
        secretKey = null
    }

    private fun getOrCreateKey(): SecretKey {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE * 8)
            .build()
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGen.init(spec)
        return keyGen.generateKey()
    }

    companion object {
        private const val KEY_ALIAS = "nexus_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12  // GCM IV size in bytes
        private const val KEY_SIZE = 32  // AES-256 key size in bytes
        private const val TAG_SIZE = 128 // GCM tag size in bits
    }
}
