package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun ProfileScreen(
    viewModel: PulseXViewModel,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val isGuest = userProfile.uid == "guest"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(2.dp, PulseXColors.Primary, CircleShape)
                ) {
                    AsyncImage(
                        model = userProfile.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (userProfile.isLoggedIn) userProfile.name else "John Doe",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )

                Text(
                    text = if (userProfile.isLoggedIn) userProfile.email else "ahsanali320n@gmail.com",
                    color = PulseXColors.OnSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pro badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PulseXColors.PrimaryGradient)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Pro",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PulseX Pro Member",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Firestore Progress Section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Progress Sync",
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isGuest) Color.White.copy(alpha = 0.08f)
                                        else PulseXColors.PositiveGreen.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isGuest) "Guest Mode" else "Cloud Sync Active",
                                    color = if (isGuest) PulseXColors.OnSurfaceVariant else PulseXColors.PositiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProgressStatItem("Points", "${userProgress.progressPoints} XP")
                            ProgressStatItem("Level", userProgress.levelName)
                            ProgressStatItem("Articles Read", "${userProgress.articlesReadCount}")
                            ProgressStatItem("Streak", "${userProgress.streakDays} Days")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Firestore Persisted Preferences Section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Persisted Preferences",
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isGuest) "Saved Locally" else "Cloud Synced",
                                color = if (isGuest) PulseXColors.OnSurfaceVariant else PulseXColors.PrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Default Currency", color = PulseXColors.OnSurfaceVariant, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PulseXColors.Surface)
                                    .bounceClick {
                                        val newCurrency = if (userPreferences.currency == "USD") "EUR" else "USD"
                                        viewModel.updateUserPreferences(userPreferences.copy(currency = newCurrency))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(userPreferences.currency, color = PulseXColors.PrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Risk Profile", color = PulseXColors.OnSurfaceVariant, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PulseXColors.Surface)
                                    .bounceClick {
                                        val newRisk = when (userPreferences.riskTolerance) {
                                            "Conservative" -> "Moderate"
                                            "Moderate" -> "Aggressive"
                                            else -> "Conservative"
                                        }
                                        viewModel.updateUserPreferences(userPreferences.copy(riskTolerance = newRisk))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(userPreferences.riskTolerance, color = PulseXColors.PositiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cloud Watchlist Sync", color = PulseXColors.OnSurfaceVariant, fontSize = 13.sp)
                            Switch(
                                checked = userPreferences.autoSyncWatchlist,
                                onCheckedChange = { checked ->
                                    viewModel.updateUserPreferences(userPreferences.copy(autoSyncWatchlist = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PulseXColors.PrimaryContainer
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        ProfileMenuItem("My Watchlist", Icons.Default.Bookmark) { onNavigateToWatchlist() }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        ProfileMenuItem("Notifications & Alerts", Icons.Default.Notifications) { onNavigateToNotifications() }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        ProfileMenuItem("Privacy & Security", Icons.Default.Security) { }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.Surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.NegativeRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = PulseXColors.NegativeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            color = PulseXColors.NegativeRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = PulseXColors.PrimaryContainer,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = PulseXColors.OnSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PulseXColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = PulseXColors.OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = PulseXColors.Outline,
            modifier = Modifier.size(16.dp)
        )
    }
}
