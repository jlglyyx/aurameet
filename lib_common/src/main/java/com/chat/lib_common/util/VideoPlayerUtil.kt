package com.chat.lib_common.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.apply
import kotlin.let


object VideoPlayerUtil {

    private const val TAG = "VideoPlayerUtil"

    private var mExoPlayer: ExoPlayer? = null


    @SuppressLint("StaticFieldLeak")
    private var mPlayerView: PlayerView? = null


    var onInit: (() -> Unit)? = null
    var onReady: (() -> Unit)? = null

    @OptIn(UnstableApi::class)
    fun init(context: Context): ExoPlayer? {
        if (mExoPlayer == null) {

            mExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

            mExoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
        } else {

            return mExoPlayer
        }



        mExoPlayer?.let {


            it.addListener(object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {

                    } else {

                    }

                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {

                            onReady?.invoke()


                        }

                        Player.STATE_BUFFERING -> {

                            Log.i(TAG, "onPlaybackStateChanged: 缓冲中...")


                        }

                        Player.STATE_ENDED -> {

                            Log.i(TAG, "onPlaybackStateChanged: ")


                        }

                        Player.STATE_IDLE -> {


                            Log.i(TAG, "onPlaybackStateChanged: ")
                        }
                    }

                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)



                }

            })

        }


        return mExoPlayer
    }


    fun play(url: String, targetView: PlayerView) {


//        if (mPlayerView == targetView) return

        if (null == mExoPlayer) return

        mPlayerView?.player = null

        releaseCurrent()

        mPlayerView = targetView


        targetView.player = mExoPlayer


        mExoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }

    }


    fun seekTo(positionMs: Long) {

        mExoPlayer?.let {

            if (it.playbackState != Player.STATE_IDLE) {

                it.seekTo(positionMs)

            }

        }

    }

    fun currentDuration(): Long {

        return mExoPlayer?.duration ?: 0L

    }


    fun isPlay(): Boolean {

        return mExoPlayer?.isPlaying ?: false
    }
    fun isRelease(): Boolean {

        return mExoPlayer == null
    }


    fun pause() {

        mExoPlayer?.pause()

    }

    fun play() {

        mExoPlayer?.play()

    }

    fun setVolume(volume: Float){

        mExoPlayer?.volume = volume
    }

    fun releaseCurrent() {
        mExoPlayer?.stop()
        mExoPlayer?.clearMediaItems()
        mPlayerView = null
    }

    fun releaseAll() {
        releaseCurrent()
        mPlayerView = null
        mExoPlayer?.release()
        mExoPlayer = null
        onInit = null
        onReady = null
    }


}