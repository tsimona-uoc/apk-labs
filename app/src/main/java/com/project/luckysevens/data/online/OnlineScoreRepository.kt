package com.project.luckysevens.data.online

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class OnlineScoreRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val apiService = RetrofitClient.instance

    /**
     * Guarda una victoria en Firebase usando el SDK.
     */
    fun saveVictory(winnings: Int, commonPrizeWon: Int = 0): Completable {
        return Completable.create { emitter ->
            val user = auth.currentUser
            if (user == null) {
                emitter.onError(Exception("Usuario no autenticado"))
                return@create
            }

            val totalWinnings = winnings + commonPrizeWon
            val victoryId = database.child("victories").push().key ?: ""
            val victory = VictoryDto(
                username = user.displayName ?: user.email ?: "Anónimo",
                winnings = totalWinnings,
                date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                timestamp = System.currentTimeMillis()
            )

            database.child("victories").child(victoryId).setValue(victory)
                .addOnSuccessListener { 
                    updatePlayerScore(user.uid, totalWinnings)
                    emitter.onComplete() 
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    /**
     * Actualiza la puntuación total del jugador en Firebase y su marca de tiempo.
     */
    private fun updatePlayerScore(uid: String, scoreToAdd: Int) {
        val updates = mapOf(
            "score" to ServerValue.increment(scoreToAdd.toLong()),
            "lastLogin" to ServerValue.TIMESTAMP
        )
        database.child("players").child(uid).updateChildren(updates)
    }

    /**
     * Recupera el Top 10 de puntuaciones usando Retrofit (REST API).
     */
    fun getTopTenScores(): Single<List<PlayerScoreDto>> {
        return apiService.getTopScores()
            .map { responseMap: Map<String, PlayerScoreDto> ->
                responseMap.values.sortedByDescending { it.score }.take(10)
            }
            .onErrorReturn { error ->
                android.util.Log.e("OnlineScoreRepo", "Error recuperando ranking: ${error.message}")
                emptyList() // Si da 404 o error, devolvemos lista vacía
            }
    }
}
