package com.sponsorflow.nexus.queue

enum class MessagePriority(val value: Int) {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);

    companion object {
        fun fromValue(value: Int): MessagePriority {
            return values().firstOrNull { it.value == value } ?: NORMAL
        }
    }
}