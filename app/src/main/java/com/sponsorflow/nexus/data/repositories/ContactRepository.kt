/*
 * SponsorFlow Nexus v1.0 - Contact Repository
 * CORREGIDO: MEDIUM-001 - Validación de entrada
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.contracts.repository.IRepository
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.ContactDao
import com.sponsorflow.nexus.data.entity.ContactEntity

/**
 * Repositorio para gestión de contactos con validación de entrada.
 * Implementa IRepository para operaciones CRUD estándar.
 */
class ContactRepository(private val dao: ContactDao) : IRepository<ContactEntity, Long> {
    
    companion object {
        private const val MIN_PHONE_LENGTH = 8
        private const val MAX_NAME_LENGTH = 100
    }
    
    /**
     * Valida un contacto antes de persistirlo.
     * @throws IllegalArgumentException si la validación falla
     */
    private fun validateContact(entity: ContactEntity) {
        require(entity.phone.isNotBlank()) { "Phone es requerido" }
        require(entity.phone.length >= MIN_PHONE_LENGTH) { 
            "Phone debe tener al menos $MIN_PHONE_LENGTH caracteres" 
        }
        require(entity.name.length <= MAX_NAME_LENGTH) { 
            "Name no puede exceder $MAX_NAME_LENGTH caracteres" 
        }
    }
    
    override suspend fun insert(entity: ContactEntity): AppResult<Long> = try {
        validateContact(entity)
        AppResult.Success(dao.insert(entity))
    } catch (e: IllegalArgumentException) {
        AppResult.Error(AppError.ValidationError(e.message ?: "Validation failed"))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun update(entity: ContactEntity): AppResult<Unit> = try {
        dao.update(entity)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun delete(id: Long): AppResult<Unit> = try {
        dao.deleteById(id)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getById(id: Long): AppResult<ContactEntity?> = try {
        AppResult.Success(dao.getById(id))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getAll(): AppResult<List<ContactEntity>> = try {
        AppResult.Success(dao.getAll())
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun getByPhone(phone: String): AppResult<ContactEntity?> = try {
        AppResult.Success(dao.getByPhone(phone))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }
}