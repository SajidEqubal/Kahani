package com.shadspace.kahani.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.shadspace.kahani.SharedPrefManager

object SubscriptionUtils {

    fun checkSubscriptionStatus(context: Context, callback: (Boolean) -> Unit) {
        // Use SharedPrefManager to fetch user email
        val userEmail = SharedPrefManager.getUserEmail(context)

        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(userEmail)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val subscriptionStatus = document.getString("subscription_status")
                        callback(subscriptionStatus == "active")
                    } else {
                        callback(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to check subscription status: ${e.message}")
                    callback(false)
                }
        } else {
            Toast.makeText(context, "User email not found. Please log in again.", Toast.LENGTH_LONG).show()
            callback(false)
        }
    }
}
