package com.sponsorflow.nexus.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.sponsorflow.nexus.R
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private var etMinDelay: EditText? = null
    private var etMaxDelay: EditText? = null
    private var etMaxDailyMessages: EditText? = null
    private var etBatchSize: EditText? = null
    private var switchBanDetection: SwitchCompat? = null
    private var switchRiskAssessment: SwitchCompat? = null
    private var switchNotifications: SwitchCompat? = null
    private var switchDarkMode: SwitchCompat? = null
    private var btnSave: Button? = null
    private var btnReset: Button? = null
    private var tvVersion: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeViewModel()
        setupListeners()
        viewModel.loadSettings()
    }

    private fun initViews(view: View) {
        etMinDelay = view.findViewById(R.id.et_min_delay)
        etMaxDelay = view.findViewById(R.id.et_max_delay)
        etMaxDailyMessages = view.findViewById(R.id.et_max_daily_messages)
        etBatchSize = view.findViewById(R.id.et_batch_size)
        switchBanDetection = view.findViewById(R.id.switch_ban_detection)
        switchRiskAssessment = view.findViewById(R.id.switch_risk_assessment)
        switchNotifications = view.findViewById(R.id.switch_notifications)
        switchDarkMode = view.findViewById(R.id.switch_dark_mode)
        btnSave = view.findViewById(R.id.btn_save_settings)
        btnReset = view.findViewById(R.id.btn_reset_settings)
        tvVersion = view.findViewById(R.id.tv_version)
    }

    private fun setupListeners() {
        btnSave?.setOnClickListener {
            saveSettings()
        }

        btnReset?.setOnClickListener {
            viewModel.resetToDefaults()
            Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settingsState.collect { state ->
                populateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveResult.collect { result ->
                result?.let {
                    val message = if (it) "Settings saved successfully" else "Failed to save settings"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateUI(state: SettingsState) {
        etMinDelay?.setText(state.minDelayMs.toString())
        etMaxDelay?.setText(state.maxDelayMs.toString())
        etMaxDailyMessages?.setText(state.maxDailyMessages.toString())
        etBatchSize?.setText(state.batchSize.toString())
        switchBanDetection?.isChecked = state.enableBanDetection
        switchRiskAssessment?.isChecked = state.enableRiskAssessment
        switchNotifications?.isChecked = state.enableNotifications
        switchDarkMode?.isChecked = state.darkMode
        tvVersion?.text = state.appVersion
    }

    private fun saveSettings() {
        val minDelay = etMinDelay?.text?.toString()?.toLongOrNull() ?: return
        val maxDelay = etMaxDelay?.text?.toString()?.toLongOrNull() ?: return
        val maxDaily = etMaxDailyMessages?.text?.toString()?.toIntOrNull() ?: return
        val batchSize = etBatchSize?.text?.toString()?.toIntOrNull() ?: return
        val banDetection = switchBanDetection?.isChecked ?: true
        val riskAssessment = switchRiskAssessment?.isChecked ?: true
        val notifications = switchNotifications?.isChecked ?: true
        val darkMode = switchDarkMode?.isChecked ?: false

        viewModel.saveSettings(
            minDelayMs = minDelay,
            maxDelayMs = maxDelay,
            maxDailyMessages = maxDaily,
            batchSize = batchSize,
            enableBanDetection = banDetection,
            enableRiskAssessment = riskAssessment,
            enableNotifications = notifications,
            darkMode = darkMode
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        etMinDelay = null
        etMaxDelay = null
        etMaxDailyMessages = null
        etBatchSize = null
        switchBanDetection = null
        switchRiskAssessment = null
        switchNotifications = null
        switchDarkMode = null
        btnSave = null
        btnReset = null
        tvVersion = null
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}