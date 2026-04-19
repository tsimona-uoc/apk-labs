package com.project.luckysevens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MusicSettingsActivity : AppCompatActivity() {

    private lateinit var switchMusic: Switch
    private lateinit var btnSelectMusic: Button
    private lateinit var btnDefaultMusic: Button

    private val PICK_AUDIO_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_settings)

        switchMusic = findViewById(R.id.switchMusic)
        btnSelectMusic = findViewById(R.id.btnSelectMusic)
        btnDefaultMusic = findViewById(R.id.btnDefaultMusic)

        // Estado inicial
        switchMusic.isChecked = MusicManager.isMusicPlaying()

        // Switch ON/OFF
        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MusicManager.startMusic(this)
            } else {
                MusicManager.pauseMusic(this)
            }
        }

        btnDefaultMusic.setOnClickListener {
            MusicManager.playDefaultMusic(this)
        }

        // BOTÓN → abrir selector de archivos
        btnSelectMusic.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "audio/*"
            startActivityForResult(intent, PICK_AUDIO_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK) {
            val audioUri: Uri? = data?.data

            if (audioUri != null) {
                contentResolver.takePersistableUriPermission(
                    audioUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                MusicManager.playCustomMusic(this, audioUri)
            }
        }
    }
}