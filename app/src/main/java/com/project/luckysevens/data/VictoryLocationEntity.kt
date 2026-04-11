package com.project.luckysevens.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "victory_locations")
data class VictoryLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val victoryType: String,
    val coinsAfterWin: Int,
    val calendarEventTitle: String
)