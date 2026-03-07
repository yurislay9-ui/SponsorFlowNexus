package com.sponsorflow.nexus.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sponsorflow.nexus.R
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private var tvTotalSent: TextView? = null
    private var tvTotalFailed: TextView? = null
    private var tvSuccessRate: TextView? = null
    private var tvDailyCount: TextView? = null
    private var tvStatus: TextView? = null
    private var rvRecentActivity: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeViewModel()
        viewModel.loadDashboardData()
    }

    private fun initViews(view: View) {
        tvTotalSent = view.findViewById(R.id.tv_total_sent)
        tvTotalFailed = view.findViewById(R.id.tv_total_failed)
        tvSuccessRate = view.findViewById(R.id.tv_success_rate)
        tvDailyCount = view.findViewById(R.id.tv_daily_count)
        tvStatus = view.findViewById(R.id.tv_status)
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity)
        rvRecentActivity?.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: DashboardState) {
        tvTotalSent?.text = state.totalMessagesSent.toString()
        tvTotalFailed?.text = state.totalMessagesFailed.toString()
        tvSuccessRate?.text = String.format("%.1f%%", state.successRate * 100)
        tvDailyCount?.text = state.dailyMessageCount.toString()
        tvStatus?.text = state.statusText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tvTotalSent = null
        tvTotalFailed = null
        tvSuccessRate = null
        tvDailyCount = null
        tvStatus = null
        rvRecentActivity = null
    }

    companion object {
        fun newInstance(): DashboardFragment = DashboardFragment()
    }
}