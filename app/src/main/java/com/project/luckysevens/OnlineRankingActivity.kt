package com.project.luckysevens

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.project.luckysevens.data.online.OnlineRankingAdapter
import com.project.luckysevens.data.online.OnlineScoreRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class OnlineRankingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OnlineRankingAdapter
    private lateinit var progressBar: ProgressBar
    private val onlineRepository = OnlineScoreRepository()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_ranking)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvOnlineRanking)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OnlineRankingAdapter(emptyList())
        recyclerView.adapter = adapter

        loadRanking()
    }

    private fun loadRanking() {
        progressBar.visibility = View.VISIBLE
        
        val disposable = onlineRepository.getTopTenScores()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ players ->
                progressBar.visibility = View.GONE
                adapter.updateData(players)
            }, { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    getString(
                        R.string.ranking_load_error,
                        error.message ?: "Unknown"
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            })
            
        disposables.add(disposable)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
