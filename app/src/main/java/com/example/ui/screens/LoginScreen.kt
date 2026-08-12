package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PulseXColors

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import com.example.ui.viewmodel.PulseXViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.systemBarsPadding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.components.StaggeredEnter
import com.example.ui.components.FadeInEnter
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun LoginScreen(
    viewModel: PulseXViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val haptic = LocalHapticFeedback.current

    // Interaction sources for press transitions
    val submitButtonInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitButtonInteractionSource.collectIsPressedAsState()
    val submitButtonScale by animateFloatAsState(
        targetValue = if (isSubmitPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "submitBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Header
            FadeInEnter(delayMillis = 100) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.pulsex_logo),
                        contentDescription = "PulseX Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PulseX", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            StaggeredEnter(delayMillis = 200) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isSignUpMode) "Create Account" else "Welcome Back",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSignUpMode)
                            "Create an account to store your profile and sync your progress."
                        else
                            "Sign in to access your personalized financial dashboard and sync your progress.",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Banner
            if (authError != null) {
                StaggeredEnter(delayMillis = 250) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PulseXColors.NegativeRed.copy(alpha = 0.12f))
                            .border(1.dp, PulseXColors.NegativeRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = authError ?: "",
                            color = PulseXColors.NegativeRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Name Input (Only on Sign Up Mode)
            if (isSignUpMode) {
                StaggeredEnter(delayMillis = 300) {
                    GlassTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearAuthError()
                        },
                        placeholder = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email Input
            StaggeredEnter(delayMillis = 350) {
                GlassTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearAuthError()
                    },
                    placeholder = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            StaggeredEnter(delayMillis = 400) {
                GlassTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearAuthError()
                    },
                    placeholder = "Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            if (!isSignUpMode) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    val forgotInteractionSource = remember { MutableInteractionSource() }
                    val isForgotPressed by forgotInteractionSource.collectIsPressedAsState()
                    val forgotAlpha by animateFloatAsState(
                        targetValue = if (isForgotPressed) 0.5f else 1f,
                        animationSpec = tween(150),
                        label = "forgotAlpha"
                    )
                    Text(
                        text = "Forgot Password?",
                        color = PulseXColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .graphicsLayer { alpha = forgotAlpha }
                            .clickable(
                                interactionSource = forgotInteractionSource,
                                indication = null,
                                onClick = {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button (Firebase Auth & Firestore Sync)
            StaggeredEnter(delayMillis = 450) {
                Button(
                    enabled = !isAuthLoading,
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (_: Exception) {}
                        if (isSignUpMode) {
                            viewModel.signUpWithFirebase(
                                email = email,
                                password = password,
                                name = name,
                                onSuccess = onLoginSuccess
                            )
                        } else {
                            viewModel.loginWithFirebase(
                                email = email,
                                password = password,
                                onSuccess = onLoginSuccess
                            )
                        }
                    },
                    interactionSource = submitButtonInteractionSource,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(submitButtonScale)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = PulseXColors.Primary,
                            clip = false
                        )
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
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create Account" else "Sign In",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Submit",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            FadeInEnter(delayMillis = 500) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
                    Text(
                        text = "Or continue with guest access",
                        color = PulseXColors.Outline,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo/Guest Quick Sign In
            StaggeredEnter(delayMillis = 550) {
                SocialLoginButton("Continue as Guest") {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                    viewModel.loginAsGuest()
                    onLoginSuccess()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up / Login Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSignUpMode) "Already have an account?" else "Don't have an account?",
                    color = PulseXColors.OnSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                val toggleInteractionSource = remember { MutableInteractionSource() }
                val isTogglePressed by toggleInteractionSource.collectIsPressedAsState()
                val toggleAlpha by animateFloatAsState(
                    targetValue = if (isTogglePressed) 0.6f else 1f,
                    animationSpec = tween(150),
                    label = "toggleAlpha"
                )
                Text(
                    text = if (isSignUpMode) "Sign In" else "Sign Up",
                    color = PulseXColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .graphicsLayer { alpha = toggleAlpha }
                        .clickable(
                            interactionSource = toggleInteractionSource,
                            indication = null,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } catch (_: Exception) {}
                                isSignUpMode = !isSignUpMode
                                viewModel.clearAuthError()
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    // Smooth transitions for focus
    val focusProgress by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TextFieldFocus"
    )

    // Glow and border animations
    val glowColor = PulseXColors.Primary.copy(alpha = 0.22f * focusProgress)
    val borderColor = lerpColor(
        Color.White.copy(alpha = 0.12f),
        PulseXColors.Primary.copy(alpha = 0.75f),
        focusProgress
    )
    val bgAlpha = 0.05f + (0.04f * focusProgress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = (6 * focusProgress).dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .background(
                color = Color.White.copy(alpha = bgAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = PulseXColors.Outline.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = lerpColor(PulseXColors.Outline, PulseXColors.Primary, focusProgress)
                )
            },
            visualTransformation = visualTransformation,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PulseXColors.OnSurface,
                unfocusedTextColor = PulseXColors.OnSurface,
                cursorColor = PulseXColors.Primary
            ),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
        )
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "socialBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + fraction * (stop.red - start.red),
        green = start.green + fraction * (stop.green - start.green),
        blue = start.blue + fraction * (stop.blue - start.blue),
        alpha = start.alpha + fraction * (stop.alpha - start.alpha)
    )
}
