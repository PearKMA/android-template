package com.testarossa.template.library.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EventViewModel<Event : Any> : ViewModel() {
    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()
    protected fun send(event: Event, task: () -> Unit = {}) {
        viewModelScope.launch {
            task()
            eventsChannel.send(event)
        }
    }
}

class EventAndroidViewModel<Event : Any>(application: Application) : AndroidViewModel(application) {
    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()
    protected fun send(event: Event, task: () -> Unit = {}) {
        viewModelScope.launch {
            task()
            eventsChannel.send(event)
        }
    }
}
