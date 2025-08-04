package com.mobicom.mco.pokus.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.databinding.ActivitySignupBinding
import com.mobicom.mco.pokus.models.User
import kotlin.text.isEmpty
import kotlin.text.trim

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var documentIdForUser: String? = null // This will store the email to be used as ID
    private val passedEmail: String? get() = intent.getStringExtra(LoginActivity.USER_EMAIL_EXTRA) // Retrieve email from Intent

    companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (!passedEmail.isNullOrEmpty()) {
            documentIdForUser = passedEmail
            Log.d(TAG, "Using passed email as document ID: $documentIdForUser")
            // Optionally, pre-fill an email field in your UI and disable it
            // binding.emailFiel.setText(documentIdForUser)
            // binding.emailFiel.isEnabled = false
        } else {
            Log.e(TAG, "Critical: Email not passed from LoginActivity or is empty. Cannot create user profile with email as ID.")
            Toast.makeText(this, "Error: User email not found. Cannot complete profile.", Toast.LENGTH_LONG).show()
            // It's crucial to handle this. You might want to finish the activity
            // or redirect back to login if the email is mandatory for the ID.
            finish() // Example: Finish if email is absolutely required
            return
        }

        binding.createAccountBtn.setOnClickListener {
            validateAndCreateUser()
        }
    }

    private fun validateAndCreateUser() {
        // Ensure documentIdForUser (the email) is set. This check is also in onCreate but good for robustness.
        if (documentIdForUser == null) {
            Log.e(TAG, "User document ID (email) is null. Cannot proceed.")
            Toast.makeText(this, "An error occurred. Please try signing in again.", Toast.LENGTH_LONG).show()
            return
        }

        val username = binding.usernameField.text.toString().trim()
        val bio = binding.bioField.text.toString().trim()
        val school = binding.schoolField.text.toString().trim()
        val links = binding.linksField.text.toString().trim()

        if (username.isEmpty()) {
            binding.usernameField.error = "Username is required"
            binding.usernameField.requestFocus()
            return
        }
        if (school.isEmpty()) {
            binding.schoolField.error = "School is required"
            binding.schoolField.requestFocus()
            return
        }

        // Get the currently authenticated Firebase User to associate with this profile.
        // Even if the document ID is the email, you still have an authenticated FirebaseUser.
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            Toast.makeText(this, "No authenticated user found. Please sign in again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "FirebaseUser is null during sign up attempt.")
            return
        }

        // Create User Object
        // The 'uid' field in the User model might now store the Auth UID for reference,
        // even though the document ID is the email. Or you might omit it if you solely rely on email.
        val newUser = User(
            id = passedEmail.toString(),
            username = username,
            pfpURL = null,
            bio = bio,
            school = school,
            links = links
        )

        Log.d(TAG, "User object created: $newUser. Will use '$documentIdForUser' as document ID.")
        saveUserToFirestore(newUser, documentIdForUser!!) // Pass the email as the document ID
    }

    private fun saveUserToFirestore(user: User, userDocumentId: String) {
        firestore.collection("users").document(userDocumentId) // Use the passed email as document ID
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User profile created successfully in Firestore with document ID: $userDocumentId")
                Toast.makeText(this, "Welcome, ${user.username}! Profile created.", Toast.LENGTH_LONG).show()
                navigateToMainApp()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user profile in Firestore with document ID: $userDocumentId", e)
                Toast.makeText(this, "Failed to create profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
