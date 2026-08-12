package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Random

/**
 * Deterministic generator for 24h mini sparkline trend points
 * when historical points are not provided or incomplete.
 */
fun generateSparklinePoints(
    seedKey: String,
    isPositive: Boolean,
    pointsCount: Int = 18
): List<Float> {
    val seed = seedKey.hashCode()
    val random = Random(seed.toLong())
    val points = mutableListOf<Float>()
    var current = 100f
    points.add(current)
    
    for (i in 1 until pointsCount) {
        val bias = if (isPositive) 0.55f else -0.55f
        val delta = (random.nextFloat() - 0.45f + bias * (i.toFloat() / pointsCount)) * 4f
        current = (current + delta).coerceAtLeast(15f)
        points.add(current)
    }
    
    // Ensure final point reflects overall direction accurately
    if (isPositive && points.last() <= points.first()) {
        points[points.lastIndex] = points.first() + 10f
    } else if (!isPositive && points.last() >= points.first()) {
        points[points.lastIndex] = (points.first() - 10f).coerceAtLeast(10f)
    }
    
    return points
}

/**
 * High-performance, Jetpack Compose native mini sparkline chart component.
 * Renders a 24-hour price trend line with smooth quadratic Bezier curves,
 * dynamic gradient fill, and end-point indicator.
 */
@Composable
fun MiniSparklineChart(
    symbol: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier.width(68.dp).height(28.dp),
    historyPoints: List<Float> = emptyList(),
    lineColor: Color = if (isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
    strokeWidth: Dp = 1.8.dp,
    showGradient: Boolean = true
) {
    val updatedHistoryPoints by rememberUpdatedState(historyPoints)
    val points = remember(symbol, isPositive, updatedHistoryPoints) {
        val pts = if (updatedHistoryPoints.size >= 2) updatedHistoryPoints
        else generateSparklinePoints(symbol, isPositive)
        android.util.Log.d(
            "ChartDiagnostic",
            "AssetType=Sparkline, Symbol=$symbol, TotalPoints=${pts.size}, RawHistorySize=${updatedHistoryPoints.size}, Min=${pts.minOrNull()}, Max=${pts.maxOrNull()}"
        )
        pts
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (points.size < 2) return@Canvas

            val width = size.width
            val height = size.height

            val minVal = points.minOrNull() ?: 0f
            val maxVal = points.maxOrNull() ?: 100f
            val range = (maxVal - minVal).let { if (it == 0f) 1f else it }

            val verticalPadding = 3.dp.toPx()
            val availableHeight = height - (verticalPadding * 2)

            // Convert points to Canvas coordinates
            val stepX = width / (points.size - 1)
            val offsets = points.mapIndexed { index, value ->
                val x = index * stepX
                val normalizedY = (value - minVal) / range
                val y = height - verticalPadding - (normalizedY * availableHeight)
                Offset(x, y)
            }

            // Build smooth Bezier path
            val strokePath = Path().apply {
                moveTo(offsets[0].x, offsets[0].y)
                for (i in 0 until offsets.size - 1) {
                    val p1 = offsets[i]
                    val p2 = offsets[i + 1]
                    val controlX = (p1.x + p2.x) / 2f
                    cubicTo(
                        controlX, p1.y,
                        controlX, p2.y,
                        p2.x, p2.y
                    )
                }
            }

            // Draw area gradient fill under line
            if (showGradient) {
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.28f),
                            lineColor.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
            }

            // Draw trend line
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw glowing end dot on the latest price point
            val lastOffset = offsets.last()
            drawCircle(
                color = lineColor.copy(alpha = 0.35f),
                radius = 3.5.dp.toPx(),
                center = lastOffset
            )
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = lastOffset
            )
        }
    }
}
