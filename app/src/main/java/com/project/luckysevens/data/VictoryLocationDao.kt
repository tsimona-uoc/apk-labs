package com.project.luckysevens.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface VictoryLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVictoryLocation(victoryLocation: VictoryLocationEntity): Completable

    @Query("SELECT * FROM victory_locations WHERE username = :username ORDER BY timestamp DESC")
    fun getVictoryLocationsByUsername(username: String): Flowable<List<VictoryLocationEntity>>
}