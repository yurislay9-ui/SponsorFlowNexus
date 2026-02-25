/*
 * SponsorFlow Nexus v1.0 - Settings Fragment
 * CORREGIDO: Usar SwitchCompat en lugar de Switch deprecated
 */
package com.sponsorflow.nexus.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.sponsorflow.nexus.R

class SettingsFragment : Fragment() {

    // CORREGIDO: Usar SwitchCompat en lugar de android.widget.Switch
    private lateinit var autoReplySwitch: SwitchCompat
    private lateinit var notificationSwitch: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoReplySwitch = view.findViewById(R.id.switch_auto_reply)
        notificationSwitch = view.findViewById(R.id.switch_notifications)
        loadSettings()
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("nexus_settings", 0)
        autoReplySwitch.isChecked = prefs.getBoolean("auto_reply", true)
        notificationSwitch.isChecked = prefs.getBoolean("notifications", true)
    }

    private fun saveSettings() {
        val prefs = requireContext().getSharedPreferences("nexus_settings", 0)
        prefs.edit()
            .putBoolean("auto_reply", autoReplySwitch.isChecked)
            .putBoolean("notifications", notificationSwitch.isChecked)
            .apply()
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }
}