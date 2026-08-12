package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun PulseXDrawerSheet(
    currentRoute: String,
    userProfile: UserProfile,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = PulseXColors.Background,
        drawerContentColor = PulseXColors.OnSurface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Header Logo & User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate("profile")
                        onCloseDrawer()
                    }
                    .padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pulsex_logo),
                    contentDescription = "PulseX Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (userProfile.isLoggedIn) userProfile.name else "PulseX User",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (userProfile.isLoggedIn) userProfile.email else "Guest Mode",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            DrawerMenuItem("Home", Icons.Default.Home, currentRoute == "home") {
                onNavigate("home")
                onCloseDrawer()
            }
            DrawerMenuItem("Markets", Icons.Default.TrendingUp, currentRoute == "markets") {
                onNavigate("markets")
                onCloseDrawer()
            }
            DrawerMenuItem("Search", Icons.Default.Search, currentRoute == "search") {
                onNavigate("search")
                onCloseDrawer()
            }
            DrawerMenuItem("Crypto", Icons.Default.CurrencyBitcoin, currentRoute == "crypto") {
                onNavigate("crypto")
                onCloseDrawer()
            }
            DrawerMenuItem("News", Icons.Default.Newspaper, currentRoute == "news") {
                onNavigate("news")
                onCloseDrawer()
            }
            DrawerMenuItem("Watchlist", Icons.Default.Bookmark, currentRoute == "watchlist") {
                onNavigate("watchlist")
                onCloseDrawer()
            }
            DrawerMenuItem("Notifications", Icons.Default.Notifications, currentRoute == "notifications") {
                onNavigate("notifications")
                onCloseDrawer()
            }
            DrawerMenuItem("Profile", Icons.Default.Person, currentRoute == "profile") {
                onNavigate("profile")
                onCloseDrawer()
            }

            Spacer(modifier = Modifier.weight(1f))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            if (userProfile.isLoggedIn) {
                DrawerMenuItem("Sign Out", Icons.AutoMirrored.Filled.Logout, false) {
                    onLogout()
                    onCloseDrawer()
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, color = if (selected) PulseXColors.Primary else PulseXColors.OnSurface) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label, tint = if (selected) PulseXColors.Primary else PulseXColors.Outline) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PulseXColors.PrimaryContainer.copy(alpha = 0.2f),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
