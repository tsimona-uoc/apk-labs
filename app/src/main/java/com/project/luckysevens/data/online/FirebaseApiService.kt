package com.project.luckysevens.data.online

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FirebaseApiService {
    
    // Obtenemos los jugadores ordenados por puntuación
    // Firebase REST API devuelve un Map<String, PlayerScoreDto>
    @GET("players.json")
    fun getTopScores(
        @Query("orderBy") orderBy: String = "\"score\"",
        @Query("limitToLast") limit: Int = 10
    ): Single<Map<String, PlayerScoreDto>>
}
