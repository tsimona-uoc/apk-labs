package com.project.luckysevens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreEntity
import com.project.luckysevens.fragments.ranking.RankingFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.project.luckysevens.data.ScoreRepository

class MainActivity : AppCompatActivity() {

    private lateinit var scoreRepository: ScoreRepository
    private val disposables = CompositeDisposable()
    private val username = "Jugador"
    private var currentScoreEntity: ScoreEntity? = null
    private lateinit var tvCoins: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        // 🎵 Música
        MusicManager.loadMusic(this)

        scoreRepository = ScoreRepository(
            AppDatabase.getInstance(applicationContext).scoreDao()
        )

        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        tvCoins = findViewById(R.id.tvCoins)
        val btnPlayGame = findViewById<Button>(R.id.btnPlayGame)
        val sideMenuCard = findViewById<CardView>(R.id.sideMenuCard)

        val menuSettings = findViewById<LinearLayout>(R.id.menuSettings)
        val menuRanking = findViewById<LinearLayout>(R.id.menuRanking)
        val menuMusic = findViewById<LinearLayout>(R.id.menuMusic)
        val menuHelp = findViewById<LinearLayout>(R.id.menuHelp)

        ensureInitialScore()
        observePlayerScore()
        checkDailyReward()

        btnPlayGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        topBar.setNavigationOnClickListener {
            sideMenuCard.visibility =
                if (sideMenuCard.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        menuSettings.setOnClickListener {
            showLanguageDialog(sideMenuCard)
        }

        menuRanking.setOnClickListener {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, RankingFragment())
                setReorderingAllowed(true)
            }
            sideMenuCard.visibility = View.GONE
        }

        menuMusic.setOnClickListener {
            startActivity(Intent(this, MusicSettingsActivity::class.java))
            sideMenuCard.visibility = View.GONE
        }

        menuHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
            sideMenuCard.visibility = View.GONE
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, RankingFragment())
            setReorderingAllowed(true)
        }
    }

    private fun ensureInitialScore() {
        val disposable = scoreRepository.ensurePlayerExists(username, 125)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { entity -> currentScoreEntity = entity },
                { it.printStackTrace() }
            )

        disposables.add(disposable)
    }

    private fun observePlayerScore() {
        val disposable = scoreRepository.observePlayerScore(username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { entity ->
                    currentScoreEntity = entity
                    updateCoinsUI(entity.score)
                },
                { it.printStackTrace() }
            )

        disposables.add(disposable)
    }

    private fun updateCoinsUI(amount: Int) {
        tvCoins.text = getString(R.string.coins_label, amount)
    }

    private fun checkDailyReward() {
        val prefs = getSharedPreferences("LuckySevensPrefs", Context.MODE_PRIVATE)
        val lastRewardDate = prefs.getString("last_reward_date", "")

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        if (lastRewardDate != currentDate) {
            val rewardAmount = 50

            val disposable = scoreRepository.addReward(username, rewardAmount, 125)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        prefs.edit().putString("last_reward_date", currentDate).apply()
                        showRewardDialog(rewardAmount)
                    },
                    { it.printStackTrace() }
                )

            disposables.add(disposable)
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

    private fun showLanguageDialog(sideMenuCard: CardView) {
        val options = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> LanguageManager.setLanguage(this, "es")
                    1 -> LanguageManager.setLanguage(this, "en")
                }

                sideMenuCard.visibility = View.GONE
                recreate()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        MusicManager.resumeMusic()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LanguageManager.loadLanguage(newBase)
        super.attachBaseContext(context)
    }
}
