package com.shadspace.kahani

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.shadspace.kahani.SharedPrefManager.getUserEmail
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Currency

class Subscribe : AppCompatActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_subscribe)


        // Initialize Razorpay Checkout
        Checkout.preload(applicationContext)
        fetchAPIs()
        //close button click

        val close: ImageView = findViewById(R.id.crossImage)
        close.setOnClickListener {
            finish()
        }


    }

    private fun fetchAPIs() {
        // Get a reference to Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Fetch the Razorpay API key from Firestore
        firestore.collection("apis").document("razorpay")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the API key from Firestore document
                    val razorpayKey = document.getString("APIs") ?: ""

                    // Start the payment with the retrieved key
                    if (razorpayKey.isNotEmpty()) {

                        //  Toast.makeText(this, razorpayKey, Toast.LENGTH_SHORT).show()

                        //OnClick to start payment
                        val payButton: CardView = findViewById(R.id.btnSubscribe)
                        payButton.setOnClickListener {
                            startPayment(razorpayKey)

                        }
                    } else {
                        Toast.makeText(this, "Razorpay key not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error fetching Razorpay key", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun startPayment(razorpayKey: String) {
        val checkout = Checkout()

        // Set your Razorpay API key
        checkout.setKeyID(razorpayKey)
        try {
            val options = JSONObject()
            options.put("name", "KahaniSuno")
            options.put("description", "Monthly Sub")
            options.put(
                "image",
                "https://firebasestorage.googleapis.com/v0/b/kahani-366e5.appspot.com/o/logo%2Fpinlogo.png?alt=media&token=806a5d67-6189-49b1-b3eb-8fd270350d11"
            )
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", "9900") // amount in paise (9900 = 99.00 INR)
            options.put("prefill.email", "test@razorpay.com")
            options.put("prefill.contact", "9876543210")

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error in starting Razorpay Checkout: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SubscriptionResult::class.java))

        if (razorpayPaymentID != null) {
            storeSubscriptionDataInFirebase(razorpayPaymentID)
        }
    }


    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: Please try again", Toast.LENGTH_SHORT).show()


    }

    private fun storeSubscriptionDataInFirebase(razorpayPaymentID: String) {
        val userEmail = getUserEmail(this)
        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            val subscriptionStart = System.currentTimeMillis()
            val subscriptionEnd = subscriptionStart + 30 * 24 * 60 * 60 * 1000

            // Define the subscription data map
            val subscriptionData = hashMapOf(
                "payment_id" to razorpayPaymentID,
                "subscription_start" to subscriptionStart,
                "subscription_end" to subscriptionEnd,
                "subscription_status" to "active" // Update status to active
            )

            // Update the user's subscription data in the "users" collection
            db.collection("users")
                .document(userEmail)
                .update(subscriptionData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("Firebase", "User subscription data updated successfully in Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase", "Error updating subscription data in Firestore", e)
                }
        } else {
            Log.e("Firebase", "User email not found in shared preferences")
        }
    }

}
