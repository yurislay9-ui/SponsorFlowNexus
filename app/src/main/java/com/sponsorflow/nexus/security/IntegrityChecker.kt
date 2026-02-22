/*
 * SponsorFlow Nexus v2.3 - Integrity Checker
 */
package com.sponsorflow.nexus.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class IntegrityChecker(private val expectedSignature: String) {

    fun checkSignature(context: Context): Boolean = try {
        val sigs = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNATURES
        ).signatures
        val hash = sigs.firstOrNull()?.let { hashSHA256(it.toByteArray()) } ?: ""
        hash == expectedSignature
    } catch (e: Exception) {
        false
    }

    fun checkInstaller(context: Context): Boolean {
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        return installer == "com.android.vending" || installer == null
    }

    fun isRooted(): Boolean {
        return checkPaths("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su")
    }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion")
    }

    fun runAllChecks(context: Context): IntegrityReport {
        return IntegrityReport(
            signatureValid = checkSignature(context),
            installerValid = checkInstaller(context),
            isRooted = isRooted(),
            isEmulator = isEmulator()
        )
    }

    private fun checkPaths(vararg paths: String): Boolean {
        return paths.any { java.io.File(it).exists() }
    }

    private fun hashSHA256(data: ByteArray): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        return md.digest(data).joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val EXPECTED_SIGNATURE = "YOUR_APP_SIGNATURE"
    }
}

data class IntegrityReport(
    val signatureValid: Boolean,
    val installerValid: Boolean,
    val isRooted: Boolean,
    val isEmulator: Boolean
) {
    val passedAll: Boolean
        get() = signatureValid && installerValid && !isRooted
}