package com.sponsorflow.nexus.antidetection

import android.content.Context

class AntiDetectionManager(private val context: Context) {
    fun canRespond(phone: String) = AntiDetectionResult(true)
    fun recordResponse(phone: String) {}
    fun filterBotPhrases(text: String) = text
}
