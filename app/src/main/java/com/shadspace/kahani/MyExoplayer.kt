package com.shadspace.kahani

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.shadspace.kahani.models.AudioModel

object MyExoplayer {

    private var exoPlayer : ExoPlayer? = null
    private var currentSong : AudioModel? =null

    fun getCurrentSong() : AudioModel?{
        return currentSong
    }

    fun getInstance() : ExoPlayer?{
        return exoPlayer
    }

    fun startPlaying(context : Context, song : AudioModel){
        if(exoPlayer==null)
            exoPlayer = ExoPlayer.Builder(context).build()

        if(currentSong!=song){
            //Its a new song so start playing
            currentSong = song

            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()

            }
        }


    }

}