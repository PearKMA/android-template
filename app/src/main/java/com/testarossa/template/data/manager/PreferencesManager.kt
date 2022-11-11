package com.testarossa.template.data.manager

import android.content.Context
import com.testarossa.template.library.android.data.manager.BaseDataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) :
    BaseDataStoreManager(context) {

//    val showIntroFlow = PreferencesKeys.FIRST_USE_KEY.watchValue(true)
//
//    suspend fun disableShowIntro() {
//        PreferencesKeys.FIRST_USE_KEY.setValue(false)
//    }
//
//
//    private object PreferencesKeys {
//        val FIRST_USE_KEY = booleanPreferencesKey("first_use")
//    }
}