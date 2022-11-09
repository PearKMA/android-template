package com.testarossa.template.library.compose.data.model

data class VideoPlayerState(
    var isPlaying: Boolean = true,
    var duration: Long = 0L,
    var currentPosition: Long = 0L,
    var playingState: Boolean = true
)
