package com.mobicom.mco.pokus.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.R
import com.mobicom.mco.pokus.profile.EditProfileActivity
import com.mobicom.mco.pokus.profile.PrivacyPolicy
import com.mobicom.mco.pokus.profile.TermsConditions



class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<TextView>(R.id.editProfileBtn).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            // Add logout logic if needed
        }

        findViewById<TextView>(R.id.termsOfService).setOnClickListener {
            startActivity(Intent(this, TermsConditions::class.java))
        }

        findViewById<TextView>(R.id.privacyPolicy).setOnClickListener {
            startActivity(Intent(this, PrivacyPolicy::class.java))
        }
    }
}
