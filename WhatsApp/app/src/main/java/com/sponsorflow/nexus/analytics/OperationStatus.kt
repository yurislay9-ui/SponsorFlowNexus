package com.sponsorflow.nexus.analytics

enum class OperationStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED;

    val isActive: Boolean
        get() = this == RUNNING || this == PAUSED

    val isTerminal: Boolean
        get() = this == COMPLETED || this == FAILED || this == CANCELLED
}