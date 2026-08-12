package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun PulseXTopHeader(
    title: String = "PulseX",
    showBack: Boolean = false,
    unreadNotifications: Int = 2,
    isBookmarked: Boolean = false,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onBookmarkClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onLogoClick: (() -> Unit)? = null,
    collapseFraction: Float = 0f
) {
    val verticalPaddingValue = (12 - 8 * collapseFraction).dp
    val horizontalPaddingValue = (16 + 8 * collapseFraction).dp
    val headerHeightValue = (64 - 14 * collapseFraction).dp
    val cornerRadiusValue = (24 - 6 * collapseFraction).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = horizontalPaddingValue, vertical = verticalPaddingValue)
            .height(headerHeightValue)
            .shadow(
                elevation = (6 - 4 * collapseFraction).dp,
                shape = RoundedCornerShape(cornerRadiusValue),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = PulseXColors.Primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadiusValue))
            .background(PulseXColors.GlassCardBg.copy(alpha = 0.75f + 0.15f * collapseFraction))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f - 0.05f * collapseFraction),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadiusValue)
            )
            .padding(horizontal = (12 - 4 * collapseFraction).dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val menuButtonSize = 48.dp
            val menuIconSize = (24 - 2 * collapseFraction).dp

            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(menuButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PulseXColors.OnSurface,
                        modifier = Modifier.size(menuIconSize)
                    )
                }
            } else {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(menuButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = PulseXColors.OnSurface,
                        modifier = Modifier.size(menuIconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.width((8 - 3 * collapseFraction).dp))

            // PulseX Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (onLogoClick != null) {
                            onLogoClick()
                        } else if (showBack) {
                            onBackClick()
                        } else {
                            onProfileClick()
                        }
                    }
            ) {
                val logoSizeValue = (32 - 6 * collapseFraction).dp
                val logoTextSpacing = (10 - 2 * collapseFraction).dp
                val titleFontSizeValue = (18 - 2 * collapseFraction).sp

                Image(
                    painter = painterResource(id = R.drawable.pulsex_logo),
                    contentDescription = "PulseX Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(logoSizeValue)
                        .clip(RoundedCornerShape((8 - 2 * collapseFraction).dp))
                )
                Spacer(modifier = Modifier.width(logoTextSpacing))
                Text(
                    text = title,
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleFontSizeValue,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val actionIconSize = (24 - 2 * collapseFraction).dp

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((4 - 2 * collapseFraction).dp)
            ) {
                if (showBack) {
                    onBookmarkClick?.let { onClick ->
                        IconButton(
                            onClick = onClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) PulseXColors.Primary else PulseXColors.OnSurfaceVariant,
                                modifier = Modifier.size(actionIconSize)
                            )
                        }
                    }
                    onShareClick?.let { onClick ->
                        IconButton(
                            onClick = onClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = PulseXColors.OnSurfaceVariant,
                                modifier = Modifier.size(actionIconSize)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = PulseXColors.OnSurfaceVariant,
                                modifier = Modifier.size(actionIconSize)
                            )
                        }
                        if (unreadNotifications > 0) {
                            val badgeOffset = (12 - 2 * collapseFraction).dp
                            val badgeSize = (8 - 1 * collapseFraction).dp
                            Box(
                                modifier = Modifier
                                    .padding(top = badgeOffset, end = badgeOffset)
                                    .size(badgeSize)
                                    .clip(CircleShape)
                                    .background(PulseXColors.Secondary)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        val profileSizeValue = (32 - 4 * collapseFraction).dp
                        val personIconSizeValue = (18 - 2 * collapseFraction).dp
                        Box(
                            modifier = Modifier
                                .size(profileSizeValue)
                                .clip(CircleShape)
                                .background(PulseXColors.Surface)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = PulseXColors.OnSurfaceVariant,
                                modifier = Modifier.size(personIconSizeValue)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SparkleAiIcon(
    modifier: Modifier = Modifier,
    color: Color = PulseXColors.Primary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Large Sparkle
        val largePath = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            quadraticTo(w * 0.5f, w * 0.5f, w * 0.88f, h * 0.5f)
            quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.88f)
            quadraticTo(w * 0.5f, w * 0.5f, w * 0.12f, h * 0.5f)
            quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.12f)
            close()
        }
        
        // Small Sparkle
        val smallPath = Path().apply {
            moveTo(w * 0.72f, h * 0.18f)
            quadraticTo(w * 0.72f, h * 0.32f, w * 0.86f, h * 0.32f)
            quadraticTo(w * 0.72f, h * 0.32f, w * 0.72f, h * 0.46f)
            quadraticTo(w * 0.72f, h * 0.32f, w * 0.58f, h * 0.32f)
            quadraticTo(w * 0.72f, h * 0.32f, w * 0.72f, h * 0.18f)
            close()
        }
        
        drawPath(largePath, color)
        drawPath(smallPath, color)
    }
}
