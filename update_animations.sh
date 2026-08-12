cat << 'INNER_EOF' > app/src/main/java/com/example/ui/components/Animations.kt
package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "bounce"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null && onClick != null) {
                        onClick()
                    }
                }
            }
        }
}

fun Modifier.scrollFadeIn(
    staggerIndex: Int = 0,
    delayMillis: Int = 0
) = composed {
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val isVisible = remember { mutableStateOf(hasAnimated) }
        
    val totalDelay = if (hasAnimated) 0 else delayMillis + (staggerIndex * 75)

    LaunchedEffect(Unit) {
        if (!hasAnimated) {
            if (totalDelay > 0) {
                kotlinx.coroutines.delay(totalDelay.toLong())
            }
            isVisible.value = true
            hasAnimated = true
        }
    }
    
    val alpha = animateFloatAsState(
        targetValue = if (isVisible.value) 1f else 0f,
        animationSpec = if (hasAnimated && isVisible.value) snap() else tween(durationMillis = 500),
        label = "scrollFadeIn_alpha"
    )
    val translationY = animateFloatAsState(
        targetValue = if (isVisible.value) 0f else 60f,
        animationSpec = if (hasAnimated && isVisible.value) snap() else spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "scrollFadeIn_translation"
    )

    this.graphicsLayer {
        this.alpha = alpha.value
        this.translationY = translationY.value
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_startOffsetX"
    )

    this
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.05f)
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        )
        .onGloballyPositioned {
            size = it.size
        }
}
INNER_EOF
