package com.project.luckysevens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object MusicManager {

    private const val PREFS_NAME = "music_prefs"
    private const val KEY_IS_PLAYING = "is_playing"
    private const val KEY_IS_CUSTOM = "is_custom"
    private const val KEY_URI = "music_uri"
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bossa8bit)
            mediaPlayer?.isLooping = true
        }

        if (isPlaying.not()) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun pauseMusic(context: Context) {
        mediaPlayer?.pause()
        isPlaying = false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PLAYING, false).apply()
    }

    fun resumeMusic(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shouldBePlaying = prefs.getBoolean(KEY_IS_PLAYING, true)

        if (shouldBePlaying && mediaPlayer != null && !isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun toggleMusic(context: Context) {
        if (isPlaying) {
            pauseMusic(context)
        } else {
            startMusic(context)
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isMusicPlaying(): Boolean {
        return isPlaying
    }

    fun playCustomMusic(context: Context, uri: Uri) {
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            isLooping = true
            prepare()
            start()
        }

        isPlaying = true

        savePreferences(context, true, uri.toString())
    }

    fun playDefaultMusic(context: Context) {
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bossa8bit)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        isPlaying = true

        savePreferences(context, false, null)
    }

    private fun savePreferences(context: Context, isCustom: Boolean, uri: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_IS_PLAYING, isPlaying)
            putBoolean(KEY_IS_CUSTOM, isCustom)
            putString(KEY_URI, uri)
            apply()
        }
    }

    fun loadMusic(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val isPlayingSaved = prefs.getBoolean(KEY_IS_PLAYING, true)
        val isCustom = prefs.getBoolean(KEY_IS_CUSTOM, false)
        val uriString = prefs.getString(KEY_URI, null)

        if (!isPlayingSaved) {
            isPlaying = false
            return
        }

        try {
            if (isCustom && uriString != null) {
                val uri = Uri.parse(uriString)
                playCustomMusic(context, uri)
            } else {
                playDefaultMusic(context)
            }
        } catch (e: Exception) {
            // Si falla, vuelve a música por defecto
            playDefaultMusic(context)
        }
    }
}