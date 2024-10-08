package com.shadspace.kahani

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.FirebaseFirestore
import com.shadspace.kahani.models.AudioModel

object MyExoplayer {

    private var exoPlayer: ExoPlayer? = null
    private var currentSong: AudioModel? = null

    fun getCurrentSong(): AudioModel? {
        return currentSong
    }

    fun getInstance(): ExoPlayer? {
        return exoPlayer
    }

    fun startPlaying(context: Context, song: AudioModel) {
        if (exoPlayer == null)
            exoPlayer = ExoPlayer.Builder(context).build()

        if (currentSong != song) {
            //Its a new song so start playing
            currentSong = song

            updateCount()

            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()

            }
        }


    }

    fun updateCount() {
        currentSong?.id?.let { id ->
            FirebaseFirestore.getInstance().collection("audio")
                .document(id)
                .get().addOnSuccessListener {
                    var latestCount = it.getLong("count")
                    if (latestCount == null) {
                        latestCount = 1L
                    } else {
                        latestCount = latestCount + 1
                    }

                    FirebaseFirestore.getInstance().collection("audio")
                        .document(id)
                        .update(mapOf("count" to latestCount))

                }
        }
    }

    fun stop() {
        // Stop the player and release resources
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems() // Clear the playlist if needed
        currentSong = null
    }

}

