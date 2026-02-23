/*
 * SponsorFlow Nexus v1.0 - Semantic Version Comparator
 */
package com.sponsorflow.nexus.core.util

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: String? = null
) : Comparable<SemanticVersion> {
    
    override fun compareTo(other: SemanticVersion): Int {
        // Comparar major
        if (major != other.major) return major.compareTo(other.major)
        // Comparar minor
        if (minor != other.minor) return minor.compareTo(other.minor)
        // Comparar patch
        if (patch != other.patch) return patch.compareTo(other.patch)
        // Comparar prerelease (null = release > prerelease)
        return comparePrerelease(prerelease, other.prerelease)
    }
    
    private fun comparePrerelease(a: String?, b: String?): Int {
        if (a == null && b == null) return 0
        if (a == null) return 1  // release > prerelease
        if (b == null) return -1
        return a.compareTo(b)
    }
    
    override fun toString(): String {
        val base = "$major.$minor.$patch"
        return if (prerelease != null) "$base-$prerelease" else base
    }
    
    companion object {
        fun parse(version: String): SemanticVersion {
            val clean = version.trim()
                .removePrefix("v")
                .removePrefix("V")
            
            val parts = clean.split("-", limit = 2)
            val versionParts = parts[0].split(".")
            
            return SemanticVersion(
                major = versionParts.getOrNull(0)?.toIntOrNull() ?: 0,
                minor = versionParts.getOrNull(1)?.toIntOrNull() ?: 0,
                patch = versionParts.getOrNull(2)?.toIntOrNull() ?: 0,
                prerelease = parts.getOrNull(1)
            )
        }
    }
}

class VersionComparator {
    
    // Comparar dos versiones
    fun compare(current: String, minimum: String): VersionResult {
        val currentVer = SemanticVersion.parse(current)
        val minVer = SemanticVersion.parse(minimum)
        
        return when {
            currentVer >= minVer -> VersionResult.UpToDate
            currentVer.major < minVer.major -> VersionResult.MajorUpdateRequired
            else -> VersionResult.UpdateAvailable
        }
    }
    
    // Verificar si necesita actualización
    fun needsUpdate(current: String, minimum: String): Boolean {
        return compare(current, minimum) != VersionResult.UpToDate
    }
    
    // Verificar si es actualización mayor
    fun isMajorUpdate(current: String, minimum: String): Boolean {
        return compare(current, minimum) == VersionResult.MajorUpdateRequired
    }
}

enum class VersionResult {
    UpToDate,           // Versión actual es suficiente
    UpdateAvailable,    // Hay actualización disponible
    MajorUpdateRequired // Actualización mayor requerida (ej: 1.x -> 2.x)
}