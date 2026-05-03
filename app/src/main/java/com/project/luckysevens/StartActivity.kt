package com.project.luckysevens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.luckysevens.auth.AuthRepository
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        supportActionBar?.hide()

        credentialManager = CredentialManager.create(this)

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnLogin = findViewById<Button>(R.id.btnPlay2)

        btnPlay.setOnClickListener {
            openMainActivity()
        }

        btnLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    this@StartActivity,
                    request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Toast.makeText(
                        this@StartActivity,
                        getString(R.string.login_invalid_google_credential),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: GetCredentialException) {
                Log.e("StartActivity", "Error obteniendo credencial", e)
                Toast.makeText(
                    this@StartActivity,
                    getString(R.string.login_google_error),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("StartActivity", "Error inesperado en login", e)
                Toast.makeText(
                    this@StartActivity,
                    getString(R.string.login_unexpected_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    if (user != null) {
                        authRepository.savePlayer(
                            user = user,
                            onSuccess = {
                                Toast.makeText(
                                    this,
                                    getString(
                                        R.string.login_success,
                                        user.displayName ?: user.email ?: getString(R.string.default_player_name)
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()

                                openMainActivity()
                            },
                            onError = { exception ->
                                Log.e("StartActivity", "Error guardando jugador", exception)

                                Toast.makeText(
                                    this,
                                    getString(R.string.login_player_save_error),
                                    Toast.LENGTH_SHORT
                                ).show()

                                openMainActivity()
                            }
                        )
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.login_user_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("StartActivity", "Error autenticando con Firebase", task.exception)

                    Toast.makeText(
                        this,
                        getString(R.string.login_firebase_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LanguageManager.loadLanguage(newBase)
        super.attachBaseContext(context)
    }
}