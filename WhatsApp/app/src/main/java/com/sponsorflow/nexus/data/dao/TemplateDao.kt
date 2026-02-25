/*
 * SponsorFlow Nexus v1.0 - Template DAO
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.TemplateEntity

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TemplateEntity): Long

    @Update
    suspend fun update(template: TemplateEntity)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getById(id: Long): TemplateEntity?

    @Query("SELECT * FROM templates WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAll(): List<TemplateEntity>

    @Query("SELECT * FROM templates WHERE category = :category AND isActive = 1")
    suspend fun getByCategory(category: String): List<TemplateEntity>

    @Query("SELECT DISTINCT category FROM templates WHERE isActive = 1")
    suspend fun getCategories(): List<String>

    @Query("SELECT COUNT(*) FROM templates WHERE isActive = 1")
    suspend fun getCount(): Int
}