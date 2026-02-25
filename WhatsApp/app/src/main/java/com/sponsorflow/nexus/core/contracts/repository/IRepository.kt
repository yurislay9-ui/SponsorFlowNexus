/*
 * SponsorFlow Nexus v1.0 - Repository Interface
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

// CORREGIDO: Extensión para conversión de Result a AppResult
fun <T> Result<T>.toAppResult(): AppResult<T> = when {
    isSuccess -> AppResult.Success(getOrThrow())
    else -> AppResult.Error(
        com.sponsorflow.nexus.core.result.AppError.fromException(exceptionOrNull() ?: Exception("Unknown error"))
    )
}

// CORREGIDO: Extensión para conversión de AppResult a Result
fun <T> AppResult<T>.toResult(): Result<T> = when (this) {
    is AppResult.Success -> Result.success(data)
    is AppResult.Error -> Result.failure(error.toException())
    is AppResult.Loading -> Result.failure(Exception("Loading state cannot be converted to Result"))
}

// CORREGIDO: Conversión de AppError a Exception
fun com.sponsorflow.nexus.core.result.AppError.toException(): Exception = when (this) {
    is com.sponsorflow.nexus.core.result.AppError.NetworkError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.TimeoutError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.DatabaseError -> cause
    is com.sponsorflow.nexus.core.result.AppError.AuthError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.PermissionError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.SecurityError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.ValidationError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.LicenseError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.PaymentError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.SubscriptionError -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.ResourceNotFound -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.ResourceExhausted -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.NotFound -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.InsufficientStock -> Exception(message)
    is com.sponsorflow.nexus.core.result.AppError.Unknown -> Exception("Unknown error")
    is com.sponsorflow.nexus.core.result.AppError.UnexpectedError -> cause ?: Exception("Unexpected error")
    is com.sponsorflow.nexus.core.result.AppError.ParseError -> Exception(message)
}
