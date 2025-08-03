package com.mobicom.mco.pokus.profile

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.R

class PrivacyPolicy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val backIcon = findViewById<ImageView>(R.id.backPrivacyIcon)
        backIcon.setOnClickListener {
            finish()
        }
    }
}
