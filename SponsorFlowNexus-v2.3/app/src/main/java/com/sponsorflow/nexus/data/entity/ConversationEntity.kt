/*
 * SponsorFlow Nexus v2.3 - Conversation Entity
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["contactId"]), Index(value = ["timestamp"])]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val message: String,
    val isIncoming: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val wasAutoReplied: Boolean = false
)