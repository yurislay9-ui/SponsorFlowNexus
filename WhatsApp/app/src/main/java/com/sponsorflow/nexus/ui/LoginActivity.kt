/*
 * Workflow Hub - Login Activity
 * Gmail sign-in flow
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sponsorflow.nexus.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if already logged in
        val prefs = getSharedPreferences("hub_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            goToMain()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        // Gmail Sign In
        findViewById<Button>(R.id.btn_gmail).setOnClickListener {
            signInWithGmail()
        }

        // Continue with email
        findViewById<Button>(R.id.btn_continue).setOnClickListener {
            val emailInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_email)
            val email = emailInput?.text?.toString()
            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Send verification code
            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

    private fun signInWithGmail() {
        // In a real app, this would use Google Sign-In API
        // For demo, we'll simulate the flow
        Toast.makeText(this, "Iniciando sesión con Google...", Toast.LENGTH_SHORT).show()
        
        // Simulate successful sign-in
        val prefs = getSharedPreferences("hub_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("login_method", "gmail")
            .putLong("login_time", System.currentTimeMillis())
            .apply()
        
        goToPhoneNumbers()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToPhoneNumbers() {
        startActivity(Intent(this, PhoneNumbersActivity::class.java))
        finish()
    }
}
