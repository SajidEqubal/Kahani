package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shadspace.kahani.SharedPrefManager.clearPreferences
import com.shadspace.kahani.databinding.ActivityProfileBinding

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // FirebaseAuth instance using lazy initialization
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize view binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val privacyUrl = "https://sites.google.com/view/kahanisuno/privacy-policy"
        val termsUrl = "https://sites.google.com/view/kahanisuno/terms-conditions"
        val contactUrl = "https://sites.google.com/view/kahanisuno/contact-us"

        //Load the user's profile photo
        loadUserProfilePhoto()


        binding.relContact.setOnClickListener {
            val intent = Intent(this, WebView::class.java)
            intent.putExtra("URL", contactUrl)
            startActivity(intent)
        }

        binding.relPrivacy.setOnClickListener {
            val intent = Intent(this, WebView::class.java)
            intent.putExtra("URL", privacyUrl)
            startActivity(intent)
        }

        binding.relTerms.setOnClickListener {
            val intent = Intent(this, WebView::class.java)
            intent.putExtra("URL", termsUrl)
            startActivity(intent)
        }


        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set onClickListener for logout button
        binding.relLogout.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
                clearPreferences(this)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun loadUserProfilePhoto() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val photoUrl = user.photoUrl

            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .error(R.drawable.logo) // on error image
                .into(binding.profileImageView)
        } else {
            binding.profileImageView.setImageResource(R.drawable.logo) // default image
        }

    }
}

