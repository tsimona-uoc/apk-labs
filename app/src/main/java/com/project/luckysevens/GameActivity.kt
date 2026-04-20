package com.project.luckysevens

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreRepository
import com.project.luckysevens.data.VictoryLocationRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.OutputStream
import java.util.Calendar
import kotlin.random.Random
import android.content.Context

class GameActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private lateinit var scoreRepository: ScoreRepository
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

    private var soundPool: SoundPool? = null
    private var soundSpin: Int = 0
    private var soundWin: Int = 0
    private var soundStop: Int = 0
    private var soundBigWin: Int = 0
    private var spinStreamId: Int = 0

    private lateinit var victoryLocationRepository: VictoryLocationRepository
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                handleVictoryWithLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
                openCalendarEventWithoutLocation()
            }
        }

    private var pendingVictoryCoins: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        supportActionBar?.hide()
        initSoundPool()

        gameManager = GameManager()
        scoreRepository = ScoreRepository(
            AppDatabase.getInstance(applicationContext).scoreDao()
        )

        victoryLocationRepository = VictoryLocationRepository(
            AppDatabase.getInstance(applicationContext).victoryLocationDao()
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                    Toast.makeText(
                        this,
                        getString(R.string.not_enough_coins),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnTogglePaytable.setOnClickListener {
            paytableCard.visibility =
                if (paytableCard.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundSpin = soundPool?.load(this, R.raw.slot_spin, 1) ?: 0
        soundWin = soundPool?.load(this, R.raw.win_sound, 1) ?: 0
        soundStop = soundPool?.load(this, R.raw.slot_stop, 1) ?: 0
        soundBigWin = soundPool?.load(this, R.raw.big_win, 1) ?: 0
    }

    private fun startSpinAnimation() {
        isSpinning = true
        btnSpin.isEnabled = false

        val currentBet = gameManager.currentBet
        val winnings = gameManager.spin()
        val targetIcons = IntArray(3) { gameManager.getDrawableId(it) }
        val finalCoins = gameManager.coins

        if (soundSpin != 0) {
            spinStreamId = soundPool?.play(soundSpin, 1f, 1f, 1, 0, 1f) ?: 0
        }

        val startTime = System.currentTimeMillis()
        val slots = listOf(slot1, slot2, slot3)
        val stopTimes = listOf(1000L, 1700L, 2400L)
        val stopped = BooleanArray(3)

        val runnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                var allStopped = true

                for (i in slots.indices) {
                    if (elapsed < stopTimes[i]) {
                        slots[i].setImageResource(
                            gameManager.icons.random()
                        )
                        allStopped = false
                    } else if (!stopped[i]) {
                        stopped[i] = true
                        slots[i].setImageResource(targetIcons[i])
                        if (soundStop != 0) soundPool?.play(soundStop, 1f, 1f, 1, 0, 1f)

                        if (i == slots.size - 1) {
                            soundPool?.stop(spinStreamId)
                        }
                    }
                }

                if (!allStopped) {
                    handler.postDelayed(this, 80)
                } else {
                    updateUI()

                    if (winnings > 0) {
                        Toast.makeText(
                            this@GameActivity,
                            getString(R.string.win_message, winnings),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    isSpinning = false
                    btnSpin.isEnabled = true
                }
            }
        }

        handler.post(runnable)
    }

    private fun updateUI() {
        tvCoins.text = getString(R.string.coins_label, gameManager.coins)
        tvBetAmount.text = getString(R.string.bet_amount, gameManager.currentBet)

        slot1.setImageResource(gameManager.getDrawableId(0))
        slot2.setImageResource(gameManager.getDrawableId(1))
        slot3.setImageResource(gameManager.getDrawableId(2))
    }

    private fun loadSavedScore() {
        val disposable = scoreRepository.getPlayerScore(username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { savedScore ->
                gameManager.setCoins(savedScore.score)
                updateUI()
            }

        disposables.add(disposable)
    }

    private fun handleVictoryWithLocation() {
        // (sin cambios relevantes aquí)
    }

    private fun openCalendarEventWithoutLocation() {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Lucky Sevens Victory")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        disposables.clear()
        soundPool?.release()
        soundPool = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        MusicManager.resumeMusic()
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LanguageManager.loadLanguage(newBase)
        super.attachBaseContext(context)
    }
}