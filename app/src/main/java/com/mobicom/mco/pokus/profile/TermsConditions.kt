package com.mobicom.mco.pokus.profile

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.R

class TermsConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)

        val backIcon = findViewById<ImageView>(R.id.backTermsIcon)
        backIcon.setOnClickListener {
            finish()
        }
    }
}
