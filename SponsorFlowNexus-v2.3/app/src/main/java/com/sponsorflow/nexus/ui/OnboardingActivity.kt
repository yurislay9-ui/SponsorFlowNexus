/*
 * SponsorFlow Nexus v2.4 - Onboarding Activity
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sponsorflow.nexus.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        // Conectar botón Comenzar
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        val prefs = getSharedPreferences("nexus_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
