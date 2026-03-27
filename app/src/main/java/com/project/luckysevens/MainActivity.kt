package com.project.luckysevens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreDao
import com.project.luckysevens.data.ScoreEntity
import com.project.luckysevens.fragments.ranking.RankingFragment
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var scoreDao: ScoreDao
    private val dbExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        scoreDao = AppDatabase.getInstance(applicationContext).scoreDao()

        val btnPlayGame = findViewById<Button>(R.id.btnPlayGame)
        val scoreText = findViewById<TextView>(R.id.tvCoins)
        dbExecutor.execute {
            val scoreEntity = scoreDao.getScoreByUsername("Jugador")
                ?: scoreDao.upsertAndGet(ScoreEntity(username = "Jugador", score = 125))

            runOnUiThread {
                scoreText.text = "Coins: ${scoreEntity.score}"
            }
        }

        btnPlayGame.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, RankingFragment())
            setReorderingAllowed(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbExecutor.shutdown()
    }
}
