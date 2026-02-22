/*
 * SponsorFlow Nexus v2.3 - Repository Interface
 * Skill: Mejores prácticas - Interface genérica CRUD
 */
package com.sponsorflow.nexus.core.contracts.repository

import com.sponsorflow.nexus.core.result.AppResult

interface IRepository<T, ID> {
    suspend fun insert(entity: T): AppResult<ID>
    suspend fun update(entity: T): AppResult<Unit>
    suspend fun delete(id: ID): AppResult<Unit>
    suspend fun getById(id: ID): AppResult<T?>
    suspend fun getAll(): AppResult<List<T>>
}