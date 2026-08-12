package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

sealed class NavTab(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavTab("home", "Home", Icons.Default.Home)
    object Markets : NavTab("markets", "Markets", Icons.Default.TrendingUp)
    object Search : NavTab("search", "Search", Icons.Default.Search)
    object Crypto : NavTab("crypto", "Crypto", Icons.Default.CurrencyBitcoin)
}

@Composable
fun PulseXBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(NavTab.Home, NavTab.Markets, NavTab.Search, NavTab.Crypto)
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PulseXColors.Surface.copy(alpha = 0.85f),
                            PulseXColors.Background.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) PulseXColors.Primary else PulseXColors.Outline,
                        animationSpec = tween(durationMillis = 250),
                        label = "tabIconTint"
                    )

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tabIconScale"
                    )

                    val pressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "tabPressScale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .scale(pressScale * iconScale)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                    onTabSelected(tab.route)
                                }
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelected,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(PulseXColors.Primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
