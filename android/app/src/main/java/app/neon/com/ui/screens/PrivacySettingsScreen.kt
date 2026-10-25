package app.neon.com.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.PrivacySettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var privacy by remember { mutableStateOf(settings.privacy) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy and Security", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                Text("SECURITY", color = TelegramPrimary, fontSize = 13.sp, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                SettingsToggleRow("Two-Step Verification", privacy.twoStepVerificationEnabled) {
                    privacy = privacy.copy(twoStepVerificationEnabled = it)
                    onSaveSettings(settings.copy(privacy = privacy))
                }
            }
            item {
                SettingsToggleRow("Passcode Lock", privacy.passcodeLockEnabled) {
                    privacy = privacy.copy(passcodeLockEnabled = it)
                    onSaveSettings(settings.copy(privacy = privacy))
                }
            }
            item { Divider(color = TelegramDarkDivider) }
            
            item {
                Text("PRIVACY", color = TelegramPrimary, fontSize = 13.sp, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                SettingsToggleRow("Phone Number", privacy.phoneNumberVisibility == "Everybody") {
                    privacy = privacy.copy(phoneNumberVisibility = if (it) "Everybody" else "My Contacts")
                    onSaveSettings(settings.copy(privacy = privacy))
                }
            }
            item {
                SettingsToggleRow("Last Seen", privacy.lastSeenVisibility == "Everybody") {
                    privacy = privacy.copy(lastSeenVisibility = if (it) "Everybody" else "My Contacts")
                    onSaveSettings(settings.copy(privacy = privacy))
                }
            }
            item {
                SettingsToggleRow("Profile Photo", privacy.profilePhotoVisibility == "Everybody") {
                    privacy = privacy.copy(profilePhotoVisibility = if (it) "Everybody" else "My Contacts")
                    onSaveSettings(settings.copy(privacy = privacy))
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
