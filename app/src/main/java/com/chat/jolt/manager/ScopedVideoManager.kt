package com.chat.jolt.manager

import android.content.ContextWrapper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.chat.jolt.widget.CustomVideoPlay
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.util.getRealUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ScopedVideoManager : DefaultLifecycleObserver {

    interface OnVideoStatusListener {
        fun onReady(duration: Int)
        fun onProgressUpdate(progress: Int)
        fun onVideoPause()
        fun onVideoPlay()
        fun onPlayEnd()
        fun detachLastPlayer()
        fun onBuffer()
    }

    private var exoPlayer: ExoPlayer? = null

    private var listener: OnVideoStatusListener? = null

    private var mCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var updateJob: Job? = null


    fun play(customView: CustomVideoPlay, url: String) {

        release()

        setOnVideoStatusListener(customView.mOnVideoStatusListener)

        customView.onClick = {

            if (isPlaying()) {

                pause()
            } else {
                play()
            }

        }
        customView.onSeek = {

            seekTo(it)

        }

        val context = (customView.context as? ContextWrapper)?.baseContext ?: customView.context


        exoPlayer = ExoPlayer.Builder(context)
            .build().also { player ->

                customView.attachPlayer(player)

                player.repeatMode = Player.REPEAT_MODE_ALL

                val proxy = BaseApplication.mApplication.getProxy()

                val proxyUrl = proxy.getProxyUrl(getRealUrl(url).toString())

                player.setMediaItem(MediaItem.fromUri(proxyUrl))
                player.prepare()
                player.playWhenReady = true

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                listener?.onReady(player.duration.toInt() / 1000)
                            }

                            Player.STATE_BUFFERING -> {
                                listener?.onBuffer()
                            }

                            Player.STATE_ENDED -> {
                                listener?.onPlayEnd()
                            }

                            Player.STATE_IDLE -> {
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            startProgressUpdates()
                        } else {
                            stopProgressUpdates()
                        }
                    }
                })
            }
    }

    fun setOnVideoStatusListener(listener: OnVideoStatusListener) {
        this.listener = listener
    }

    private fun startProgressUpdates() {
        updateJob?.cancel()
        updateJob = mCoroutineScope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    val current = (player.currentPosition / 1000.0).roundToInt()
                    listener?.onProgressUpdate(current)
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        updateJob?.cancel()
    }

    fun pause() {
        exoPlayer?.pause()
        listener?.onVideoPause()
    }

    fun play() {
        exoPlayer?.play()
        listener?.onVideoPlay()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun getDuration(): Long {

        return exoPlayer?.duration ?: 0L
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun release() {
        listener?.detachLastPlayer()
        stopProgressUpdates()
        exoPlayer?.release()
        exoPlayer = null
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        release()
    }
}