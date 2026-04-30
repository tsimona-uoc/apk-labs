package com.project.luckysevens

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder

class MusicService : Service() {

    companion object {
        const val ACTION_LOAD = "com.project.luckysevens.music.LOAD"
        const val ACTION_START = "com.project.luckysevens.music.START"
        const val ACTION_PAUSE = "com.project.luckysevens.music.PAUSE"
        const val ACTION_RESUME = "com.project.luckysevens.music.RESUME"
        const val ACTION_DEFAULT = "com.project.luckysevens.music.DEFAULT"
        const val ACTION_CUSTOM = "com.project.luckysevens.music.CUSTOM"
        const val ACTION_RELEASE = "com.project.luckysevens.music.RELEASE"

        fun send(context: Context, action: String, uri: Uri? = null) {
            val intent = Intent(context, MusicService::class.java).apply {
                this.action = action
                uri?.let { data = it }
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOAD -> MusicManager.loadMusic(this)
            ACTION_START -> MusicManager.startMusic(this)
            ACTION_PAUSE -> MusicManager.pauseMusic(this)
            ACTION_RESUME -> MusicManager.resumeMusic(this)
            ACTION_DEFAULT -> MusicManager.playDefaultMusic(this)
            ACTION_CUSTOM -> intent.data?.let { MusicManager.playCustomMusic(this, it) }
            ACTION_RELEASE -> {
                MusicManager.release()
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}