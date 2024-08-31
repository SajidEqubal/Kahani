package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private var clickCount = 0

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var loginButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        loadingAnimation = findViewById(R.id.animation_view)
        loginButton = findViewById(R.id.googleLogin)

        // Initialize Firebase and Google Sign-In
        FirebaseApp.initializeApp(this)
        setupGoogleSignIn()

        // Set click listener for login button
        loginButton.setOnClickListener {
            startGoogleSignIn()
        }

        val adminlogin: ImageView = findViewById(R.id.logo)
        adminlogin.setOnClickListener {
            clickCount++
            if (clickCount >= 10) {
                showLoginDialog()
                clickCount = 0 // Reset the counter if needed
            }
        }
    }


    private fun showLoginDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.editEmail)
        val passwordEditText: EditText = dialogView.findViewById(R.id.editPassword)
        val loginButton: CardView =
            dialogView.findViewById(R.id.buttonLogin)
        val closeIcon: ImageView = dialogView.findViewById(R.id.iconClose)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        closeIcon.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email == "test@gmail.com" && password == "test@123") {
                // Start the HomeActivity
                startActivity(Intent(this, Home::class.java))
                finish()
                dialog.dismiss() // Close the dialog
            } else {
                // Optionally, show an error message
                emailEditText.error = "Invalid credentials"
                passwordEditText.error = "Invalid credentials"
            }
        }

        dialog.show()
    }


    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun startGoogleSignIn() {
        toggleLoading(true)
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            } else {
                toggleLoading(false)
                Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            toggleLoading(false)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save login status in SharedPreferences using the utility class
                SharedPrefManager.setLogin(this, true)
                startActivity(Intent(this, Home::class.java))
                finish()
            } else {
                toggleLoading(false)
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        loadingAnimation.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
    }
}
