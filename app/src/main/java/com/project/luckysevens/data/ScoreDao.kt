package com.project.luckysevens.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertScore(score: ScoreEntity): Completable

    @Query("SELECT * FROM user_scores WHERE username = :username LIMIT 1")
    fun getScoreByUsername(username: String): Maybe<ScoreEntity>

    @Query("SELECT * FROM user_scores WHERE username = :username LIMIT 1")
    fun observeScoreByUsername(username: String): Flowable<ScoreEntity>
}

