package com.project.luckysevens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.project.luckysevens.fragments.ranking.RankingFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // Ocultar barra superior por defecto

        val btnPlayGame = findViewById<Button>(R.id.btnPlayGame)

        btnPlayGame.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, RankingFragment())
            setReorderingAllowed(true)
        }
    }
}
