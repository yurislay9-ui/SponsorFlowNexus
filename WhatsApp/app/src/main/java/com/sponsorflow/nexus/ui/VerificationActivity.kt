/*
 * Workflow Hub - Verification Activity
 * Email verification code
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sponsorflow.nexus.R

class VerificationActivity : AppCompatActivity() {

    private var email: String = ""
    private var countDownTimer: CountDownTimer? = null
    
    private lateinit var etCode1: EditText
    private lateinit var etCode2: EditText
    private lateinit var etCode3: EditText
    private lateinit var etCode4: EditText
    private lateinit var etCode5: EditText
    private lateinit var etCode6: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        email = intent.getStringExtra("email") ?: ""
        
        setupViews()
        startTimer()
    }

    private fun setupViews() {
        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // Email text
        findViewById<TextView>(R.id.tv_email).apply {
            text = "Te enviamos un código a $email"
            visibility = View.GONE // Hidden since we use tv_subtitle
        }

        // Code inputs
        etCode1 = findViewById(R.id.et_code_1)
        etCode2 = findViewById(R.id.et_code_2)
        etCode3 = findViewById(R.id.et_code_3)
        etCode4 = findViewById(R.id.et_code_4)
        etCode5 = findViewById(R.id.et_code_5)
        etCode6 = findViewById(R.id.et_code_6)

        // Auto-focus and move between inputs
        setupCodeInput(etCode1, null, etCode2)
        setupCodeInput(etCode2, etCode1, etCode3)
        setupCodeInput(etCode3, etCode2, etCode4)
        setupCodeInput(etCode4, etCode3, etCode5)
        setupCodeInput(etCode5, etCode4, etCode6)
        setupCodeInput(etCode6, etCode5, null)

        // Verify button
        findViewById<View>(R.id.btn_verify).setOnClickListener {
            verifyCode()
        }

        // Resend button
        findViewById<View>(R.id.btn_resend).setOnClickListener {
            resendCode()
        }
    }

    private fun setupCodeInput(current: EditText, prev: EditText?, next: EditText?) {
        current.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && current.text.isNullOrEmpty() && prev != null) {
                prev.requestFocus()
            }
        }
    }

    private fun verifyCode() {
        val code = "${etCode1.text}${etCode2.text}${etCode3.text}${etCode4.text}${etCode5.text}${etCode6.text}"
        
        if (code.length != 6) {
            Toast.makeText(this, "Ingresa el código completo", Toast.LENGTH_SHORT).show()
            return
        }

        // In production, verify code with server
        // For demo, accept "123456" or any 6-digit code
        if (code == "123456" || code.length == 6) {
            // Save login state
            val prefs = getSharedPreferences("hub_prefs", MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("login_method", "email")
                .putString("user_email", email)
                .putLong("login_time", System.currentTimeMillis())
                .apply()

            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
            
            // Go to phone numbers setup
            startActivity(Intent(this, PhoneNumbersActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendCode() {
        Toast.makeText(this, "Código reenviado", Toast.LENGTH_SHORT).show()
        startTimer()
    }

    private fun startTimer() {
        val btnResend = findViewById<TextView>(R.id.btn_resend)
        val tvTimer = findViewById<TextView>(R.id.tv_timer)
        
        btnResend.isEnabled = false
        btnResend.alpha = 0.5f
        tvTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = "Espera $seconds segundos"
            }

            override fun onFinish() {
                btnResend.isEnabled = true
                btnResend.alpha = 1.0f
                tvTimer.visibility = View.GONE
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
