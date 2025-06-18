package com.mobicom.mco.pokus.auth // Replace with your actual package name

import android.os.Bundle
//import android.widget.Toast
import androidx.activity.ComponentActivity // Import this
import androidx.activity.compose.setContent // Import this
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // For previewing

class LoginActivity : ComponentActivity() { // Inherit from ComponentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { // Use setContent for Compose UI
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // In a real app, you'd likely use a ViewModel to hold this state and logic
    // For simplicity in this example, we'll keep it in the Composable.
    // val context = LocalContext.current // If you need context for Toast inside Composable

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    // In a real app, you would send these credentials to a server
                    // For this example, we'll just simulate a login attempt
                    println("Attempting login with: $username") // Use println or a proper logger

                    // TODO: Implement actual authentication logic here
                    // e.g., Call an API, check against a local database, etc.
                    // On successful login, navigate to the next activity.
                    // On failure, show an error message.

                    // Example: Show a Toast (if you need context, pass it or use LocalContext)
                    // Toast.makeText(context, "Attempting login with: $username", Toast.LENGTH_SHORT).show()

                } else {
                    println("Please enter both username and password")
                    // Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

// Optional: Preview your Composable
@Preview(showBackground = true)
@Composable
fun DefaultPreviewOfLoginScreen() {
    // You might want to wrap this in your app's theme if you have one
    LoginScreen()
}