package com.mobicom.mco.pokus.profile

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import android.util.Log
import com.mobicom.mco.pokus.auth.LoginActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var linkEditText: EditText
    private lateinit var saveBtn: Button
    private lateinit var profileImageView: ImageView // To display the selected drawable
    private lateinit var changeProfilePicTextView: TextView // To trigger the selection
    private lateinit var logOutBtn: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // List of your drawable resource names (without extension)
    private val animalIconResourceNames = listOf(
        "astronaut", "bear", "cat", "chicken",
        "dog", "gorilla", "panda", "rabbit"
    )
    private var selectedDrawableName: String? = null // Will store "ic_animal_lion", etc.
    private val defaultDrawableName = "ic_default_profile" // Your default profile icon name

    companion object {
        private const val TAG = "EditProfileActivity"
        const val FIRESTORE_PFP_FIELD = "pfpURL" // Field name in Firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile) // Ensure this layout has the new ImageView and TextView

        nameEditText = findViewById(R.id.inputUsername)
        bioEditText = findViewById(R.id.inputBio)
        linkEditText = findViewById(R.id.inputLink)
        saveBtn = findViewById(R.id.saveBtn)
        profileImageView = findViewById(R.id.profilePic) // Your ImageView for the profile picture
        changeProfilePicTextView = findViewById(R.id.changePhoto) // Your TextView/Button to change PFP
        logOutBtn = findViewById(R.id.logoutBtn)


        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadCurrentProfileData()

        changeProfilePicTextView.setOnClickListener {
            showAnimalIconSelectionDialog()
        }

        saveBtn.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            val newBio = bioEditText.text.toString().trim()
            val newLink = linkEditText.text.toString().trim()

            if (newName.isEmpty()) {
                nameEditText.error = "Name cannot be empty"
                return@setOnClickListener
            }
            // `selectedDrawableName` will be used if a new icon was chosen,
            // otherwise, the existing one (or default) will be kept or resaved.
            updateUserProfileInFirestore(newName, newBio, newLink, selectedDrawableName)
        }

        logOutBtn.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish() // Close this activity
            //Start LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun getDrawableIdByName(drawableName: String): Int {
        return try {
            resources.getIdentifier(drawableName, "drawable", packageName)
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Drawable resource not found: $drawableName", e)
            resources.getIdentifier(defaultDrawableName, "drawable", packageName) // Fallback
        }
    }

    private fun loadCurrentProfileData() {
        val userId = firebaseAuth.currentUser?.email
        if (userId == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: MainActivity.currentUsername
                    val bio = document.getString("bio") ?: MainActivity.currentBio
                    val link = document.getString("link") ?: MainActivity.currentLink
                    val pfpDrawableName = document.getString(FIRESTORE_PFP_FIELD) ?: defaultDrawableName

                    nameEditText.setText(username)
                    bioEditText.setText(bio)
                    linkEditText.setText(link)

                    selectedDrawableName = pfpDrawableName // Initialize with current selection
                    val drawableId = getDrawableIdByName(pfpDrawableName)
                    if (drawableId != 0) {
                        profileImageView.setImageResource(drawableId)
                    } else {
                        profileImageView.setImageResource(getDrawableIdByName(defaultDrawableName))
                    }

                } else {
                    // Load from local cache or defaults if no Firestore document
                    nameEditText.setText(MainActivity.currentUsername)
                    bioEditText.setText(MainActivity.currentBio)
                    linkEditText.setText(MainActivity.currentLink)
                    val drawableId = getDrawableIdByName(selectedDrawableName ?: defaultDrawableName)
                    if (drawableId != 0) {
                        profileImageView.setImageResource(drawableId)
                    } else {
                        profileImageView.setImageResource(getDrawableIdByName(defaultDrawableName))
                    }
                    Toast.makeText(this, "Profile data not found, using local cache.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error loading profile from Firestore", e)
                // Fallback to local cache or defaults
                nameEditText.setText(MainActivity.currentUsername)
                bioEditText.setText(MainActivity.currentBio)
                linkEditText.setText(MainActivity.currentLink)
                val drawableId = getDrawableIdByName(selectedDrawableName ?: defaultDrawableName)
                if (drawableId != 0) {
                    profileImageView.setImageResource(drawableId)
                } else {
                    profileImageView.setImageResource(getDrawableIdByName(defaultDrawableName))
                }
            }

    }

    private fun showAnimalIconSelectionDialog() {
        // A simple dialog for selection. You can replace this with a more sophisticated UI.
        val displayNames = animalIconResourceNames.map { it.replace("ic_animal_", "").replace("_", " ").capitalizeWords() }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Profile Icon")
            .setItems(displayNames) { dialog, which ->
                selectedDrawableName = animalIconResourceNames[which]
                val drawableId = getDrawableIdByName(selectedDrawableName!!)
                if (drawableId != 0) {
                    profileImageView.setImageResource(drawableId)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Helper to capitalize words for display names
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }


    private fun updateUserProfileInFirestore(name: String, bio: String, link: String, pfpDrawableName: String?) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_LONG).show()
            return
        }
        val userId = currentUser.email

        val userProfileUpdates = hashMapOf<String, Any?>(
            "username" to name,
            "bio" to bio,
            "link" to link
        )
        // Only include pfpDrawableName if it's not null (i.e., it was loaded or selected)
        // If it's null and was never loaded, it means we might want to keep the existing one or set a default.
        // For simplicity, we'll save it if available. If it was never selected and not loaded,
        // it might save as null, so ensure loadCurrentProfileData handles null by using default.
        // A better approach might be to ensure pfpDrawableName is always non-null here,
        // defaulting to the current value or defaultDrawableName if no new selection was made.


        if (!isFinishing && !isDestroyed) {
            Toast.makeText(this, "Updating profile...", Toast.LENGTH_SHORT).show()
        }

        if (userId != null) {
            firestore.collection("users").document(userId)
                .set(userProfileUpdates, com.google.firebase.firestore.SetOptions.merge()) // Use merge to avoid overwriting other fields
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()

                    MainActivity.currentUsername = name
                    MainActivity.currentBio = bio
                    MainActivity.currentLink = link

                    val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString(MainActivity.KEY_NAME, name)
                        putString(MainActivity.KEY_BIO, bio)
                        putString(MainActivity.KEY_LINK, link)
                        apply()
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error updating profile in Firestore", e)
                }
        }
    }
}
