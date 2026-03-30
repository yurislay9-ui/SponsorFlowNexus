package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sponsorflow.nexus.R

class PhoneNumbersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_numbers)

        setupViews()
    }

    private fun setupViews() {
        // Back button
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        // Plan info - Load from preferences (use same prefs as LoginActivity)
        val prefs = getSharedPreferences("hub_prefs", MODE_PRIVATE)
        val planName = prefs.getString("plan_name", "Plan Gratis")
        findViewById<TextView>(R.id.tv_plan_name)?.text = planName

        // Update plan limit based on tier
        val planLimit = when (planName) {
            "Plan VIP" -> "3 números permitidos"
            "Plan Avanzado", "Plan Básico" -> "1 número permitido"
            else -> "1 número permitido"
        }
        findViewById<TextView>(R.id.tv_plan_limit)?.text = planLimit

        // Show/hide VIP number based on plan
        val cardNumber3 = findViewById<CardView>(R.id.card_number_3)
        val cardNumber2 = findViewById<CardView>(R.id.card_number_2)
        val cardUpgrade = findViewById<CardView>(R.id.card_upgrade)

        when (planName) {
            "Plan VIP" -> {
                cardNumber2?.isClickable = true
                cardNumber3?.isClickable = true
                cardUpgrade?.visibility = android.view.View.GONE
            }
            "Plan Avanzado", "Plan Básico" -> {
                cardNumber2?.isClickable = true
                cardNumber3?.isClickable = false
                cardNumber3?.alpha = 0.5f
                cardUpgrade?.visibility = android.view.View.VISIBLE
            }
            else -> {
                cardNumber2?.isClickable = false
                cardNumber2?.alpha = 0.5f
                cardNumber3?.isClickable = false
                cardNumber3?.alpha = 0.5f
                cardUpgrade?.visibility = android.view.View.VISIBLE
            }
        }

        // Edit buttons
        findViewById<ImageView>(R.id.btn_edit_1)?.setOnClickListener {
            Toast.makeText(this, "Configurar número principal", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_edit_2)?.setOnClickListener {
            Toast.makeText(this, "Agregar número secundario", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_edit_3)?.setOnClickListener {
            Toast.makeText(this, "Agregar número VIP", Toast.LENGTH_SHORT).show()
        }

        // Upgrade card click
        cardUpgrade?.setOnClickListener {
            Toast.makeText(this, "Actualizando a Premium...", Toast.LENGTH_SHORT).show()
        }

        // Auto-navigate to MainActivity after setup
        android.os.Handler(mainLooper).postDelayed({
            if (!isFinishing) {
                goToMain()
            }
        }, 500)
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
