package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shadspace.kahani.databinding.ActivitySubscriptionResultBinding
import com.shadspace.kahani.ui.Home
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionResult : AppCompatActivity() {

    val dateFormat = SimpleDateFormat("dd MMMM | h:mm a", Locale.getDefault())
    val currentDate = Date()
    val formattedDate = dateFormat.format(currentDate)

    lateinit var binding: ActivitySubscriptionResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySubscriptionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.linDone.setOnClickListener { startActivity(Intent(this, Home::class.java))
        finish()}

        binding.textTime.text = formattedDate

    }
}