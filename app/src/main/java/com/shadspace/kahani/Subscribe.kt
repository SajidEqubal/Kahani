package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
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

        val payButton: CardView = findViewById(R.id.btnSubscribe)
        payButton.setOnClickListener {
            startPayment()

        }
    }

    private fun startPayment() {
        val checkout = Checkout()

        // Set your Razorpay API key
        checkout.setKeyID("rzp_live_43oF5rZJLnvtrG")
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
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: Please try again", Toast.LENGTH_SHORT).show()


    }
}
