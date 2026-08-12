package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PulseXColors
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isButtonPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "btnScale"
    )

    // Infinite floating animation for hero composition
    val infiniteTransition = rememberInfiniteTransition(label = "heroFloat")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background)
    ) {
        // Background subtle glow gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PulseXColors.PrimaryContainer.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // PulseX Header pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(PulseXColors.GlassCardBg)
                    .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.pulsex_logo),
                        contentDescription = "PulseX Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PulseX", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PREMIUM APPLE KEYNOTE HERO COMPOSITION ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background subtle world map / grid effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Draw horizontal and vertical grid lines
                    for (i in 1..5) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(0f, h * i / 6),
                            end = Offset(w, h * i / 6),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Central floating composition container with floatAnim translation
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(300.dp)
                        .graphicsLayer {
                            translationY = floatAnim
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Main Central Glass Platform Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .background(PulseXColors.GlassCardBg)
                            .border(1.5.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(28.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // PulseX Logo centerpiece with glow
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(PulseXColors.PrimaryContainer.copy(alpha = 0.6f))
                                    .border(1.dp, PulseXColors.Primary.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.pulsex_logo),
                                    contentDescription = "PulseX Official Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                );
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Intelligent Financial & News Terminal",
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mini live chart line illustration inside card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val path = Path().apply {
                                        moveTo(0f, h * 0.7f)
                                        lineTo(w * 0.2f, h * 0.4f)
                                        lineTo(w * 0.4f, h * 0.6f)
                                        lineTo(w * 0.6f, h * 0.2f)
                                        lineTo(w * 0.8f, h * 0.3f)
                                        lineTo(w, h * 0.1f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = PulseXColors.PositiveGreen,
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    // --- FLOATING GLASS WIDGET 1: Top Left Crypto Coin Card ---
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-8).dp, y = (-12).dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PulseXColors.GlassCardBg.copy(alpha = 0.95f))
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PulseXColors.Secondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₿", color = PulseXColors.Secondary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("BTC/USD", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("+4.82%", color = PulseXColors.PositiveGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    // --- FLOATING GLASS WIDGET 2: Top Right AI Analytics Card ---
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PulseXColors.GlassCardBg.copy(alpha = 0.95f))
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PulseXColors.AiAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Perplexity AI", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // --- FLOATING GLASS WIDGET 3: Bottom Left Stock Widget ---
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 4.dp, y = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PulseXColors.GlassCardBg.copy(alpha = 0.95f))
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PulseXColors.PositiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("AAPL $234.8", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Global Markets", color = PulseXColors.Outline, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Content
            Text(
                text = "Stay Updated.\nStay Ahead.",
                color = PulseXColors.OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Explore real-time global news, live stock tickers, cryptocurrency charts, and AI-powered financial intelligence — all in one Apple-grade terminal.",
                color = PulseXColors.OnSurfaceVariant,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CTA Button with apple glass gradient
            Button(
                onClick = onGetStartedClick,
                interactionSource = buttonInteractionSource,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), spotColor = PulseXColors.Primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PulseXColors.PrimaryContainer,
                                    PulseXColors.Primary
                                )
                            )
                        )
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
