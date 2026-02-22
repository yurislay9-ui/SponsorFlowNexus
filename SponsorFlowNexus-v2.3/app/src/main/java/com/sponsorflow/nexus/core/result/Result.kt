/*
 * SponsorFlow Nexus v2.3 - Result Type
 * Skill: Mejores prácticas - Sealed class para estados
 */
package com.sponsorflow.nexus.core.result

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError, val message: String? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError, String?) -> Unit): AppResult<T> {
        if (this is Error) action(error, message)
        return this
    }

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(error, message)
        is Loading -> Loading
    }

    inline fun <R> flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(error, message)
        is Loading -> Loading
    }

    fun getOrNull(): T? = if (this is Success) data else null

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}