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
import com.mobicom.mco.pokus.home.MainActivity // Replace with your actual main activity
import com.mobicom.mco.pokus.R
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException


class LoginActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var signInRequest: BeginSignInRequest // If you separate sign-in and sign-up slightly

    private lateinit var credentialManager: CredentialManager

    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar // Assuming you have a ProgressBar with id "progressBar"

    companion object {
        private const val TAG = "LoginActivity"
    }

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
            initiateGoogleSignInWithCredentialManager() // Or initiateOneTapSignIn()
        }
    }

    /**
     * Initiates Google Sign-In using Credential Manager.
     * This is the more modern approach that integrates with passkeys and other credential types.
     */
    private fun initiateGoogleSignInWithCredentialManager() {
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
                handleSignInSuccessWithCredentialManager(result)
            } catch (e: GetCredentialException) {
                handleSignInFailure(e)
            }
        }
    }


    private fun handleSignInSuccessWithCredentialManager(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is GoogleIdTokenCredential -> {
                val googleIdToken = credential.idToken
                Log.d(TAG, "Credential Manager Google ID Token: $googleIdToken")

                // TODO: Send this token to your backend for verification and further processing
                // For now, let's just log it and navigate

                try {
                    val parsedToken = GoogleIdTokenCredential.createFrom(credential.data)
                    Log.i(TAG, "CM Sign-In success: Display Name: ${parsedToken.displayName}, Email: ${parsedToken.id}")
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Error parsing Google ID Token from Credential Manager: ${e.message}", e)
                }

                Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show()
                navigateToMainApp()
            }
            is CustomCredential -> {
                Log.d(TAG, "Signed in with a Custom Credential: ${credential.type}")
                // Handle other credential types like passkeys if you've configured them
                Toast.makeText(this, "Signed in with a custom credential.", Toast.LENGTH_SHORT).show()
                navigateToMainApp()
                // You might need to navigate or perform other actions based on the custom credential type
            }
            else -> {
                Log.e(TAG, "Unexpected credential type from Credential Manager: ${credential.type}")
                Toast.makeText(this, "Sign-in failed: Unexpected credential type.", Toast.LENGTH_LONG).show()
            }
        }
        showLoading(false)
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


    /**
     * Initiates Google Sign-In using the older One Tap client.
     * This can be a fallback or used if you prefer the One Tap UI directly.
     */
    private fun initiateOneTapSignIn() {
        showLoading(true)
        // Using signUpRequest here to be more inclusive for first-time users.
        // You could use signInRequest if you are sure the user has signed in before.
        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapSignInResultLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}", e)
                    Toast.makeText(this, "Could not start sign-in process.", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "Google One Tap beginSignIn() failed: ${e.localizedMessage}", e)
                // Common errors:
                // - Developer error (misconfiguration of client ID, SHA1, API not enabled)
                // - No network
                // - No eligible accounts and not falling back gracefully
                if (e is ApiException) {
                    if (e.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                        Toast.makeText(this, "Sign-in configuration error. Check Logcat.", Toast.LENGTH_LONG).show()
                    } else if (e.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                        Toast.makeText(this, "Network error. Please check connection.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Could not initiate sign-in: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Could not initiate sign-in: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                showLoading(false)
            }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        signInButton.isEnabled = !isLoading
    }

    // Optional: Sign out (clears Credential Manager state for your app)
    private fun signOut() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                Log.d(TAG, "Sign out successful from Credential Manager.")
                Toast.makeText(this@LoginActivity, "Signed out.", Toast.LENGTH_SHORT).show()
                // TODO: Also clear your app's local session, tokens, etc.
                // And potentially sign out from OneTapClient if you want to clear its state too
                // oneTapClient.signOut()
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Credential Manager Sign out failed: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Sign out failed.", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
}
