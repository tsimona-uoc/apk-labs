package com.project.luckysevens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.commit
import com.project.luckysevens.fragments.ranking.RankingFragment
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var coins = 125 // Saldo inicial si no hay guardado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        // Cargar monedas guardadas
        val prefs = getSharedPreferences("LuckySevensPrefs", Context.MODE_PRIVATE)
        coins = prefs.getInt("user_coins", 125)

        val tvCoins = findViewById<TextView>(R.id.tvCoins)
        val btnPlayGame = findViewById<Button>(R.id.btnPlayGame)
        val btnMenu = findViewById<TextView>(R.id.btnMenu)
        val sideMenuCard = findViewById<CardView>(R.id.sideMenuCard)
        
        // Opciones del menú
        val menuSettings = findViewById<LinearLayout>(R.id.menuSettings)
        val menuRanking = findViewById<LinearLayout>(R.id.menuRanking)
        val menuMusic = findViewById<LinearLayout>(R.id.menuMusic)

        updateCoinsUI(tvCoins)

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

        // Comprobar recompensa diaria
        checkDailyReward(tvCoins)
    }

    private fun updateCoinsUI(tvCoins: TextView) {
        tvCoins.text = getString(R.string.coins_label, coins)
    }

    private fun checkDailyReward(tvCoins: TextView) {
        val prefs = getSharedPreferences("LuckySevensPrefs", Context.MODE_PRIVATE)
        val lastRewardDate = prefs.getString("last_reward_date", "")
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        if (lastRewardDate != currentDate) {
            // Entregar recompensa de 50 monedas
            val rewardAmount = 50
            coins += rewardAmount
            
            // Guardar nueva fecha y saldo
            prefs.edit().apply {
                putString("last_reward_date", currentDate)
                putInt("user_coins", coins)
                apply()
            }

            updateCoinsUI(tvCoins)
            showRewardDialog(rewardAmount)
        }
    }

    private fun showRewardDialog(amount: Int) {
        val dialogView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        textView.text = "¡RECOMPENSA DIARIA!\nHas recibido $amount monedas por iniciar sesión hoy. 🎰✨"
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setPadding(20, 50, 20, 50)

        AlertDialog.Builder(this)
            .setTitle("¡Felicidades!")
            .setView(dialogView)
            .setPositiveButton("¡GENIAL!") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
}
