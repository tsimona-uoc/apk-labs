package com.project.luckysevens.fragments.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.luckysevens.R
import com.project.luckysevens.data.AppDatabase
import com.project.luckysevens.data.ScoreRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class RankingFragment : Fragment(R.layout.fragment_ranking) {
    
    private lateinit var scoreRepository: ScoreRepository
    private val disposables = CompositeDisposable()
    private lateinit var adapter: RankingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scoreRepository = ScoreRepository(
            AppDatabase.getInstance(requireContext()).scoreDao()
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvScores)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = RankingsAdapter()
        recyclerView.adapter = adapter

        loadRanking()
    }

    private fun loadRanking() {
        val disposable = scoreRepository.getRanking()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { scores ->
                    adapter.updateData(scores)
                },
                { error ->
                    error.printStackTrace()
                }
            )
        disposables.add(disposable)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}
