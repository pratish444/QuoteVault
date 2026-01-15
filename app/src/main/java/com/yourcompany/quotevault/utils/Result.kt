package com.yourcompany.quotevault.utils


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success<T> -> data
        else -> null
    }
}