package com.project.luckysevens.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertScore(score: ScoreEntity): Long

    @Query("SELECT * FROM user_scores WHERE username = :username LIMIT 1")
    fun getScoreByUsername(username: String): ScoreEntity?

    @Transaction
    fun upsertAndGet(score: ScoreEntity): ScoreEntity {
        upsertScore(score)
        return getScoreByUsername(score.username) ?: score
    }
}

