package com.shadspace.kahani

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.shadspace.kahani.SharedPrefManager // Import the SharedPrefManager

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        if (isInternetAvailable(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                // Check login status using SharedPrefManager
                val isLoggedIn = SharedPrefManager.isLoggedIn(this)

                if (isLoggedIn) {
                    // User is logged in, navigate to Home screen
                    startActivity(Intent(this, Home::class.java))
                } else {
                    // User is not logged in, navigate to Onboarding
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }, 3000)
        } else {
            // Handle no internet connection
            // (Existing code for handling no internet connection remains the same)
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
