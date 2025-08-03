package com.mobicom.mco.pokus.auth // Use your actual package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mobicom.mco.pokus.MainActivity // Replace with your actual main activity
import com.mobicom.mco.pokus.R
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var signInRequest: BeginSignInRequest // If you separate sign-in and sign-up slightly

    private lateinit var credentialManager: CredentialManager

    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar // Assuming you have a ProgressBar with id "progressBar"
    private lateinit var email: String

    companion object {
        private const val TAG = "LoginActivity"
    }

    val db = FirebaseFirestore.getInstance()

    // ActivityResultLauncher for the One Tap UI
    private val oneTapSignInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        Log.d(TAG, "One Tap Google ID Token: $idToken")
                        // TODO: Send this idToken to your backend server for verification
                        // and to create a session or user account.

                        // For now, just log and navigate
                        val displayName = credential.displayName
                        val email = credential.id
                        this.email = email
                        Log.i(TAG, "One Tap Sign-In success: Email: $email, Name: $displayName")
                        Toast.makeText(this, "One Tap Sign-In successful!", Toast.LENGTH_SHORT).show()
                        navigateToMainApp()
                    } else {
                        // Shouldn't happen if the result is OK and it's a Google ID token
                        Log.e(TAG, "One Tap: No ID token or error!")
                        Toast.makeText(this, "Sign-in failed: No ID token.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One Tap UI Canceled.")
                            Toast.makeText(this, "Sign-in cancelled.", Toast.LENGTH_SHORT).show()
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.e(TAG, "One Tap Network error.", e)
                            Toast.makeText(this, "Network error during sign-in.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Log.e(TAG, "One Tap Sign-in failed: ${e.localizedMessage}", e)
                            Toast.makeText(this, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Log.w(TAG, "One Tap UI flow failed with result code: ${result.resultCode}")
                Toast.makeText(this, "Sign-in attempt failed.", Toast.LENGTH_LONG).show()
            }
            showLoading(false)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val auth = FirebaseAuth.getInstance()
        Log.d("Auth", "Firebase initialized. Current user: ${auth.currentUser?.email}")

        setContentView(R.layout.activity_login)

        signInButton = findViewById(R.id.signInButton)
        progressBar = findViewById(R.id.progressBar) // Initialize your ProgressBar

        credentialManager = CredentialManager.create(this)
        oneTapClient = Identity.getSignInClient(this)

        // Prepare the request for Google Sign-In with One Tap (can be done here or in initiateSignIn)
        // This is primarily for the One Tap UI flow.
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false) // Allow any Google account on device
                    .build()
            )
            .setAutoSelectEnabled(true) // Attempt to auto-select if only one account
            .build()

        // You might have a slightly different request if you strictly want to separate sign-in
        // (filterByAuthorizedAccounts = true) from sign-up (filterByAuthorizedAccounts = false)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true) // For returning users
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()


        signInButton.setOnClickListener {
            initiateSignIn() // Or initiateOneTapSignIn()
        }
    }

    /**
     * Initiates Google Sign-In using Credential Manager.
     * This is the more modern approach that integrates with passkeys and other credential types.
     */
    private fun initiateSignIn() {
        showLoading(true)
        // Configure the GetGoogleIdOption for Credential Manager
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Set to true if you only want previously used accounts
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false) // If true & one account, it might auto-sign-in without a prompt.
            // .setNonce("YOUR_NONCE_STRING_IF_NEEDED_FOR_REPLAY_PROTECTION") // Optional
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                handleSignInSuccess(result)
            } catch (e: GetCredentialException) {
                handleSignInFailure(e)
            }
        }
    }

    private fun handleSignInSuccess(result: GetCredentialResponse) {
        showLoading(true) // Show loading indicator
        val credential = result.credential

        when (credential) {
            is GoogleIdTokenCredential -> {
                val googleIdToken = credential.idToken
                Log.d(TAG, "Credential Manager Google ID Token: $googleIdToken")
                this.email = credential.id 
                val firebaseGoogleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                FirebaseAuth.getInstance().signInWithCredential(firebaseGoogleCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            if (firebaseUser != null) {
                                Log.i(TAG, "Firebase Sign-In SUCCESS with Google Credential. User: ${firebaseUser.uid}, Email: ${firebaseUser.email}")
                                this.email = firebaseUser.email ?: credential.id // Prefer Firebase's email if available
                                
                                checkUser(firebaseUser)
                            } else {
                                Log.e(TAG, "Firebase Sign-In success but currentUser is null!")
                                Toast.makeText(this, "Sign-in failed: Could not get user session.", Toast.LENGTH_LONG).show()
                                showLoading(false)
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "Firebase Sign-In FAILED with Google Credential", task.exception)
                            Toast.makeText(this, "Firebase Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            showLoading(false)
                        }
                    }
            }
            is CustomCredential -> {
                Log.d(TAG, "Signed in with a Custom Credential: ${credential.type}")
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idTokenString = googleIdTokenCredential.idToken
                        this.email = googleIdTokenCredential.id
                        val displayName = googleIdTokenCredential.displayName

                        Log.d(TAG, "Custom Credential is Google ID Token. Token: $idTokenString")
                        Log.i(TAG, "CM Sign-In (Custom Google ID Token): Email: ${this.email}, Name: $displayName")
                        
                        val firebaseGoogleCredential = GoogleAuthProvider.getCredential(idTokenString, null)
                        FirebaseAuth.getInstance().signInWithCredential(firebaseGoogleCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    if (firebaseUser != null) {
                                        Log.i(TAG, "Firebase Sign-In SUCCESS (from Custom Cred). User: ${firebaseUser.uid}, Email: ${firebaseUser.email}")
                                        this.email = firebaseUser.email ?: googleIdTokenCredential.id
                                        
                                        checkUser(firebaseUser)
                                    } else {
                                        Log.e(TAG, "Firebase Sign-In success (from Custom Cred) but currentUser is null!")
                                        Toast.makeText(this, "Sign-in failed: Could not get user session.", Toast.LENGTH_LONG).show()
                                        showLoading(false)
                                    }
                                } else {
                                    Log.e(TAG, "Firebase Sign-In FAILED (from Custom Cred)", task.exception)
                                    Toast.makeText(this, "Firebase Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    showLoading(false)
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse Google ID token from CustomCredential", e)
                        Toast.makeText(this, "Failed to process Google sign-in.", Toast.LENGTH_LONG).show()
                        showLoading(false)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing CustomCredential as Google ID Token", e)
                        Toast.makeText(this, "An unexpected error occurred during sign-in.", Toast.LENGTH_LONG).show()
                        showLoading(false)
                    }
                } else {
                    Log.w(TAG, "Received an unhandled CustomCredential type: ${credential.type}")
                    Toast.makeText(this, "Signed in with an unsupported custom credential.", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type from Credential Manager: ${credential.type}")
                Toast.makeText(this, "Sign-in failed: Unexpected credential type.", Toast.LENGTH_LONG).show()
                showLoading(false)
            }
        }
    }

    private fun checkUser(firebaseUser: FirebaseUser) {

        if (!this::email.isInitialized || this.email.isEmpty()) {
            Log.e(TAG, "Email is not available for Firestore check. This shouldn't happen after successful Firebase sign-in.")
            Toast.makeText(this, "Error: Email not found for profile check.", Toast.LENGTH_LONG).show()
            showLoading(false)
            return
        }

        val userDocumentId = this.email

        Log.d(TAG, "Checking Firestore for user document: $userDocumentId. Firebase Auth UID: ${firebaseUser.uid}")

        db.collection("users").document(userDocumentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "User profile already exists in Firestore for $userDocumentId.")
                    navigateToMainApp()
                } else {
                    Log.d(TAG, "No existing user profile for $userDocumentId. Redirecting to sign up.")
                    navigateToSignUp(userDocumentId)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking user existence for $userDocumentId", e)
                Toast.makeText(this, "Failed to check user data. Try again.", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }


    private fun handleSignInFailure(e: GetCredentialException) {
        showLoading(false)
        Log.e(TAG, "Sign-in failed: ${e.message}", e)

        val errorMessage = when (e) {
            is GetCredentialCancellationException -> "Sign-in cancelled by user."
            is NoCredentialException -> "No credentials available for this sign-in request."
            is GetCredentialInterruptedException -> "Sign-in was interrupted."
            is GetCredentialCustomException -> "A custom error occurred: ${e.type}"
            is GetCredentialUnknownException -> "An unknown sign-in error occurred."
            else -> {
                Log.w(TAG, "Unhandled exception type: ${e::class.simpleName}")
                e.localizedMessage ?: "An unexpected error occurred."
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainApp() {
        showLoading(false)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp(emailToUseAsId: String?) {
        showLoading(false)
        val intent = Intent(this, SignUpActivity::class.java)
        if (emailToUseAsId != null) {
            intent.putExtra("USER_EMAIL", emailToUseAsId)
            Log.d(TAG, "Navigating to SignUpActivity with email for ID: $emailToUseAsId")
        } else {
            Log.w(TAG, "Navigating to SignUpActivity but email for ID is null. This is problematic.")
            Toast.makeText(this, "Critical error: Email for profile ID is missing.", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        signInButton.isEnabled = !isLoading
    }
}
