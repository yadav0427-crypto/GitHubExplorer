package com.example.core.common

sealed interface Resource<out T> {
    data class Success<out T>(val data: T, val lastUpdated: Long? = null) : Resource<T>
    data class SuccessOffline<out T>(val data: T, val lastUpdated: Long? = null) : Resource<T>
    data class Error(val message: String, val code: Int? = null, val throwable: Throwable? = null) : Resource<Nothing>
}
