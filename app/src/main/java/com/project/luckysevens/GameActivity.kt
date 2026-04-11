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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.random.Random
import com.project.luckysevens.data.ScoreRepository
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.project.luckysevens.data.VictoryLocationRepository
import java.util.Calendar


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

    private lateinit var victoryLocationRepository: VictoryLocationRepository
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                handleVictoryWithLocation()
            } else {
                Toast.makeText(
                    this,
                    "No se ha concedido el permiso de ubicación",
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

                    if (hasSpecialRubyVictory()) {
                        pendingVictoryCoins = gameManager.coins
                        checkLocationPermissionAndHandleVictory()
                    }

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
        val disposable = scoreRepository.getPlayerScore(username)
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
        val disposable = scoreRepository.savePlayerScore(username, score)
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

    private fun hasSpecialRubyVictory(): Boolean {
        val rubyDrawable = R.drawable.ic_rubi

        var rubyCount = 0

        if (gameManager.getDrawableId(0) == rubyDrawable) rubyCount++
        if (gameManager.getDrawableId(1) == rubyDrawable) rubyCount++
        if (gameManager.getDrawableId(2) == rubyDrawable) rubyCount++

        return rubyCount >= 2
    }

    private fun checkLocationPermissionAndHandleVictory() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                handleVictoryWithLocation()
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun handleVictoryWithLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            openCalendarEventWithoutLocation()
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        saveVictoryLocationAndOpenCalendar(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                        openCalendarEventWithoutLocation()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                    openCalendarEventWithoutLocation()
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            openCalendarEventWithoutLocation()
        }
    }

    private fun saveVictoryLocationAndOpenCalendar(latitude: Double, longitude: Double) {
        val eventTitle = "Victoria Lucky Sevens"
        val victoryType = "DOUBLE_RUBY_WIN"

        val disposable = victoryLocationRepository.saveVictoryLocation(
            username = username,
            latitude = latitude,
            longitude = longitude,
            victoryType = victoryType,
            coinsAfterWin = pendingVictoryCoins,
            calendarEventTitle = eventTitle
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    openCalendarEventWithLocation(eventTitle, latitude, longitude)
                },
                { error ->
                    error.printStackTrace()
                    openCalendarEventWithLocation(eventTitle, latitude, longitude)
                }
            )

        disposables.add(disposable)
    }

    private fun openCalendarEventWithLocation(
        title: String,
        latitude: Double,
        longitude: Double
    ) {
        val beginTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
        }

        val endTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 35)
        }

        val locationText = "Lat: $latitude, Lon: $longitude"

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, "Victoria especial con 2 o más rubíes")
            putExtra(CalendarContract.Events.EVENT_LOCATION, locationText)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
        }

        startActivity(intent)
    }

    private fun openCalendarEventWithoutLocation() {
        val beginTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
        }

        val endTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 35)
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Victoria Lucky Sevens")
            putExtra(CalendarContract.Events.DESCRIPTION, "Victoria especial con 2 o más rubíes")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
        }

        startActivity(intent)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}