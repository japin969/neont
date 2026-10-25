// Neont Messenger - EditProfileScreen.kt
// Pixel-Perfect Telegram Android Edit Profile Screen
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.User
import com.neont.messenger.ui.components.TelegramAvatar
import com.neont.messenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentUser: User?,
    onSaveProfile: (firstName: String, lastName: String, bio: String, avatarUrl: String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf(currentUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentUser?.lastName ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TelegramPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 12.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                onSaveProfile(firstName.trim(), lastName.trim(), bio.trim(), avatarUrl)
                            },
                            enabled = firstName.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = if (firstName.isNotBlank()) TelegramPrimary else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                )
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Avatar 80dp dengan Kamera Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramSurface)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { /* trigger image picker */ },
                    contentAlignment = Alignment.Center
                ) {
                    TelegramAvatar(
                        name = firstName.ifEmpty { "User" },
                        avatarUrl = avatarUrl,
                        size = 80
                    )

                    // Ikon Kamera Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Set Profile Photo",
                    color = TelegramPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(14.dp))

            // Form Input dengan garis bawah (border-bottom) warna biru
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramSurface)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // First Name
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name (required)", color = TelegramTextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary, // Garis bawah biru
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Last Name
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name (optional)", color = TelegramTextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary, // Garis bawah biru
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Bio
                TextField(
                    value = bio,
                    onValueChange = { if (it.length <= 70) bio = it },
                    label = { Text("Bio", color = TelegramTextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary, // Garis bawah biru
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    supportingText = {
                        Text(
                            text = "${70 - bio.length} characters left",
                            color = TelegramTextSecondary,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "Any details such as age, occupation or city.\nExample: 23 y.o. designer from San Francisco.",
                color = TelegramTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
