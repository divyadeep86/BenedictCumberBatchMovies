package com.vh.benedictcumberbatchmovies.domain.common

/**
 * Wrapper for emitting result or error (and optional loading).
 */
sealed class DataState<out T> {
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val cause: Throwable? = null
    ) : DataState<Nothing>()

    object Loading : DataState<Nothing>()
}