package com.sponsorflow.nexus.core

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class NexusAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() { super.onServiceConnected() }
}
