package com.project.luckysevens.data

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe

class ScoreRepository(
    private val scoreDao: ScoreDao
) {
    fun observePlayerScore(username: String): Flowable<ScoreEntity> {
        return scoreDao.observeScoreByUsername(username)
    }

    fun getPlayerScore(username: String): Maybe<ScoreEntity> {
        return scoreDao.getScoreByUsername(username)
    }

    fun ensurePlayerExists(username: String, initialScore: Int): Maybe<ScoreEntity> {
        return scoreDao.getScoreByUsername(username)
            .switchIfEmpty(
                scoreDao.upsertScore(
                    ScoreEntity(username = username, score = initialScore)
                ).andThen(scoreDao.getScoreByUsername(username))
            )
    }

    fun savePlayerScore(username: String, score: Int): Completable {
        return scoreDao.upsertScore(
            ScoreEntity(
                username = username,
                score = score,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun addReward(username: String, rewardAmount: Int, initialScore: Int): Completable {
        return ensurePlayerExists(username, initialScore)
            .flatMapCompletable { entity ->
                savePlayerScore(username, entity.score + rewardAmount)
            }
    }

    fun getRanking(): Flowable<List<ScoreEntity>> {
        return scoreDao.getAllScoresOrdered()
    }

    fun saveGameResult(username: String, bet: Int, winnings: Int): Completable {
        return scoreDao.insertGameResult(
            GameResultEntity(
                username = username,
                bet = bet,
                winnings = winnings
            )
        )
    }

    fun getGameHistory(username: String): Flowable<List<GameResultEntity>> {
        return scoreDao.getHistoryByUsername(username)
    }
}
