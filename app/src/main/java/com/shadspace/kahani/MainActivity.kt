package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore
import com.shadspace.kahani.SharedPrefManager.getUserEmail
import com.shadspace.kahani.ui.Home

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

        //Check the subscription status daily
        checkAndUpdateSubscriptionStatus()


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

                // Get the user's email and name
                val userEmail = firebaseAuth.currentUser?.email
                val userName = firebaseAuth.currentUser?.displayName

                // Save the user email and name to shared preferences
                userEmail?.let { email ->
                    SharedPrefManager.setLogin(this, true)
                    SharedPrefManager.setUserEmail(this, email)

                    userName?.let { name ->
                        SharedPrefManager.setUserName(this, name) // Save the user's name
                    }

                    // Store the user's email in Firebase for subscription purposes
                    storeUserEmailInFirestore(email)

                }
                startActivity(Intent(this, Home::class.java))
                finish()
            } else {
                toggleLoading(false)
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeUserEmailInFirestore(email: String) {
        val db = FirebaseFirestore.getInstance()

        // Reference to the "users" collection
        val userRef = db.collection("users").document(email)

        // Check if the document with the given email already exists
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // If the document exists, log or handle the case
                    Log.d("Firebase", "User already exists in Firestore!")
                  //  Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    // Define the user data map
                    val userData = hashMapOf(
                        "email" to email,
                        "subscription_status" to "inactive",
                        "created_at" to System.currentTimeMillis()
                    )

                    // Store the user's email in the "users" collection
                    userRef.set(userData)
                        .addOnSuccessListener {
                            Log.d("Firebase", "User email successfully stored in Firestore!")
                           // Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "Error storing user email", e)
                            Toast.makeText(
                                this,
                                "Failed to register user. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error checking user existence: ${e.message}")
                Toast.makeText(
                    this,
                    "Error checking user existence. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun checkAndUpdateSubscriptionStatus() {
        val userEmail = getUserEmail(this)
        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users")
                .document(userEmail)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val subscriptionEnd = document.getLong("subscription_end") ?: 0L
                        val currentDate = System.currentTimeMillis()

                        if (currentDate > subscriptionEnd) {
                            // Subscription has expired, update status to inactive
                            db.collection("users")
                                .document(userEmail)
                                .update("subscription_status", "inactive")
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Subscription status updated to inactive.")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firebase", "Error updating subscription status", e)
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase", "Failed to check subscription status: ${e.message}")
                }
        } else {
            Log.e("Firebase", "User email not found in shared preferences")
        }
    }


}
