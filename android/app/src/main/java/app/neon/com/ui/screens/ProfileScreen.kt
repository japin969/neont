// Neont Messenger - ProfileScreen.kt
// Pixel-Perfect Reverse Engineering of Telegram Android Profile Screen
// Dioptimasi menggunakan LazyColumn untuk performa anti-lag & zero frame drops
package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import app.neon.com.data.model.User
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
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
        // LazyColumn untuk performa scroll yang mulus (Anti-Lag khas Telegram Android)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Foto Profil Full-Width 200dp di bagian atas
            item(key = "profile_header_photo") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    TelegramPrimary,
                                    Color(0xFF1B3B5C),
                                    TelegramSurface
                                )
                            )
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.firstName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(TelegramPrimary.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.firstName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Gradient overlay at bottom of photo for text legibility
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xCC17212B))
                                )
                            )
                    )

                    // Nama & Nomor HP overlay di foto
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "Neont User" },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = user.phoneNumber.ifEmpty { "+62 812 3456 7890" },
                            color = TelegramTextSecondary, // #8B9DAE
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Bagian Info Akun (Bio, Username)
            item(key = "profile_account_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramSurface)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Account",
                        color = TelegramPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "@${user.username.ifEmpty { "user" }}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Username",
                        color = TelegramTextSecondary,
                        fontSize = 12.sp
                    )

                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = user.bio.ifEmpty { "Hey there! I am using Neont." },
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "Bio",
                        color = TelegramTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            // Header Daftar Menu Pengaturan
            item(key = "profile_settings_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramSurface)
                        .padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
                ) {
                    Text(
                        text = "Settings",
                        color = TelegramPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Daftar Menu Pengaturan dengan ripple efek
            item(key = "menu_notifications") {
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications and Sounds",
                    onClick = {}
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
            }

            item(key = "menu_privacy") {
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy and Security",
                    onClick = {}
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
            }

            item(key = "menu_storage") {
                ProfileMenuItem(
                    icon = Icons.Default.PieChart,
                    title = "Data and Storage",
                    onClick = {}
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
            }

            item(key = "menu_chat_settings") {
                ProfileMenuItem(
                    icon = Icons.Default.Chat,
                    title = "Chat Settings",
                    onClick = {}
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
            }

            item(key = "menu_devices") {
                ProfileMenuItem(
                    icon = Icons.Default.Devices,
                    title = "Devices",
                    onClick = {}
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
            }

            item(key = "menu_language") {
                ProfileMenuItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    onClick = {}
                )
            }

            item(key = "profile_bottom_spacer") {
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelegramSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(20.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        // Chevron di kanan
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
