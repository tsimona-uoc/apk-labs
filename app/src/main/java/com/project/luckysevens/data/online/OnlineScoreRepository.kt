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
    fun saveVictory(winnings: Int): Completable {
        return Completable.create { emitter ->
            val user = auth.currentUser
            if (user == null) {
                emitter.onError(Exception("Usuario no autenticado"))
                return@create
            }

            val victoryId = database.child("victories").push().key ?: ""
            val victory = VictoryDto(
                username = user.displayName ?: user.email ?: "Anónimo",
                winnings = winnings,
                date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                timestamp = System.currentTimeMillis()
            )

            database.child("victories").child(victoryId).setValue(victory)
                .addOnSuccessListener { 
                    updatePlayerScore(user.uid, winnings)
                    updateCommonPrize(winnings / 10) // El 10% va al premio común
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
     * Actualiza el premio común (Common Prize) usando una transacción para evitar colisiones.
     */
    private fun updateCommonPrize(amount: Int) {
        database.child("commonPrize").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentPrize = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentPrize + amount
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
            }
        })
    }

    /**
     * Observa el premio común en tiempo real.
     */
    fun observeCommonPrize(): Observable<Int> {
        return Observable.create { emitter ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prize = snapshot.getValue(Int::class.java) ?: 0
                    emitter.onNext(prize)
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(error.toException())
                }
            }
            database.child("commonPrize").addValueEventListener(listener)
            emitter.setCancellable { database.child("commonPrize").removeEventListener(listener) }
        }
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
