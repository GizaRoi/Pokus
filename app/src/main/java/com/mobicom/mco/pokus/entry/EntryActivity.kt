package com.mobicom.mco.pokus.entry // Or your specific package for this activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity // Or androidx.activity.ComponentActivity
// Import your LoginActivity and MainActivity
import com.mobicom.mco.pokus.auth.LoginActivity
import com.mobicom.mco.pokus.home.MainActivity // Assuming MainActivity is in ui.main

@SuppressLint("CustomSplashScreen") // If you're not using the new SplashScreen API yet
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: You can setContentView to a splash screen layout here if desired
        // setContentView(R.layout.activity_splash)

        // Check login status (replace with your actual login check logic)
        if (isUserLoggedIn()) {
            navigateToMainActivity()
        } else {
            navigateToLoginActivity()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        // Implement your logic to check if the user is logged in.
        // This could involve checking:
        // 1. SharedPreferences for a stored token or user ID
        // 2. A local database
        // 3. A Singleton UserManager class
        // Example using SharedPreferences (simplified):
        val sharedPreferences = getSharedPreferences("YourAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false) // Or check for a non-null auth token
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so it's not in the back stack
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so it's not in the back stack
    }
}