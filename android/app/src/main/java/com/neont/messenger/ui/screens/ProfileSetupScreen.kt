// Neont Messenger - ProfileSetupScreen.kt
// Layar Setup Profil untuk user baru setelah registrasi / OTP
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.TelegramAvatar

@Composable
fun ProfileSetupScreen(
    initialEmail: String,
    onCompleteSetup: (firstName: String, lastName: String, username: String, bio: String) -> Unit,
    isLoading: Boolean = false
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialEmail.substringBefore("@")) }
    var bio by remember { mutableStateOf("Hey there! I am using Neont.") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramDarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Set up your Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Enter your name and choose a username", color = Color(0xFF7F92A2), fontSize = 14.sp)

            Spacer(Modifier.height(28.dp))

            TelegramAvatar(name = firstName.ifBlank { "User" }, size = 88)

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name (required)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TelegramLightBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name (optional)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TelegramLightBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (@handle)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TelegramLightBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { onCompleteSetup(firstName, lastName, username, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramLightBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = firstName.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Start Messaging", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}
