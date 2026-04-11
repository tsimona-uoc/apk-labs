package com.project.luckysevens.data

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

class VictoryLocationRepository(
    private val victoryLocationDao: VictoryLocationDao
) {
    fun saveVictoryLocation(
        username: String,
        latitude: Double,
        longitude: Double,
        victoryType: String,
        coinsAfterWin: Int,
        calendarEventTitle: String
    ): Completable {
        return victoryLocationDao.insertVictoryLocation(
            VictoryLocationEntity(
                username = username,
                latitude = latitude,
                longitude = longitude,
                victoryType = victoryType,
                coinsAfterWin = coinsAfterWin,
                calendarEventTitle = calendarEventTitle
            )
        )
    }

    fun getVictoryLocations(username: String): Flowable<List<VictoryLocationEntity>> {
        return victoryLocationDao.getVictoryLocationsByUsername(username)
    }
}