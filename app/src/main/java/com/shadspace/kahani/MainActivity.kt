package com.shadspace.kahani

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val textView: TextView = findViewById(R.id.textView)
        setMessageWithClickableLink(textView)
    }

    private fun setMessageWithClickableLink(textView: TextView) {
        // The text and URLs
        val content = "By continuing you agree to our Terms of Service and Privacy Policy"
        val url1 = "https://www.google.com" // URL for "Terms of Service"
        val url2 = "https://www.facebook.com" // URL for "Privacy Policy"

        // ClickableSpan for "Terms of Service"
        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url1)
                startActivity(intent)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = true
                //textPaint.color = Color.BLUE // Customize link color if needed
            }
        }

        // ClickableSpan for "Privacy Policy"
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url2)
                startActivity(intent)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = true
                // textPaint.color = Color.BLUE // Customize link color if needed
            }
        }

        val spannableString = SpannableString(content)

        // Set clickable span for "Terms of Service"
        val termsStartIndex = content.indexOf("Terms of Service")
        val termsEndIndex = termsStartIndex + "Terms of Service".length
        spannableString.setSpan(
            termsClickableSpan,
            termsStartIndex,
            termsEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set clickable span for "Privacy Policy"
        val privacyStartIndex = content.indexOf("Privacy Policy")
        val privacyEndIndex = privacyStartIndex + "Privacy Policy".length
        spannableString.setSpan(
            privacyClickableSpan,
            privacyStartIndex,
            privacyEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply the spannable string to the TextView
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }
}
