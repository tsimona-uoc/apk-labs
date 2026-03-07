package com.project.luckysevens.fragments.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.luckysevens.R
import com.project.luckysevens.fragments.ranking.RankingsAdapter

class RankingFragment : Fragment(R.layout.fragment_ranking) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    // 2. Este método NO devuelve nada (Unit), por lo que no hay error de subtipo
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // El parámetro 'view' ya es el layout inflado
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvScores)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RankingsAdapter(List(20) { "Item $it" })
    }
}