package com.testarossa.template.library.android.utils.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.google.android.exoplayer2.util.Util
import java.io.IOException

open class MediaPlayerUtility(
    private val context: Context,
    private val mediaListener: IMediaPlayerListener
) :
    AudioFocusUtility.MediaControlListener {
    // region Const and Fields
    private val audioFocusUtility by lazy {
        AudioFocusUtility(context, this)
    }

    private var player: MediaPlayer? = null
    private var speed = 1.0f
    private var fromAssets = false
    private var mediaUrl = ""
    private var typeUri = false
    private var isMediaOpened = false
    private var playbackPosition = 0
    private var playWhenReady = false
    private var enableRepeat = false
    private var delay = DEFAULT_DELAY_INTERVAL

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (player != null) {
                val current = player!!.currentPosition
                mediaListener.onPlaybackPositionChanged(current.toLong())
                handler.postDelayed(this, delay)
            }
        }
    }
    // endregion

    // region controller
    open fun getPlayer() = player

    open fun setSpeed(speed: Float) {
        this.speed = speed
        if (player != null) {
            playWhenReady = player!!.isPlaying
            onStopMedia()
            player?.prepareSource()
        }
    }

    open fun setDelayHandlePosition(time: Long) {
        delay = time
    }

    open fun enableRepeat(enable: Boolean) {
        enableRepeat = enable
    }

    open fun setPlayFromAssets(fromAssets: Boolean) {
        this.fromAssets = fromAssets
    }

    open fun setTypeUri(isUri: Boolean) {
        typeUri = isUri
    }

    open fun setMedia(mediaUrl: String, playWhenReady: Boolean = false) {
        this.mediaUrl = mediaUrl
        this.playWhenReady = playWhenReady
        if (player == null) {
            initializePlayer()
        } else {
            onStopMedia()
            player?.prepareSource()
        }
    }

    open fun changeStatePlayer(playing: Boolean) {
        if (playing) {
            if (player?.isPlaying == true && isMediaOpened) {
                player?.pause()
            }
            audioFocusUtility.tryPlayback()
        } else {
            onPauseMedia()
            seekTo(0)
        }
    }

    open fun onPlayPauseMedia() {
        player?.let {
            if (!it.isPlaying) {
                audioFocusUtility.tryPlayback()
            } else {
                onPauseMedia()
            }
        }
    }

    open fun seekTo(time: Int) {
        player?.seekTo(time)
    }
    // endregion

    // region lifecycle methods
    open fun onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    open fun onResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    open fun onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    open fun onStop() {
        if (Util.SDK_INT > 23) {
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

    // region override
    override fun onPlayMedia() {
        player?.apply {
            if (isMediaOpened) {
                start()
            } else {
                playWhenReady = true
                prepareSource()
            }
        }

        mediaListener.onIsPlayingChanged(true)
    }

    override fun onPauseMedia() {
        if (isMediaOpened) {
            player?.pause()
        }
        mediaListener.onIsPlayingChanged(false)
    }

    override fun onStopMedia() {
        player?.apply {
            if (isMediaOpened || isPlaying) {
                stop()
            }
            audioFocusUtility.finishPlayback()
            isMediaOpened = false
            reset()
            mediaListener.onPlaybackStateChanged(PlaybackState.IDLE)
        }
        mediaListener.onIsPlayingChanged(false)
    }
    // endregion

    // region private methods
    private fun initializePlayer() {
        mediaListener.onPlaybackStateChanged(PlaybackState.IDLE)
        isMediaOpened = false
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnPreparedListener {
                it.seekTo(playbackPosition)

                isMediaOpened = true
                if (playWhenReady) {
                    playWhenReady = false
                    val params = it.playbackParams
                    params.speed = speed
                    it.playbackParams = params
                    audioFocusUtility.tryPlayback()
                }
                mediaListener.getDurationMedia(it.duration.toLong())
                mediaListener.onPlaybackStateChanged(PlaybackState.READY)
            }
            setOnCompletionListener {
                if (enableRepeat) {
                    it.start()
                } else {
                    mediaListener.onIsPlayingChanged(false)
                }
                mediaListener.onPlaybackStateChanged(PlaybackState.ENDED)
            }
            setOnErrorListener { mp, w, c ->
                isMediaOpened = false
                mp.reset()
                mediaListener.onPlayerError(w, c)
                true
            }
            prepareSource()
        }
        handler.post(mRunnable)
    }


    /**
     * Release media player
     */
    private fun releasePlayer() {
        handler.removeCallbacks(mRunnable)
        player?.apply {
            playWhenReady = isPlaying
            playbackPosition = currentPosition
            if (isMediaOpened) {
                stop()
            }
            audioFocusUtility.finishPlayback()
            reset()
            release()
        }
        player = null
    }

    private fun MediaPlayer.prepareSource() {
        if (mediaUrl.isEmpty()) return
        if (!fromAssets) {
            if (!typeUri) {
                setDataSource(mediaUrl)
            } else {
                setDataSource(context, mediaUrl.toUri())
            }
            prepareAsync()
        } else {
            try {
                val afd = context.assets?.openFd(mediaUrl) ?: return
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepareAsync()
            } catch (e: IOException) {
            }
        }
    }
    // endregion

    interface IMediaPlayerListener {
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onPlayerError(
            typeError: Int,
            code: Int
        ) // https://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener

        fun getDurationMedia(duration: Long)
        fun onPlaybackPositionChanged(position: Long)
        fun onPlaybackStateChanged(playbackState: PlaybackState)
    }


    enum class PlaybackState {
        IDLE, READY, ENDED
    }

    companion object {
        private const val DEFAULT_DELAY_INTERVAL = 200L
    }
}
