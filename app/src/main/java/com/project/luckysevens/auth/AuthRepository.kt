package com.project.luckysevens.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentPlayerName(): String {
        val user = auth.currentUser
        return user?.displayName ?: user?.email ?: "Jugador invitado"
    }

    fun savePlayer(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val playerData: Map<String, Any> = mapOf(
            "displayName" to (user.displayName ?: "Jugador"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "lastLogin" to ServerValue.TIMESTAMP
        )

        database
            .child("players")
            .child(user.uid)
            .updateChildren(playerData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun signOut() {
        auth.signOut()
    }
}