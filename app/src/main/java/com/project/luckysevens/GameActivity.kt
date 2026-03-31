package com.project.luckysevens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreDao
import com.project.luckysevens.data.ScoreEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private lateinit var scoreDao: ScoreDao

    private lateinit var tvCoins: TextView
    private lateinit var tvBetAmount: TextView
    private lateinit var slot1: ImageView
    private lateinit var slot2: ImageView
    private lateinit var slot3: ImageView
    private lateinit var btnSpin: Button
    private lateinit var paytableCard: CardView
    private lateinit var btnTogglePaytable: Button

    private val handler = Handler(Looper.getMainLooper())
    private val disposables = CompositeDisposable()
    private val username = "Jugador"
    private var isSpinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        supportActionBar?.hide()

        gameManager = GameManager()
        scoreDao = AppDatabase.getInstance(applicationContext).scoreDao()

        tvCoins = findViewById(R.id.tvCoinsGame)
        tvBetAmount = findViewById(R.id.tvBetAmount)
        slot1 = findViewById(R.id.slot1)
        slot2 = findViewById(R.id.slot2)
        slot3 = findViewById(R.id.slot3)
        btnSpin = findViewById(R.id.btnSpin)
        paytableCard = findViewById(R.id.paytableCard)
        btnTogglePaytable = findViewById(R.id.btnTogglePaytable)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)

        updateUI()
        loadSavedScore()

        btnBack.setOnClickListener { finish() }

        btnMinus.setOnClickListener {
            if (!isSpinning) {
                gameManager.decreaseBet()
                updateUI()
            }
        }

        btnPlus.setOnClickListener {
            if (!isSpinning) {
                gameManager.increaseBet()
                updateUI()
            }
        }

        btnSpin.setOnClickListener {
            if (!isSpinning) {
                if (gameManager.coins >= gameManager.currentBet && gameManager.currentBet > 0) {
                    startSpinAnimation()
                } else {
                    Toast.makeText(this, "No tienes suficientes monedas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnTogglePaytable.setOnClickListener {
            paytableCard.visibility =
                if (paytableCard.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun startSpinAnimation() {
        isSpinning = true
        btnSpin.isEnabled = false

        val duration = 1500L
        val interval = 100L
        val startTime = System.currentTimeMillis()

        val runnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime

                if (elapsed < duration) {
                    slot1.setImageResource(gameManager.icons[Random.nextInt(gameManager.icons.size)])
                    slot2.setImageResource(gameManager.icons[Random.nextInt(gameManager.icons.size)])
                    slot3.setImageResource(gameManager.icons[Random.nextInt(gameManager.icons.size)])

                    handler.postDelayed(this, interval)
                } else {
                    val winnings = gameManager.spin()
                    updateUI()
                    saveScore(gameManager.coins)
                    isSpinning = false
                    btnSpin.isEnabled = true

                    if (winnings > 0) {
                        Toast.makeText(
                            this@GameActivity,
                            "¡GANASTE $winnings MONEDAS! 🎉",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        handler.post(runnable)
    }

    private fun updateUI() {
        tvCoins.text = "Coins: ${gameManager.coins}"
        tvBetAmount.text = "${gameManager.currentBet} COINS"

        slot1.setImageResource(gameManager.getDrawableId(0))
        slot2.setImageResource(gameManager.getDrawableId(1))
        slot3.setImageResource(gameManager.getDrawableId(2))
    }

    private fun loadSavedScore() {
        val disposable = scoreDao.getScoreByUsername(username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { savedScore ->
                    gameManager.setCoins(savedScore.score)
                    updateUI()
                },
                { error ->
                    error.printStackTrace()
                },
                {
                    saveScore(gameManager.coins)
                }
            )

        disposables.add(disposable)
    }

    private fun saveScore(score: Int) {
        val disposable = scoreDao.upsertScore(
            ScoreEntity(
                username = username,
                score = score,
                updatedAt = System.currentTimeMillis()
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { },
                { error ->
                    error.printStackTrace()
                }
            )

        disposables.add(disposable)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}