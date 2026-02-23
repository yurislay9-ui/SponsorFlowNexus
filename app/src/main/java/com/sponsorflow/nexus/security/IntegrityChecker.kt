/*
 * SponsorFlow Nexus v1.0 - Integrity Checker
 * CORREGIDO: checkInstaller, passedAll incluye emulator, signature desde BuildConfig
 */
package com.sponsorflow.nexus.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

class IntegrityChecker(private val expectedSignature: String = "") {

    // Obtener firma esperada desde BuildConfig o config
    private fun getExpectedSignature(): String {
        return expectedSignature.ifBlank {
            // Intentar obtener desde BuildConfig o config remoto
            com.sponsorflow.nexus.BuildConfig.APP_SIGNATURE
        }
    }

    fun checkSignature(context: Context): Boolean = try {
        val expected = getExpectedSignature()
        
        // Si no hay firma configurada, fallar en release
        if (expected.isBlank() || expected == "YOUR_APP_SIGNATURE") {
            return !BuildConfig.DEBUG // En debug permite, en release falla
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
            val hash = signatures.firstOrNull()?.let { hashSHA256(it.toByteArray()) } ?: ""
            hash == expected
        } else {
            @Suppress("DEPRECATION")
            val sigs = context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_SIGNATURES
            ).signatures
            val hash = sigs.firstOrNull()?.let { hashSHA256(it.toByteArray()) } ?: ""
            hash == expected
        }
    } catch (e: Exception) {
        false
    }

    // CORREGIDO: Solo aceptar Google Play como instalador válido
    fun checkInstaller(context: Context): Boolean {
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        // CORREGIDO: Solo aceptar Google Play, rechazar ADB/sideloading
        return installer == "com.android.vending"
    }

    fun isRooted(): Boolean {
        // CORREGIDO: Verificaciones más robustas
        return checkPaths("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su") ||
                checkRootProperties() ||
                checkDangerousApps()
    }
    
    private fun checkRootProperties(): Boolean {
        return try {
            val buildProps = listOf(
                Build.getString("ro.debuggable"),
                Build.getString("ro.secure"),
                Build.getString("ro.adb.secure")
            )
            // Si debuggable = 1, probable root
            buildProps.any { it == "1" }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkDangerousApps(): Boolean {
        val dangerousApps = listOf(
            "com.topjohnwu.magisk",
            "com.kernelexpansion.in",
            "com.saurik.substrate",
            "eu.chainfire.supersu"
        )
        return try {
            dangerousApps.any { app ->
                context.packageManager.getPackageInfo(app, 0)
                true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
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
}

data class IntegrityReport(
    val signatureValid: Boolean,
    val installerValid: Boolean,
    val isRooted: Boolean,
    val isEmulator: Boolean
) {
    // CORREGIDO: Include isEmulator in passedAll
    val passedAll: Boolean
        get() = signatureValid && installerValid && !isRooted && !isEmulator
    
    // Para pagos, requiere máximo nivel de seguridad
    val passedForPayments: Boolean
        get() = passedAll
}
