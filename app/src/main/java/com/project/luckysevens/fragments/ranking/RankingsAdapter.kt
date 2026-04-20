package com.project.luckysevens.fragments.ranking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.luckysevens.R
import com.project.luckysevens.data.ScoreEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class RankingsAdapter(private var items: List<ScoreEntity> = emptyList()) :
    RecyclerView.Adapter<RankingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRankName)
        val tvScore: TextView = view.findViewById(R.id.tvRankScore)
        val tvDate: TextView = view.findViewById(R.id.tvRankDate)
    }

    fun updateData(newItems: List<ScoreEntity>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        Log.d("DATE_DEBUG", "raw timestamp: ${item.updatedAt}")

        holder.tvName.text = "${position + 1}. ${item.username}"

        holder.tvScore.text = context.getString(R.string.coins_amount, item.score)

        val sdf = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT,
            SimpleDateFormat.SHORT,
            Locale.getDefault()
        )

        holder.tvDate.text = sdf.format(Date(item.updatedAt))
    }

    override fun getItemCount() = items.size
}