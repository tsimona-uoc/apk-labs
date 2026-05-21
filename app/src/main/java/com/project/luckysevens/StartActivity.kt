package com.project.luckysevens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.luckysevens.auth.AuthRepository

class StartActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {

                val account = task.getResult(ApiException::class.java)

                if (account.idToken != null) {

                    firebaseAuthWithGoogle(account.idToken!!)

                } else {

                    Toast.makeText(
                        this,
                        getString(R.string.login_invalid_google_credential),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: ApiException) {

                Log.e("StartActivity", "Google Sign-In failed", e)

                Toast.makeText(
                    this,
                    getString(R.string.login_google_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        supportActionBar?.hide()

        configureGoogleSignIn()

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnLogin = findViewById<Button>(R.id.btnPlay2)

        btnPlay.setOnClickListener {
            openMainActivity()
        }

        btnLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun configureGoogleSignIn() {

        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(
                getString(R.string.default_web_client_id)
            )
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {

        googleSignInClient.signOut().addOnCompleteListener {

            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
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
                                        user.displayName
                                            ?: user.email
                                            ?: getString(R.string.default_player_name)
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()

                                openMainActivity()
                            },

                            onError = { exception ->

                                Log.e(
                                    "StartActivity",
                                    "Error guardando jugador",
                                    exception
                                )

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

                    Log.e(
                        "StartActivity",
                        "Error autenticando con Firebase",
                        task.exception
                    )

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