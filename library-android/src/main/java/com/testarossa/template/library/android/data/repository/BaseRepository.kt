package com.testarossa.template.library.android.data.repository

import com.testarossa.template.library.android.data.model.ActionEvent
import com.testarossa.template.library.android.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Resource.Failure(throwable, null)
            }
        }
    }

    suspend fun <T> safeAction(
        action: suspend () -> T
    ): ActionEvent<T> {
        return try {
            ActionEvent.Success(action.invoke())
        } catch (throwable: Throwable) {
            ActionEvent.Failure(throwable)
        }
    }
}
