package com.testarossa.template.library.android.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.testarossa.template.library.android.utils.extension.send
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseMediaStoreViewModel<Event : Any>(application: Application) :
    AndroidViewModel(application) {
    //region Const and Fields
    private var contentObserver: ContentObserver? = null

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()
    //endregion

    //region abstract methods
    abstract suspend fun actionFetchData()
    //endregion

    //region open methods
    protected fun fetchData() {
        viewModelScope.launch {
            actionFetchData()
            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    getUriStore()
                ) {
                    fetchData()
                }
            }
        }
    }

    protected fun sendEvent(event: Event) {
        send(eventsChannel, event)
    }

    protected fun getUriStore(): Uri = MediaStore.Files.getContentUri("external")
    //endregion

    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }
}

/**
 * Convenience extension method to register a [ContentObserver] given a lambda.
 */
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}
