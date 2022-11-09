package com.testarossa.template.library.compose.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.testarossa.template.library.android.utils.media.AudioFocusUtility
import com.testarossa.template.library.compose.data.model.VideoPlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@Composable
fun VideoPlayer(
    modifier: Modifier,
    uri: Uri,
    loop: Boolean = true,
    volume: Float = 1.0f,
    simpleCache: SimpleCache? = null,
    contentController: @Composable (playerState: VideoPlayerState, modifier: Modifier, togglePlayPause: () -> Unit, seekTo: (Long) -> Unit) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                if (simpleCache != null) {
                    val mediaSource = ProgressiveMediaSource.Factory(
                        CacheDataSource.Factory()
                            .setCache(simpleCache)
                            .setUpstreamDataSourceFactory(
                                DefaultHttpDataSource.Factory()
                                    .setAllowCrossProtocolRedirects(true)
                            )
                            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    ).createMediaSource(MediaItem.fromUri(uri))
                    exoPlayer.setMediaSource(mediaSource, true)
                } else {
                    exoPlayer.setMediaItem(MediaItem.fromUri(uri))
                }
                exoPlayer.repeatMode = if (!loop) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
                exoPlayer.volume = volume
                exoPlayer.prepare()
            }
    }

    val scope = rememberCoroutineScope()
    var job: Job? by remember {
        mutableStateOf(null)
    }

    var playerState by remember {
        mutableStateOf(VideoPlayerState())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val playbackStateListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    if (playerState.duration == 0L) {
                        playerState =
                            playerState.copy(duration = exoPlayer.duration.coerceAtLeast(0))
                    }
                    job?.cancel()
                    job = scope.launch {
                        while (this.isActive) {
                            playerState = playerState.copy(
                                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
                            )
                            delay(250)
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerState = playerState.copy(isPlaying = isPlaying)
            }

        }
        exoPlayer.addListener(playbackStateListener)
        val manageAudio =
            AudioFocusUtility(context, object : AudioFocusUtility.MediaControlListener {
                override fun onPlayMedia() {
                    exoPlayer.playWhenReady = true
                }

                override fun onPauseMedia() {
                    exoPlayer.playWhenReady = false
                }

                override fun onStopMedia() {
                    exoPlayer.stop()
                }
            })
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (playerState.playingState) {
                        manageAudio.tryPlayback()
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    playerState = playerState.copy(playingState = exoPlayer.isPlaying)
                    manageAudio.finishPlayback()
                    exoPlayer.playWhenReady = false
                }

                else -> {}
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)


        // When the effect leaves the Composition, remove the observer
        onDispose {
            manageAudio.finishPlayback()
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Implementing ExoPlayer
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = modifier,
            factory = {
                StyledPlayerView(context).apply {
                    useController = false
                    player = exoPlayer
                }
            })
        if (playerState.duration > 0L) {
            contentController(
                playerState,
                modifier = Modifier.align(Alignment.BottomCenter),
                togglePlayPause = {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                },
                seekTo = {
                    exoPlayer.seekTo(it)
                })
        }
    }
}