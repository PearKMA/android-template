package com.testarossa.template.library.android.utils.media

import android.content.Context
import android.os.Handler
import android.os.Looper
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

/**
 *
 * How to use:
 *
 * init constructor
 * implement interface IExoPlayerCallback
 * -> onAudioLoss:  onloss -> pauseVideo() + set image pause
 *                  else -> resumeVideo() + set image play if resumeVideo() == true else pause
 * -> onCurrentTime: (text current time).text = time + (seekbar time).progress = progress
 *
 * (seekbar time).max = this.duration!!.toInt()
 * (text duration).text = formatTime(this.duration!!)
 *
 * button play/pause: if(playPauseVideo()){ set image play} else { set image pause}
 * seek: seekTo(time)
 * onResume:    startHandler() + check resumeVideo()
 * onPause:     stopHandler() + pauseVideo() + set image pause
 * onstart:     initializePlayer(url)
 * onstop:      killPlayer()
 */

open class ExoPlayerUtility(
    private val playerView: StyledPlayerView?,
    private val context: Context,
    delay: Long = 1000
) {
    // region Const and Fields

    interface IExoPlayerCallback {
        fun getDurationMedia(duration: Long)
        fun onPlaybackPositionChanged(position: Long)
        fun onPlaybackStateChanged(playbackState: Int)
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onPlayerError(error: PlaybackException)
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
    private var currentItem = 0
    private var playbackPosition = 0L
    private var enableRepeat = false
    private var volume = 1f

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
    open fun getPlayer() = player

    open fun setSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    open fun enableRepeat(enable: Boolean) {
        enableRepeat = enable
        player?.repeatMode = if (!enable) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
    }

    open fun setMedia(media: MediaItem, simpleCache: SimpleCache? = null) {
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

    open fun changeStatePlayer(playing: Boolean) {
        if (playing) {
            audioHelper.tryPlayback()
        } else {
            player?.playWhenReady = false
        }
    }

    /**
     * Play/Pause media
     */
    open fun onPlayPauseMedia() {
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
    open fun seekTo(time: Long) {
        playbackPosition = time
        player?.seekTo(playbackPosition)
    }

    open fun changeVolume(volume: Float) {
        this.volume = volume
        player?.volume = volume
    }

    open fun onRestoreStatePlayWhenReady() {
        player?.playWhenReady = playWhenReady
    }

    open fun onSavedStatePlayWhenReady() {
        playWhenReady = player?.playWhenReady ?: playWhenReady
        player?.playWhenReady = false
    }
    // endregion

    // region lifecycle methods
    open fun onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    open fun onResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    open fun onPause() {
        if (Util.SDK_INT <= 23) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    open fun onStop() {
        if (Util.SDK_INT > 23) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    open fun onCreate() {
        initializePlayer()
    }

    open fun onDestroy() {
        releasePlayer()
    }
    // endregion

    // region private methods
    private fun initializePlayer() {
        player = ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                playerView?.player = exoPlayer
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.repeatMode =
                    if (!enableRepeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
                exoPlayer.volume = volume
                exoPlayer.seekTo(currentItem, playbackPosition)

                if (mediaSource != null) {
                    exoPlayer.setMediaSource(mediaSource!!)
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
            currentItem = exoPlayer.currentMediaItemIndex
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
            }
            listener?.onPlaybackStateChanged(playbackState)
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
            player?.playWhenReady = true
        }

        override fun onPauseMedia() {
            player?.playWhenReady = false
        }

        override fun onStopMedia() {
            player?.playWhenReady = false
        }
    }
    // endregion
}
