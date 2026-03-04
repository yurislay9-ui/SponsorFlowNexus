/*
 * SponsorFlow Nexus v1.0 - Dashboard Fragment
 * Nuevo diseño profesional
 */
package com.sponsorflow.nexus.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sponsorflow.nexus.R
import com.sponsorflow.nexus.ui.AssistantChatActivity

class DashboardFragment : Fragment() {

    private var isServiceActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        loadStats(view)
    }

    private fun setupViews(view: View) {
        // Service toggle switch
        val switchService = view.findViewById<SwitchMaterial>(R.id.switch_service)
        switchService?.setOnCheckedChangeListener { _, isChecked ->
            isServiceActive = isChecked
            updateStatus(view, isChecked)
        }

        // AI Assistant card
        view.findViewById<View>(R.id.card_ai_assistant)?.setOnClickListener {
            startActivity(Intent(requireContext(), AssistantChatActivity::class.java))
        }

        // Settings button
        view.findViewById<ImageView>(R.id.btn_settings)?.setOnClickListener {
            Toast.makeText(requireContext(), "Configuración", Toast.LENGTH_SHORT).show()
            // Navigate to settings
        }

        // Analytics card
        view.findViewById<View>(R.id.card_analytics)?.setOnClickListener {
            Toast.makeText(requireContext(), "Análisis", Toast.LENGTH_SHORT).show()
        }

        // Products card
        view.findViewById<View>(R.id.card_products)?.setOnClickListener {
            Toast.makeText(requireContext(), "Inventario", Toast.LENGTH_SHORT).show()
        }

        // Subscription card
        view.findViewById<View>(R.id.card_subscription)?.setOnClickListener {
            Toast.makeText(requireContext(), "Suscripción", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStats(view: View) {
        // These would normally load from a ViewModel
        // For now, showing sample data
        view.findViewById<TextView>(R.id.count_messages)?.text = "0"
        view.findViewById<TextView>(R.id.count_chats)?.text = "0"
        view.findViewById<TextView>(R.id.count_responses)?.text = "0"
        view.findViewById<TextView>(R.id.count_products)?.text = "0"
    }

    private fun updateStatus(view: View, active: Boolean) {
        val statusTitle = view.findViewById<TextView>(R.id.status_title)
        val statusSubtitle = view.findViewById<TextView>(R.id.status_subtitle)
        
        if (active) {
            statusTitle?.text = "Servicio Activo"
            statusSubtitle?.text = "Monitoreando conversaciones"
            Toast.makeText(requireContext(), "Asistente activado", Toast.LENGTH_SHORT).show()
        } else {
            statusTitle?.text = "Servicio Detenido"
            statusSubtitle?.text = "Inactivo"
            Toast.makeText(requireContext(), "Asistente detenido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats when returning to dashboard
        view?.let { loadStats(it) }
    }
}
