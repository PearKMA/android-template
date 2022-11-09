package com.testarossa.template.library.android.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) {
            return
        }
    }
}

// require lifecycle runtime
fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch {
        delay(firstTimeDelay)
        flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .collect(action)
    }
}

fun <T> AppCompatActivity.collectWhenStarted(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch {
        delay(firstTimeDelay)
        flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .collect(action)
    }
}

fun Fragment.collectWhenStarted(
    firstTimeDelay: Long = 0L,
    action: (scope: CoroutineScope) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        delay(firstTimeDelay)
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            action(this)
        }
    }
}

fun AppCompatActivity.collectWhenStarted(
    firstTimeDelay: Long = 0L,
    action: (scope: CoroutineScope) -> Unit
) {
    lifecycleScope.launch {
        delay(firstTimeDelay)
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            action(this)
        }
    }
}

fun <T> ViewModel.send(channel: Channel<T>, value: T) {
    viewModelScope.launch {
        channel.send(value)
    }
}

/**
 * Returns a flow which performs the given [action] on each value of the original flow's [Event].
 */
fun <T> Flow<Event<T?>>.onEachEvent(action: suspend (T) -> Unit): Flow<T> = transform { value ->
    value.getContentIfNotHandled()?.let {
        action(it)
        return@transform emit(it)
    }
}

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
