package com.testarossa.template.library.android.data.model

sealed class ActionEvent<out T> {
    object Loading : ActionEvent<Nothing>()
    data class Success<out T>(val value: T) : ActionEvent<T>()
    data class Failure(val throwable: Throwable?) : ActionEvent<Nothing>()
    object None : ActionEvent<Nothing>()
}


sealed class Resource<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Failure<T>(
        throwable: Throwable,
        data: T? = null
    ) : Resource<T>(data, throwable)

    class Loading<T>(data: T? = null) : Resource<T>(data)
}
