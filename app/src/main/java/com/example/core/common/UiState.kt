package com.example.core.common

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T, val isOffline: Boolean = false, val lastUpdated: Long? = null) : UiState<T>
    data class Error(val message: String, val code: Int? = null, val throwable: Throwable? = null) : UiState<Nothing>
}
