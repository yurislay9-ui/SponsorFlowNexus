/*
 * SponsorFlow Nexus v1.0 - Template Entity
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "templates",
    indices = [Index(value = ["category"]), Index(value = ["isActive"])]
)
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val content: String,
    val category: String = "general",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)