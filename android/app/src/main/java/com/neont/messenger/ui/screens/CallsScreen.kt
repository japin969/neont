// Neont Messenger - CallsScreen.kt
// Layar Log Panggilan Telegram Android membaca data dari calls.json
package com.neont.messenger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.CallLog
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkDivider
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.TelegramAvatar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    calls: List<CallLog>,
    onCallClick: (CallLog) -> Unit,
    onNewCallClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calls", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewCallClick,
                containerColor = TelegramLightBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.AddCall, contentDescription = "New Call")
            }
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(calls, key = { it.callId }) { call ->
                val timeFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(call.timestamp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallClick(call) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelegramAvatar(name = call.userName, avatarUrl = call.userAvatar, size = 48)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = call.userName,
                            color = if (call.type == "missed") Color.Red else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (call.type) {
                                    "incoming" -> Icons.Default.CallReceived
                                    "missed" -> Icons.Default.CallMissed
                                    else -> Icons.Default.CallMade
                                },
                                contentDescription = null,
                                tint = if (call.type == "missed") Color.Red else Color(0xFF4FA0FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(timeFormatted, color = Color(0xFF7F92A2), fontSize = 13.sp)
                        }
                    }
                    IconButton(onClick = { onCallClick(call) }) {
                        Icon(
                            imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = TelegramLightBlue
                        )
                    }
                }
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 78.dp))
            }
        }
    }
}
