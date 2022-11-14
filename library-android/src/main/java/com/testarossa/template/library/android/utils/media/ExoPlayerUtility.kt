package com.hunglvv.renaissance.library.android.utils.media

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.FloatRange
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.testarossa.template.library.android.utils.media.AudioFocusUtility


/**

# How to use:

private lateinit var exoUtility: ExoPlayerUtility
exoPlayerUtility = ExoPlayerUtility(binding.playerView, requireContext()).apply {
viewLifecycleOwner.lifecycle.addObserver(this)
enableRepeat(true)
setMedia()
listener = object : ExoPlayerUtility.IExoPlayerCallback {
override fun getDurationMedia(duration: Long) {
}

override fun onPlaybackPositionChanged(position: Long) {

}

override fun onEndPlaying() {

}

override fun onLoadComplete() {

}

override fun onIsPlayingChanged(isPlaying: Boolean) {

}

override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {

}

}
}
# set media:
exoUtility.setMedia(MediaItem.fromUri(Uri.parse(event.path)))
# change state playing:
exoUtility.changeStatePlayer(event.playing)
 */


class ExoPlayerUtility(
    private val playerView: StyledPlayerView?,
    private val context: Context,
    private val runInBackground: Boolean = false,
    delay: Long = 1000
) : DefaultLifecycleObserver {

    // region Const and Fields
    interface IExoPlayerCallback {
        fun getDurationMedia(duration: Long) {}
        fun onPlaybackPositionChanged(position: Long) {}
        fun onLoadComplete() {}
        fun onEndPlaying() {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onPlayerError(error: PlaybackException) {}
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val audioFocusListener: AudioFocusUtility.MediaControlListener = audioFocusListener()
    private val audioHelper: AudioFocusUtility by lazy {
        AudioFocusUtility(context, audioFocusListener)
    }
    var listener: IExoPlayerCallback? = null
    private var mediaItem: MediaItem? = null
    private var mediaSource: BaseMediaSource? = null
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var startItemIndex = C.INDEX_UNSET
    private var playbackPosition = 0L
    private var enableRepeat = false
    private var volume = 1f
    private var stopped = false

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (player != null) {
                val current = player!!.currentPosition
                listener?.onPlaybackPositionChanged(current)
                handler.postDelayed(this, delay)
            }
        }
    }

    // endregion

    // region controller
    fun getPlayer() = player

    fun setSpeed(@FloatRange(from = 0.0, fromInclusive = false) speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun enableRepeat(enable: Boolean) {
        enableRepeat = enable
        player?.repeatMode = if (!enable) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
    }

    fun toggleRepeat() {
        enableRepeat = !enableRepeat
        player?.repeatMode = if (!enableRepeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
    }

    fun setMedia(media: MediaItem, simpleCache: SimpleCache? = null) {
        mediaItem = media
        player?.let {
            if (simpleCache != null) {
                mediaSource = ProgressiveMediaSource.Factory(
                    CacheDataSource.Factory()
                        .setCache(simpleCache)
                        .setUpstreamDataSourceFactory(
                            DefaultHttpDataSource.Factory()
                                .setAllowCrossProtocolRedirects(true)
                        )
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                ).createMediaSource(media)
                it.setMediaSource(mediaSource!!, true)
            } else {
                it.setMediaItem(mediaItem!!, true)
            }
            it.prepare()
        }
    }

    fun changeStatePlayer(playing: Boolean) {
        if (playing) {
            audioHelper.tryPlayback()
        } else {
            player?.playWhenReady = false
        }
    }

    /**
     * Play/Pause media
     */
    fun onPlayPauseMedia() {
        player?.let {
            if (it.playbackState == ExoPlayer.STATE_ENDED) {
                playbackPosition = 0L
                player?.seekTo(playbackPosition)
            }
            if (!it.isPlaying) {
                audioHelper.tryPlayback()
            } else {
                it.playWhenReady = false
            }
        }
    }

    /**
     * Seek media to miliseconds
     */
    fun seekTo(time: Long) {
        playbackPosition = time
        player?.seekTo(playbackPosition)
    }

    fun changeVolume(volume: Float) {
        this.volume = volume
        player?.volume = volume
    }
    // endregion

    // region lifecycle methods
    override fun onResume(owner: LifecycleOwner) {
        if ((Util.SDK_INT <= 23 || player == null) && !runInBackground) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (Util.SDK_INT > 23 && !runInBackground) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (Util.SDK_INT <= 23 && !runInBackground) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (Util.SDK_INT > 23 && !runInBackground) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        if (runInBackground) {
            initializePlayer()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (runInBackground) {
            releasePlayer()
        }
    }
    // endregion

    // region private methods
    private fun initializePlayer() {
        player = ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                playerView?.player = exoPlayer
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.repeatMode =
                    if (!enableRepeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
                exoPlayer.volume = volume
                val haveStartPosition = startItemIndex != C.INDEX_UNSET
                if (haveStartPosition) {
                    exoPlayer.seekTo(startItemIndex, playbackPosition)
                }
                if (mediaSource != null) {
                    exoPlayer.setMediaSource(mediaSource!!, !haveStartPosition)
                    exoPlayer.prepareSource()
                } else if (mediaItem != null) {
                    exoPlayer.setMediaItem(mediaItem!!)
                    exoPlayer.prepareSource()
                }
            }
        handler.post(mRunnable)
    }

    private fun ExoPlayer.prepareSource() {
        if (playWhenReady) {
            audioHelper.tryPlayback()
        }
        prepare()
    }


    /**
     * Release media player
     */
    private fun releasePlayer() {
        audioHelper.finishPlayback()
        handler.removeCallbacks(mRunnable)
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            startItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY) {
                listener?.getDurationMedia(player?.duration ?: 0L)
                listener?.onLoadComplete()
            } else if (playbackState == ExoPlayer.STATE_ENDED) {
                listener?.onEndPlaying()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            listener?.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            listener?.onPlayerError(error)
        }
    }

    private fun audioFocusListener() = object : AudioFocusUtility.MediaControlListener {
        override fun onPlayMedia() {
            if (stopped) {
                stopped = false
                player?.prepare()
            } else {
                player?.playWhenReady = true
            }
        }

        override fun onPauseMedia() {
            player?.playWhenReady = false
        }

        override fun onStopMedia() {
            stopped = true
            player?.stop()
        }
    }
    // endregion
}
