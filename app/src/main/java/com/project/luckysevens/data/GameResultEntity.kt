package com.project.luckysevens.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "game_history")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val bet: Int,
    val winnings: Int,
    val date: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
)
