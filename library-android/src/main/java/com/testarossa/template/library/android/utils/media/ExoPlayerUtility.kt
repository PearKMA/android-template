package com.testarossa.template.library.android.utils.media

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
import com.google.android.exoplayer2.video.VideoSize


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
    private val runInBackground: Boolean = false
) : DefaultLifecycleObserver {

    // region Const and Fields
    interface IExoPlayerCallback {
        fun onPlaybackPositionChanged(position: Long) {}
        fun onLoadComplete(duration: Long) {}
        fun onEndPlaying() {}
        fun onLoading() {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onPlayerError(error: PlaybackException) {}
        fun onMediaSizeChange(videoSize: VideoSize) {}
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val audioFocusListener: AudioFocusUtility.MediaControlListener = audioFocusListener()
    private val audioFocusUtility: AudioFocusUtility by lazy {
        AudioFocusUtility(context, audioFocusListener)
    }
    var listener: IExoPlayerCallback? = null
    var repeat = false
    var volume = 1f

    private var mediaItem: MediaItem? = null
    private var mediaSource: BaseMediaSource? = null
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var startItemIndex = C.INDEX_UNSET
    private var playbackPosition = 0L

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (player != null) {
                val current = player!!.currentPosition
                listener?.onPlaybackPositionChanged(current)
                handler.postDelayed(this, 200L)
            }
        }
    }

    // endregion

    // region controller
    fun getPlayer() = player

    fun setSpeed(@FloatRange(from = 0.0, fromInclusive = false) speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun toggleRepeat() {
        repeat = !repeat
        player?.repeatMode = if (!repeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
    }

    fun setMedia(media: MediaItem, playNow: Boolean = true, simpleCache: SimpleCache? = null) {
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
            player?.setMediaSource(mediaSource!!, true)
        } else {
            mediaItem = media
            player?.setMediaItem(media, true)
        }
        this.playWhenReady = playNow
        player?.prepareSource()
    }


    fun changeStatePlaying(playing: Boolean) {
        playWhenReady = playing
        if (playing) {
            audioFocusUtility.tryPlayback()
        } else {
            player?.playWhenReady = false
        }
    }

    /**
     * Play/Pause media
     */
    fun toggleStatePlaying() {
        playWhenReady = !playWhenReady
        player?.let {
            if (!it.isPlaying) {
                audioFocusUtility.tryPlayback()
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

    fun startSeekTo() {
        playWhenReady = if (player?.playbackState == ExoPlayer.STATE_ENDED) {
            false
        } else {
            player?.playWhenReady ?: playWhenReady
        }
        player?.playWhenReady = false
    }

    fun stopSeekTo() {
        player?.playWhenReady = playWhenReady
    }

    fun changeVolume(@FloatRange(from = 0.0, to = 1.0) volume: Float) {
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
                exoPlayer.repeatMode =
                    if (!repeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
                exoPlayer.volume = volume
                val haveStartPosition = startItemIndex != C.INDEX_UNSET
                if (haveStartPosition) {
                    exoPlayer.seekTo(startItemIndex, playbackPosition)
                }
                if (mediaSource != null) {
                    exoPlayer.setMediaSource(mediaSource!!, !haveStartPosition)
                    exoPlayer.prepareSource()
                } else if (mediaItem != null) {
                    exoPlayer.setMediaItem(mediaItem!!, !haveStartPosition)
                    exoPlayer.prepareSource()
                }
            }
        handler.post(mRunnable)
    }

    private fun ExoPlayer.prepareSource() {
        prepare()
        if (playWhenReady) {
            audioFocusUtility.tryPlayback()
        }
    }


    /**
     * Release media player
     */
    private fun releasePlayer() {
        audioFocusUtility.finishPlayback()
        handler.removeCallbacks(mRunnable)
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            startItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = false//exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
        listener?.onIsPlayingChanged(false)
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            listener?.onMediaSizeChange(videoSize)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_READY -> {
                    listener?.onLoadComplete(player?.duration ?: 0L)
                }

                ExoPlayer.STATE_ENDED -> {
                    listener?.onEndPlaying()
                }

                ExoPlayer.STATE_BUFFERING -> {
                    listener?.onLoading()
                }

                Player.STATE_IDLE -> {

                }
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
            if (player?.playbackState == ExoPlayer.STATE_IDLE) {
                player?.prepare()
            } else if (player?.playbackState == ExoPlayer.STATE_ENDED) {
                playbackPosition = 0L
                player?.seekTo(playbackPosition)
            }
            playWhenReady = true
            player?.playWhenReady = true
        }

        override fun onPauseMedia() {
            playWhenReady = true
            player?.playWhenReady = false
        }

        override fun onStopMedia() {
            player?.stop()
        }
    }
    // endregion
}
