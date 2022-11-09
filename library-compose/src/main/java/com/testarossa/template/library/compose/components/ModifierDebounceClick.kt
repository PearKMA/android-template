package com.testarossa.template.library.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

inline fun Modifier.debounceClickable(
    debounceInterval: Long = 400,
    crossinline onClick: () -> Unit,
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    clickable(interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = false), onClick = {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastClickTime) < debounceInterval) return@clickable
            lastClickTime = currentTime
            onClick()
        })
}

inline fun Modifier.debounceAlphaClickable(
    debounceInterval: Long = 400,
    crossinline onClick: () -> Unit,
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    clickable(interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(
            color =
            Color.White.copy(alpha = 0.5f)
        ), onClick = {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastClickTime) < debounceInterval) return@clickable
            lastClickTime = currentTime
            onClick()
        })
}
