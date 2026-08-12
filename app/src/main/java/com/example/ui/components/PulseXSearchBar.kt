package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PulseXSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search news, markets, and crypto",
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val isGlowing = enabled && onClick == null && (isFocused || query.trim().isNotEmpty())

    val glowAlpha by animateFloatAsState(
        targetValue = if (isGlowing) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "glowAlpha"
    )

    val shadowElevation = (10 * glowAlpha).dp
    val glowColor = PulseXColors.Primary.copy(alpha = 0.2f * glowAlpha)

    val borderBrush = if (isGlowing) {
        Brush.linearGradient(
            colors = listOf(
                PulseXColors.Primary.copy(alpha = 0.8f),
                PulseXColors.Primary.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f + (0.03f * glowAlpha)))
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isGlowing) PulseXColors.Primary else PulseXColors.Outline,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (!enabled || onClick != null || query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = PulseXColors.Outline.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }

                if (enabled && onClick == null) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = TextStyle(
                            color = PulseXColors.OnSurface,
                            fontSize = 15.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(PulseXColors.Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                    )
                }
            }

            if (query.isNotEmpty() && onClick == null) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = PulseXColors.Outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Powered",
                    tint = if (isGlowing) PulseXColors.AiAccent else PulseXColors.Outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
