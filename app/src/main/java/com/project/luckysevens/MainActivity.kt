package com.project.luckysevens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.commit
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreDao
import com.project.luckysevens.data.ScoreEntity
import com.project.luckysevens.fragments.ranking.RankingFragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var scoreDao: ScoreDao
    private val dbExecutor = Executors.newSingleThreadExecutor()
    private var currentScoreEntity: ScoreEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        scoreDao = AppDatabase.getInstance(applicationContext).scoreDao()

        val tvCoins = findViewById<TextView>(R.id.tvCoins)
        val btnPlayGame = findViewById<Button>(R.id.btnPlayGame)
        val btnMenu = findViewById<TextView>(R.id.btnMenu)
        val sideMenuCard = findViewById<CardView>(R.id.sideMenuCard)
        
        val menuSettings = findViewById<LinearLayout>(R.id.menuSettings)
        val menuRanking = findViewById<LinearLayout>(R.id.menuRanking)
        val menuMusic = findViewById<LinearLayout>(R.id.menuMusic)

        // Cargar monedas desde la base de datos
        dbExecutor.execute {
            val entity = scoreDao.getScoreByUsername("Jugador")
                ?: scoreDao.upsertAndGet(ScoreEntity(username = "Jugador", score = 125))
            
            currentScoreEntity = entity
            
            runOnUiThread {
                updateCoinsUI(tvCoins, entity.score)
                checkDailyReward(tvCoins)
            }
        }

        btnPlayGame.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener {
            sideMenuCard.visibility = if (sideMenuCard.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        menuSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            sideMenuCard.visibility = View.GONE
        }

        menuRanking.setOnClickListener {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, RankingFragment())
                setReorderingAllowed(true)
            }
            sideMenuCard.visibility = View.GONE
        }

        menuMusic.setOnClickListener {
            Toast.makeText(this, "Music toggled", Toast.LENGTH_SHORT).show()
            sideMenuCard.visibility = View.GONE
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, RankingFragment())
            setReorderingAllowed(true)
        }
    }

    private fun updateCoinsUI(tvCoins: TextView, amount: Int) {
        tvCoins.text = getString(R.string.coins_label, amount)
    }

    private fun checkDailyReward(tvCoins: TextView) {
        val prefs = getSharedPreferences("LuckySevensPrefs", Context.MODE_PRIVATE)
        val lastRewardDate = prefs.getString("last_reward_date", "")
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        if (lastRewardDate != currentDate) {
            val rewardAmount = 50
            
            dbExecutor.execute {
                val entity = currentScoreEntity ?: scoreDao.getScoreByUsername("Jugador")
                if (entity != null) {
                    val newScore = entity.score + rewardAmount
                    val updatedEntity = ScoreEntity(username = entity.username, score = newScore)
                    scoreDao.upsertScore(updatedEntity)
                    currentScoreEntity = updatedEntity
                    
                    runOnUiThread {
                        updateCoinsUI(tvCoins, newScore)
                        showRewardDialog(rewardAmount)
                    }
                }
            }
            
            prefs.edit().putString("last_reward_date", currentDate).apply()
        }
    }

    private fun showRewardDialog(amount: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.daily_reward_title)
            .setMessage(getString(R.string.daily_reward_message, amount))
            .setPositiveButton(R.string.daily_reward_button) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbExecutor.shutdown()
    }
}
