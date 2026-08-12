package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun StaggeredEnter(
    delayMillis: Int = 0,
    durationMillis: Int = 400,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        state.targetState = true
    }

    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(animationSpec = tween(durationMillis)) +
                slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(durationMillis)
                ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun FadeInEnter(
    delayMillis: Int = 0,
    durationMillis: Int = 600,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        state.targetState = true
    }

    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(animationSpec = tween(durationMillis)),
        modifier = modifier
    ) {
        content()
    }
}
