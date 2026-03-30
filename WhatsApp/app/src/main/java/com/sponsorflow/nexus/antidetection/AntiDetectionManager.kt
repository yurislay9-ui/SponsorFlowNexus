package com.sponsorflow.nexus.antidetection

import android.content.Context

class AntiDetectionManager(private val context: Context) {
    @Suppress("UNUSED_PARAMETER")
    fun canRespond(phone: String) = AntiDetectionResult(true)

    @Suppress("UNUSED_PARAMETER")
    fun recordResponse(phone: String) {}

    fun filterBotPhrases(text: String) = text
}
