package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PulseXColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onNavigateNext: (String) -> Unit
) {
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0f) }
    val currentIsLoggedIn by rememberUpdatedState(isLoggedIn)

    LaunchedEffect(Unit) {
        // Animate scale and alpha in parallel
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(1200)
        if (currentIsLoggedIn) {
            onNavigateNext("home")
        } else {
            onNavigateNext("welcome")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(PulseXColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .graphicsLayer { this.alpha = alpha.value }
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(PulseXColors.GlassCardBg)
                    .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(28.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pulsex_logo),
                    contentDescription = "PulseX Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PULSEX",
                color = PulseXColors.OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Financial Intelligence & Markets",
                color = PulseXColors.OnSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PulseXColors.Primary,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
        }
    }
}

