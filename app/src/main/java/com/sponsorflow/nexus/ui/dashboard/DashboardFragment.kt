/*
 * SponsorFlow Nexus v1.0 - Dashboard Fragment
 */
package com.sponsorflow.nexus.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sponsorflow.nexus.R
import com.sponsorflow.nexus.ui.AssistantChatActivity

class DashboardFragment : Fragment() {

    private var isActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val toggleButton = view.findViewById<Button>(R.id.btn_toggle)
        val aiHelpButton = view.findViewById<Button>(R.id.btn_ai_help)
        
        toggleButton.setOnClickListener {
            toggleService()
        }
        
        aiHelpButton.setOnClickListener {
            openAssistantChat()
        }
        
        updateButtonState(toggleButton)
    }

    private fun toggleService() {
        isActive = !isActive
        view?.findViewById<Button>(R.id.btn_toggle)?.let { updateButtonState(it) }
        
        val message = if (isActive) "Servicio activado" else "Servicio desactivado"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openAssistantChat() {
        startActivity(Intent(requireContext(), AssistantChatActivity::class.java))
    }

    private fun updateButtonState(button: Button) {
        button.text = if (isActive) "Detener Asistente" else "Activar Asistente"
    }
}
