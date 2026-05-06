package com.project.luckysevens.data.online

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.luckysevens.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnlineRankingAdapter(private var players: List<PlayerScoreDto>) :
    RecyclerView.Adapter<OnlineRankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRankName: TextView = view.findViewById(R.id.tvRankName)
        val tvRankDate: TextView = view.findViewById(R.id.tvRankDate)
        val tvRankScore: TextView = view.findViewById(R.id.tvRankScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.tvRankName.text = "${position + 1}. ${player.username}"
        holder.tvRankScore.text = "${player.score} Coins"
        
        val dateStr = if (player.lastUpdate > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(player.lastUpdate))
        } else {
            ""
        }
        holder.tvRankDate.text = dateStr
    }

    override fun getItemCount() = players.size

    fun updateData(newPlayers: List<PlayerScoreDto>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}
