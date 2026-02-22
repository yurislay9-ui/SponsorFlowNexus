/*
 * SponsorFlow Nexus v2.3 - Encryption Service Interface
 * Skill: Seguridad - Cifrado AES-256-GCM
 */
package com.sponsorflow.nexus.core.contracts.security

import com.sponsorflow.nexus.core.result.AppResult

interface IEncryptionService {
    fun encrypt(data: ByteArray, key: ByteArray? = null): AppResult<ByteArray>
    fun decrypt(data: ByteArray, key: ByteArray? = null): AppResult<ByteArray>
    fun generateKey(): ByteArray
    fun hashSHA256(input: String): String
    fun encryptString(text: String): AppResult<String>
    fun decryptString(encryptedText: String): AppResult<String>
    fun isReady(): Boolean
    fun clearKeys()

    companion object {
        const val KEY_SIZE = 32
        const val IV_SIZE = 12
    }
}