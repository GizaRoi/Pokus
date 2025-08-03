package com.mobicom.mco.pokus.profile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val name = findViewById<EditText>(R.id.editName)
        val bio = findViewById<EditText>(R.id.editBio)
        val link = findViewById<EditText>(R.id.editLink)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        // Pre-fill with current values
        name.setText(MainActivity.currentUsername)
        bio.setText(MainActivity.currentBio)
        link.setText(MainActivity.currentLink)

        saveBtn.setOnClickListener {
            val newName = name.text.toString()
            val newBio = bio.text.toString()
            val newLink = link.text.toString()

            // Save to SharedPreferences
            val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(MainActivity.KEY_NAME, newName)
                putString(MainActivity.KEY_BIO, newBio)
                putString(MainActivity.KEY_LINK, newLink)
                apply()
            }

            // Also update MainActivity variables
            MainActivity.currentUsername = newName
            MainActivity.currentBio = newBio
            MainActivity.currentLink = newLink

            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
