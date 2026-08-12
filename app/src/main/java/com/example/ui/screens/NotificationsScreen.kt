package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun NotificationsScreen(
    viewModel: PulseXViewModel,
    onNavigateToTarget: (String?, String?) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val prefs = userProfile.preferences

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notifications",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Real-time alerts & financial updates",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                    Text(
                        text = "Mark all read",
                        color = PulseXColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Preferences", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                NotificationToggle(
                    title = "Breaking News",
                    checked = prefs.breakingNewsEnabled,
                    onCheckedChange = { viewModel.updatePreferences(prefs.copy(breakingNewsEnabled = it)) }
                )
                NotificationToggle(
                    title = "Market Alerts",
                    checked = prefs.marketAlertsEnabled,
                    onCheckedChange = { viewModel.updatePreferences(prefs.copy(marketAlertsEnabled = it)) }
                )
                NotificationToggle(
                    title = "Crypto Alerts",
                    checked = prefs.cryptoAlertsEnabled,
                    onCheckedChange = { viewModel.updatePreferences(prefs.copy(cryptoAlertsEnabled = it)) }
                )
                NotificationToggle(
                    title = "General News",
                    checked = prefs.generalNewsEnabled,
                    onCheckedChange = { viewModel.updatePreferences(prefs.copy(generalNewsEnabled = it)) }
                )
            }
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
        }

        itemsIndexed(notifications, key = { _, it -> it.id }) { index, notif ->
            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick {
                        viewModel.markNotificationAsRead(notif.id)
                        onNavigateToTarget(notif.targetType, notif.targetId)
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (notif.isRead) Color.Transparent else PulseXColors.Secondary)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notif.title,
                            color = PulseXColors.OnSurface,
                            fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = notif.timestamp,
                            color = PulseXColors.Outline,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notif.description,
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@Composable
fun NotificationToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = PulseXColors.OnSurface, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PulseXColors.Background,
                checkedTrackColor = PulseXColors.Primary
            )
        )
    }
}
