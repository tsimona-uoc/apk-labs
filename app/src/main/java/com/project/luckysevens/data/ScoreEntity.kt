package com.project.luckysevens.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_scores")
data class ScoreEntity(
    @PrimaryKey val username: String,
    val score: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

