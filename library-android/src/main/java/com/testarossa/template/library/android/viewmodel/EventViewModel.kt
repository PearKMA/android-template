package com.testarossa.template.library.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class EventViewModel<Event : Any> : ViewModel() {
    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

}

class EventAndroidViewModel<Event : Any>(application: Application) : AndroidViewModel(application) {
    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

}
