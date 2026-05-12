package com.project.luckysevens.data.online

import com.google.firebase.database.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class CommonPrizeRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val commonPrizeRef = database.child("commonPrize")

    /**
     * Observa el valor actual del premio común en tiempo real.
     */
    fun observeCommonPrize(): Observable<Int> {
        return Observable.create { emitter ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prize = snapshot.getValue(Int::class.java) ?: 0
                    if (!emitter.isDisposed) {
                        emitter.onNext(prize)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!emitter.isDisposed) {
                        emitter.onError(error.toException())
                    }
                }
            }
            commonPrizeRef.addValueEventListener(listener)
            emitter.setCancellable { commonPrizeRef.removeEventListener(listener) }
        }
    }

    /**
     * Incrementa el premio común en una cantidad específica de forma atómica.
     */
    fun incrementPrize(amount: Int): Single<Int> {
        return Single.create { emitter ->
            commonPrizeRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val currentPrize = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = currentPrize + amount
                    return Transaction.success(mutableData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) {
                        emitter.onError(error.toException())
                    } else {
                        emitter.onSuccess(snapshot?.getValue(Int::class.java) ?: 0)
                    }
                }
            })
        }
    }

    /**
     * Reclama el premio común de forma atómica. 
     * Devuelve el valor del premio si se ha reclamado con éxito y lo reinicia a 0.
     * Esto evita condiciones de carrera.
     */
    fun claimPrize(): Single<Int> {
        return Single.create { emitter ->
            commonPrizeRef.runTransaction(object : Transaction.Handler {
                var prizeToClaim = 0

                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val currentPrize = mutableData.getValue(Int::class.java) ?: 0
                    if (currentPrize > 0) {
                        prizeToClaim = currentPrize
                        mutableData.value = 0 // Reiniciar premio
                        return Transaction.success(mutableData)
                    } else {
                        // Si el premio ya es 0, alguien más lo reclamó
                        return Transaction.abort()
                    }
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) {
                        emitter.onError(error.toException())
                    } else if (!committed) {
                        emitter.onSuccess(0) // El premio ya fue reclamado por otro
                    } else {
                        emitter.onSuccess(prizeToClaim)
                    }
                }
            })
        }
    }
}
