package com.mobicom.mco.pokus.profile

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val usernameInput = findViewById<EditText>(R.id.inputUsername)
        val schoolInput = findViewById<EditText>(R.id.inputSchool)
        val bioInput = findViewById<EditText>(R.id.inputBio)
        val linkInput = findViewById<EditText>(R.id.inputLink)
        val saveText = findViewById<TextView>(R.id.btnSave)
        val backButton = findViewById<ImageView>(R.id.backEditIcon)

        // Optional: load current data
        usernameInput.setText(MainActivity.currentUsername)
        schoolInput.setText("De La Salle University - Manila") // default or static
        bioInput.setText(MainActivity.currentBio)
        linkInput.setText(MainActivity.currentLink)

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Save action (you can add a proper Save button later)
        saveText.setOnClickListener {
            val newName = usernameInput.text.toString()
            val newBio = bioInput.text.toString()
            val newLink = linkInput.text.toString()

            // Save to SharedPreferences
            val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(MainActivity.KEY_NAME, newName)
                putString(MainActivity.KEY_BIO, newBio)
                putString(MainActivity.KEY_LINK, newLink)
                apply()
            }

            // Update global vars
            MainActivity.currentUsername = newName
            MainActivity.currentBio = newBio
            MainActivity.currentLink = newLink

            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
