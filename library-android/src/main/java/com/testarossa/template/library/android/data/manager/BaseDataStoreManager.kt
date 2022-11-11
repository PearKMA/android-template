package com.testarossa.template.library.android.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

abstract class BaseDataStoreManager(
    context: Context,
    nameDataStore: String = "user_preferences"
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(nameDataStore)
    private val mContext = context

    // region helper function
    suspend fun <T> Preferences.Key<T>.setValue(value: T) {
        mContext.dataStore.edit { preferences -> preferences[this] = value }
    }

    fun <T> Preferences.Key<T>.watchValue(defaultValue: T): Flow<T> {
        return mContext.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] ?: defaultValue }
    }

    fun <T> Preferences.Key<T>.watchValue(): Flow<T?> {
        return mContext.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] }
    }

    private fun Flow<Preferences>.catchAndHandleError(): Flow<Preferences> {
        this.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        return this@catchAndHandleError
    }
    // endregion
}
