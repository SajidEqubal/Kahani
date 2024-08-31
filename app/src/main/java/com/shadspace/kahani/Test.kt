package com.shadspace.kahani

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.shimmer.ShimmerFrameLayout

class Test : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)

        val shimmerView: ShimmerFrameLayout = findViewById(R.id.simmer_view2)
        shimmerView.startShimmer()

    }
}