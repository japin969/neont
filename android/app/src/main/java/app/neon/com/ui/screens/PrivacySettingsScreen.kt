// Neont Messenger - PrivacySettingsScreen.kt
// Pixel-Perfect Telegram Android Privacy and Security Settings
// Fitur: Two-Step Verification (Password Tambahan) & Auto-Delete Messages Timer (settings.json)
package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import app.neon.com.data.file.FileDatabaseManager
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.PrivacySettings
import app.neon.com.ui.theme.TelegramDarkBG
import app.neon.com.ui.theme.TelegramDivider
import app.neon.com.ui.theme.TelegramPrimary
import app.neon.com.ui.theme.TelegramSurface
import app.neon.com.ui.theme.TelegramTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBackClick: () -> Unit,
    fileDb: FileDatabaseManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeDb = remember(fileDb, context) {
        fileDb ?: FileDatabaseManager(context, Gson())
    }

    var appSettings by remember { mutableStateOf(AppSettings()) }
    var showTwoStepDialog by remember { mutableStateOf(false) }
    var twoStepInputPassword by remember { mutableStateOf("") }
    var showAutoDeleteDialog by remember { mutableStateOf(false) }

    val autoDeleteOptions = listOf("Off", "24 Hours", "7 Days", "1 Month")

    LaunchedEffect(Unit) {
        val loaded = activeDb.readSettings()
        appSettings = loaded
        twoStepInputPassword = loaded.privacy.twoStepPassword
    }

    fun savePrivacy(updatedPrivacy: PrivacySettings) {
        val updated = appSettings.copy(privacy = updatedPrivacy)
        appSettings = updated
        scope.launch {
            activeDb.writeSettings(updated)
        }
    }

    // Dialog Two-Step Verification
    if (showTwoStepDialog) {
        AlertDialog(
            onDismissRequest = { showTwoStepDialog = false },
            containerColor = TelegramSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TelegramPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Two-Step Verification", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Set an additional password that will be required when logging into your Neont account.",
                        color = TelegramTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = twoStepInputPassword,
                        onValueChange = { twoStepInputPassword = it },
                        label = { Text("Additional Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramPrimary,
                            unfocusedBorderColor = TelegramDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = TelegramPrimary,
                            unfocusedLabelColor = TelegramTextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isEnabled = twoStepInputPassword.isNotBlank()
                        savePrivacy(appSettings.privacy.copy(
                            twoStepVerificationEnabled = isEnabled,
                            twoStepPassword = twoStepInputPassword
                        ))
                        showTwoStepDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTwoStepDialog = false }) {
                    Text("Cancel", color = TelegramTextSecondary)
                }
            }
        )
    }

    // Dialog Auto-Delete Messages Timer
    if (showAutoDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showAutoDeleteDialog = false },
            containerColor = TelegramSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = TelegramPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Auto-Delete Messages", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Automatically delete newly sent messages for both participants after a set period.",
                        color = TelegramTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    autoDeleteOptions.forEach { option ->
                        val selected = appSettings.privacy.autoDeleteTimer == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    savePrivacy(appSettings.privacy.copy(autoDeleteTimer = option))
                                    showAutoDeleteDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    savePrivacy(appSettings.privacy.copy(autoDeleteTimer = option))
                                    showAutoDeleteDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TelegramPrimary,
                                    unselectedColor = TelegramTextSecondary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = option, color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAutoDeleteDialog = false }) {
                    Text("Close", color = TelegramTextSecondary)
                }
            }
        )
    }

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
                title = { Text("Privacy and Security", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "SECURITY",
                color = TelegramPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Column(modifier = Modifier.background(TelegramSurface)) {
                // Two-Step Verification
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTwoStepDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Two-Step Verification", color = Color.White, fontSize = 16.sp)
                        Text(
                            text = if (appSettings.privacy.twoStepVerificationEnabled) "Enabled (Additional password active)" else "Disabled",
                            color = if (appSettings.privacy.twoStepVerificationEnabled) TelegramPrimary else TelegramTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = appSettings.privacy.twoStepVerificationEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                showTwoStepDialog = true
                            } else {
                                savePrivacy(appSettings.privacy.copy(twoStepVerificationEnabled = false, twoStepPassword = ""))
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TelegramPrimary)
                    )
                }

                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))

                // Auto-Delete Messages Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAutoDeleteDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Delete Messages", color = Color.White, fontSize = 16.sp)
                        Text(text = "Automatically clear messages in chats", color = TelegramTextSecondary, fontSize = 13.sp)
                    }
                    Text(
                        text = appSettings.privacy.autoDeleteTimer,
                        color = TelegramPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))

                // Passcode Lock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Passcode Lock", color = Color.White, fontSize = 16.sp)
                        Text(text = "Lock app when inactive", color = TelegramTextSecondary, fontSize = 13.sp)
                    }
                    Switch(
                        checked = appSettings.privacy.passcodeLockEnabled,
                        onCheckedChange = { isChecked ->
                            savePrivacy(appSettings.privacy.copy(passcodeLockEnabled = isChecked))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TelegramPrimary)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "PRIVACY",
                color = TelegramPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Column(modifier = Modifier.background(TelegramSurface)) {
                PrivacyOptionRow(
                    title = "Phone Number",
                    value = appSettings.privacy.phoneNumberVisibility,
                    onClick = {
                        val next = if (appSettings.privacy.phoneNumberVisibility == "My Contacts") "Nobody" else "My Contacts"
                        savePrivacy(appSettings.privacy.copy(phoneNumberVisibility = next))
                    }
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))

                PrivacyOptionRow(
                    title = "Last Seen & Online",
                    value = appSettings.privacy.lastSeenVisibility,
                    onClick = {
                        val next = if (appSettings.privacy.lastSeenVisibility == "My Contacts") "Nobody" else "My Contacts"
                        savePrivacy(appSettings.privacy.copy(lastSeenVisibility = next))
                    }
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))

                PrivacyOptionRow(
                    title = "Profile Photos",
                    value = appSettings.privacy.profilePhotoVisibility,
                    onClick = {
                        val next = if (appSettings.privacy.profilePhotoVisibility == "Everybody") "My Contacts" else "Everybody"
                        savePrivacy(appSettings.privacy.copy(profilePhotoVisibility = next))
                    }
                )
                Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))

                PrivacyOptionRow(
                    title = "Peer-to-Peer Calls",
                    value = appSettings.privacy.callsP2P,
                    onClick = {
                        val next = if (appSettings.privacy.callsP2P == "Everybody") "My Contacts" else "Everybody"
                        savePrivacy(appSettings.privacy.copy(callsP2P = next))
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacyOptionRow(
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Text(text = value, color = TelegramPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
