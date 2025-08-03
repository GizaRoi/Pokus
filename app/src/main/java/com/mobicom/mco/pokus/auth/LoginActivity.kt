package com.mobicom.mco.pokus.auth // Use your actual package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mobicom.mco.pokus.MainActivity // Replace with your actual main activity
import com.mobicom.mco.pokus.R
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInFallbackLauncher: ActivityResultLauncher<Intent>

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        private const val TAG = "LoginActivity"
        private const val USER_ID_EXTRA = "USER_ID_EXTRA"
        private const val USER_EMAIL_EXTRA = "USER_EMAIL_EXTRA" // If you still need to pass email
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Activity created. Firebase current user: ${firebaseAuth.currentUser?.uid}")
        setContentView(R.layout.activity_login)

        signInButton = findViewById(R.id.signInButton)
        progressBar = findViewById(R.id.progressBar)
        credentialManager = CredentialManager.create(this)

        setupGoogleSignInFallback()

        signInButton.setOnClickListener {
            initiateSignInWithCredentialManager()
        }

         if (firebaseAuth.currentUser != null) {
             Log.d(TAG, "User already signed in. Navigating to main app.")
             navigateToMainApp()
             return // Skip further setup if already signed in
         }
    }

    private fun setupGoogleSignInFallback() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Crucial for Firebase
            .requestEmail() // Request email to have it available
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInFallbackLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            showLoading(false)
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        ?.getResult(ApiException::class.java)
                    if (account?.idToken != null) {
                        Log.d(TAG, "Google Sign-In (Fallback) successful. ID Token present.")
                        signInToFirebaseWithGoogleToken(account.idToken!!, "GoogleSignInFallback")
                    } else {
                        Log.w(TAG, "Google Sign-In (Fallback) - ID Token is null.")
                        Toast.makeText(this, "Google Sign-In failed: Could not get token.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    Log.w(TAG, "Google Sign-In (Fallback) failed: ${e.statusCode}", e)
                    Toast.makeText(this, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.w(TAG, "Google Sign-In (Fallback) UI flow failed or cancelled. Result code: ${result.resultCode}")
                Toast.makeText(this, "Google Sign-In attempt cancelled or failed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initiateSignInWithCredentialManager() {
        showLoading(true)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false) // Ensures UI is shown for account selection
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleCredentialManagerSignInSuccess(result)
            } catch (e: GetCredentialException) {
                handleCredentialManagerSignInFailure(e)
            }
        }
    }

    private fun handleCredentialManagerSignInSuccess(result: GetCredentialResponse) {
        val credential = result.credential
        var idToken: String? = null

        when (credential) {
            is GoogleIdTokenCredential -> {
                Log.d(TAG, "Credential Manager: GoogleIdTokenCredential received.")
                idToken = credential.idToken
            }
            is CustomCredential -> {
                Log.d(TAG, "Credential Manager: CustomCredential received. Type: ${credential.type}")
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "CustomCredential successfully parsed as Google ID Token.")
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse Google ID token from CustomCredential", e)
                        showLoading(false)
                        Toast.makeText(this, "Failed to process Google sign-in.", Toast.LENGTH_LONG).show()
                        return
                    }
                } else {
                    Log.w(TAG, "Received an unhandled CustomCredential type: ${credential.type}")
                    showLoading(false)
                    Toast.makeText(this, "Signed in with an unsupported custom credential.", Toast.LENGTH_LONG).show()
                    return
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type from Credential Manager: ${credential.javaClass.simpleName}")
                showLoading(false)
                Toast.makeText(this, "Sign-in failed: Unexpected credential type.", Toast.LENGTH_LONG).show()
                return
            }
        }

        if (idToken != null) {
            signInToFirebaseWithGoogleToken(idToken, "CredentialManager")
        } else {
            Log.e(TAG, "No ID token extracted from credential after successful Credential Manager flow.")
            showLoading(false)
            Toast.makeText(this, "Sign-in failed: Could not retrieve token.", Toast.LENGTH_LONG).show()
        }
    }

    private fun signInToFirebaseWithGoogleToken(idToken: String, sourceDescription: String) {
        // showLoading(true) is likely already called by the initiator
        val firebaseGoogleCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(firebaseGoogleCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        Log.i(TAG, "Firebase Sign-In SUCCESS (from $sourceDescription). User UID: ${firebaseUser.uid}, Email: ${firebaseUser.email}")
                        checkUserInFirestore(firebaseUser)
                    } else {
                        // This case should be rare if task.isSuccessful is true
                        Log.e(TAG, "Firebase Sign-In (from $sourceDescription) success but currentUser is null!")
                        showLoading(false)
                        Toast.makeText(this, "Sign-in failed: Could not get user session.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e(TAG, "Firebase Sign-In FAILED (from $sourceDescription)", task.exception)
                    showLoading(false)
                    Toast.makeText(this, "Firebase Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserInFirestore(firebaseUser: FirebaseUser) {
        val userUid = firebaseUser.uid // Use UID for document ID
        Log.d(TAG, "Checking Firestore for user document: $userUid")

        firestore.collection("users").document(userUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "User profile already exists in Firestore for $userUid.")
                    navigateToMainApp()
                } else {
                    Log.d(TAG, "No existing user profile for $userUid. Redirecting to sign up.")
                    // Pass UID and email to SignUpActivity
                    navigateToSignUp(userUid, firebaseUser.email)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking user existence for $userUid", e)
                showLoading(false)
                Toast.makeText(this, "Failed to check user data. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleCredentialManagerSignInFailure(e: GetCredentialException) {
        showLoading(false)
        Log.w(TAG, "Credential Manager Sign-in failed: ${e.javaClass.simpleName}", e)

        when (e) {
            is GetCredentialCancellationException -> {
                Toast.makeText(this, "Sign-in cancelled by user.", Toast.LENGTH_SHORT).show()
            }
            is NoCredentialException -> {
                Log.d(TAG, "No credentials found with Credential Manager. Triggering Google Sign-In fallback.")
                Toast.makeText(this, "No saved accounts found. Trying manual sign-in.", Toast.LENGTH_LONG).show()
                triggerGoogleSignInFallback()
            }
            else -> {
                // Generic message for other GetCredentialExceptions
                Toast.makeText(this, "Sign-in error: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun triggerGoogleSignInFallback() {
        showLoading(true)
        Log.d(TAG, "Attempting to sign in with Google (Fallback)...")
        val signInIntent = googleSignInClient.signInIntent
        googleSignInFallbackLauncher.launch(signInIntent)
    }

    private fun navigateToMainApp() {
        showLoading(false)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp(userId: String, userEmail: String?) {
        showLoading(false)
        val intent = Intent(this, SignUpActivity::class.java).apply {
            putExtra(USER_ID_EXTRA, userId)
            userEmail?.let { putExtra(USER_EMAIL_EXTRA, it) }
        }
        Log.d(TAG, "Navigating to SignUpActivity with User ID: $userId, Email: $userEmail")
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        signInButton.isEnabled = !isLoading
    }
}
