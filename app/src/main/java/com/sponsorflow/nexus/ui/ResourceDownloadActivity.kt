/*
 * SponsorFlow Nexus v1.0 - Resource Download Activity
 */
package com.sponsorflow.nexus.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sponsorflow.nexus.R
import com.sponsorflow.nexus.ai.ResourceDownloadManager
import kotlinx.coroutines.launch

class ResourceDownloadActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_download)
        progressBar = findViewById(R.id.progress_bar)
        statusText = findViewById(R.id.text_status)
    }

    fun downloadModel(url: String, fileName: String) {
        val manager = ResourceDownloadManager(this)
        lifecycleScope.launch {
            manager.downloadModel(url, fileName) { progress ->
                progressBar.progress = progress
                statusText.text = "$progress%"
            }
        }
    }
}