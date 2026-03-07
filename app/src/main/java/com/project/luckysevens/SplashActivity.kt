package com.project.luckysevens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Ocultamos la barra superior para que parezca un juego a pantalla completa
        supportActionBar?.hide()

        // Creamos un temporizador de 2 segundos (2000 milisegundos)
        Handler(Looper.getMainLooper()).postDelayed({
            // Saltamos a la MainActivity
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            // Cerramos la Splash para que el usuario no pueda volver atrás
            finish()
        }, 2000)
    }
}