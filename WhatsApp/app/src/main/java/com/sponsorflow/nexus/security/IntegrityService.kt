package com.sponsorflow.nexus.security

import android.content.Context

class IntegrityService(private val context: Context) {
    fun verify(): Boolean = true
    fun checkRoot(): Boolean = false
}
