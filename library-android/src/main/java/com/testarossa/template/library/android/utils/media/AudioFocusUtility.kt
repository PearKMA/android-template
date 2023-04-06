package com.testarossa.template.library.android.utils.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import java.util.concurrent.TimeUnit

class AudioFocusUtility(context: Context, private val listener: MediaControlListener) {
    // region Const and Fields
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var isRequested = false
    private val focusLock = Any()
    private var playbackDelayed = false
    private var resumeOnFocusGain = false
    private var playbackNowAuthorized = false

    private val handler = Handler(Looper.getMainLooper())
    private var delayedStopRunnable = Runnable {
        listener.onStopMedia()
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                listener.onPauseMedia()
                // Wait 30 seconds before stopping playback
                handler.postDelayed(delayedStopRunnable, TimeUnit.SECONDS.toMillis(30))
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback
                synchronized(focusLock) {
                    // only resume if playback is being interrupted
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                listener.onPauseMedia()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    handler.removeCallbacks(delayedStopRunnable)
                    listener.onPlayMedia()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var focusRequest: AudioFocusRequest? = null

    // endregion

    // region interactive
    fun tryPlayback() {
        if (isRequested) {
            handler.removeCallbacks(delayedStopRunnable)
            listener.onPlayMedia()
            return
        }
        if (isBuildLargerThan(Build.VERSION_CODES.O)) {
            // requesting audio focus and processing the response
            if (null == focusRequest) {
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(afChangeListener, handler)
                    build()
                }
            }
            val res = audioManager.requestAudioFocus(focusRequest!!)
            synchronized(focusLock) {
                playbackNowAuthorized = when (res) {
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        handler.removeCallbacks(delayedStopRunnable)
                        listener.onPlayMedia()
                        true
                    }

                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }

                    else -> false
                }
            }
        } else {
            // Request audio focus for playback
            @Suppress("DEPRECATION")
            val result: Int = audioManager.requestAudioFocus(
                afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN
            )

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                handler.removeCallbacks(delayedStopRunnable)
                listener.onPlayMedia()
            }
        }
        isRequested = true
    }

    fun finishPlayback() {
        isRequested = false
        if (isBuildLargerThan(Build.VERSION_CODES.O)) {
            if (null != focusRequest) {
                audioManager.abandonAudioFocusRequest(focusRequest!!)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(afChangeListener)
        }
    }
    // endregion

    interface MediaControlListener {
        fun onPlayMedia()
        fun onPauseMedia()
        fun onStopMedia()
    }
}
