package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Stock
import com.example.ui.components.LiveMarketStatusBar
import com.example.ui.components.PulseXColors
import com.example.ui.components.MiniSparklineChart
import com.example.ui.viewmodel.PulseXViewModel
import kotlin.math.roundToInt
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    viewModel: PulseXViewModel,
    onStockClick: (String) -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val marketError by viewModel.marketError.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = remember { listOf("ALL", "TECHNOLOGY", "SEMICONDUCTORS", "AUTOMOTIVE", "ENTERTAINMENT") }

    val filteredStocks = remember(stocks, selectedCategory) {
        if (selectedCategory == "ALL") {
            stocks
        } else {
            stocks.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        }
    }

    PullToRefreshBox(
        isRefreshing = isMarketLoading,
        onRefresh = { viewModel.refreshMarketData() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PulseXColors.Background),
            contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
        ) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Markets Overview",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { cat ->
                        val isSelected = selectedCategory == cat
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        
                        val chipScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.92f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "chipScale"
                        )
                        
                        val backgroundBrush = if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PulseXColors.PrimaryContainer,
                                    PulseXColors.Primary
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.05f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        }
                        
                        val borderBrush = if (isSelected) {
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
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        }

                        val haptic = LocalHapticFeedback.current

                        Box(
                            modifier = Modifier
                                .scale(chipScale)
                                .shadow(
                                    elevation = if (isSelected) 6.dp else 2.dp,
                                    shape = CircleShape,
                                    clip = false,
                                    ambientColor = if (isSelected) PulseXColors.Primary.copy(alpha = 0.2f) else Color.Transparent,
                                    spotColor = if (isSelected) PulseXColors.Primary.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clip(CircleShape)
                                .background(backgroundBrush)
                                .border(
                                    width = 1.dp,
                                    brush = borderBrush,
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (_: Exception) {}
                                        selectedCategory = cat
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else PulseXColors.OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LiveMarketStatusBar()
            }
        }

        if (isMarketLoading && filteredStocks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PulseXColors.PrimaryContainer)
                }
            }
        } else if (marketError != null && filteredStocks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load market data", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshMarketData() },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        } else {
            items(filteredStocks, key = { it.symbol }) { stock ->
            val isWatchlisted = watchlist.any { it.symbol == stock.symbol }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onStockClick(stock.symbol) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PulseXColors.GlassCardBg)
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stock.symbol.take(2),
                            color = PulseXColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stock.symbol,
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stock.name,
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }

                MiniSparklineChart(
                    symbol = stock.symbol,
                    isPositive = stock.isPositive,
                    historyPoints = stock.historyPoints,
                    modifier = Modifier.width(64.dp).height(28.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${stock.price}",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (stock.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (stock.isPositive) "+" else ""}${stock.percentChange}%",
                                color = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleWatchlistStock(stock) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (isWatchlisted) PulseXColors.Primary else PulseXColors.Outline
                        )
                    }
                }
            }
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                RechartsMarketTrendsSection()
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }
}
}

@Composable
fun RechartsMarketTrendsSection() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    val sp500Data = listOf(5120f, 5145f, 5110f, 5180f, 5210f, 5230f, 5250f)
    val nasdaqData = listOf(16100f, 16250f, 16180f, 16350f, 16420f, 16500f, 16580f)
    val dowData = listOf(38800f, 38950f, 38900f, 39100f, 39250f, 39300f, 39420f)

    var showSp500 by remember { mutableStateOf(true) }
    var showNasdaq by remember { mutableStateOf(true) }
    var showDow by remember { mutableStateOf(true) }
    var hoveredDayIndex by remember { mutableStateOf<Int?>(null) }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recharts Market Trends",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PulseXColors.Primary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Recharts 2.x", color = PulseXColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Weekly performance trends of major indices",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarketFilterChip(
                    selected = showSp500,
                    onClick = { showSp500 = !showSp500 },
                    label = "S&P 500",
                    color = PulseXColors.Primary
                )
                MarketFilterChip(
                    selected = showNasdaq,
                    onClick = { showNasdaq = !showNasdaq },
                    label = "Nasdaq",
                    color = Color(0xFF9C27B0)
                )
                MarketFilterChip(
                    selected = showDow,
                    onClick = { showDow = !showDow },
                    label = "Dow Jones",
                    color = PulseXColors.PositiveGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val stepX = size.width / (days.size - 1).coerceAtLeast(1)
                                val idx = (offset.x / stepX).roundToInt().coerceIn(0, days.size - 1)
                                hoveredDayIndex = idx
                                tryAwaitRelease()
                                hoveredDayIndex = null
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val stepX = size.width / (days.size - 1).coerceAtLeast(1)
                                val idx = (offset.x / stepX).roundToInt().coerceIn(0, days.size - 1)
                                hoveredDayIndex = idx
                            },
                            onDragEnd = { hoveredDayIndex = null },
                            onDragCancel = { hoveredDayIndex = null },
                            onDrag = { change, _ ->
                                change.consume()
                                val stepX = size.width / (days.size - 1).coerceAtLeast(1)
                                val idx = (change.position.x / stepX).roundToInt().coerceIn(0, days.size - 1)
                                hoveredDayIndex = idx
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height * i / gridLines
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (showSp500) drawLineSeries(this, sp500Data, PulseXColors.Primary, width, height)
                    if (showNasdaq) drawLineSeries(this, nasdaqData, Color(0xFF9C27B0), width, height)
                    if (showDow) drawLineSeries(this, dowData, PulseXColors.PositiveGreen, width, height)

                    hoveredDayIndex?.let { idx ->
                        val stepX = width / (days.size - 1).coerceAtLeast(1)
                        val x = idx * stepX
                        drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                hoveredDayIndex?.let { idx ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .border(1.dp, PulseXColors.Primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Day: ${days[idx]}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (showSp500) Text(text = "S&P 500: ${sp500Data[idx]}", color = PulseXColors.Primary, fontSize = 11.sp)
                            if (showNasdaq) Text(text = "Nasdaq: ${nasdaqData[idx]}", color = Color(0xFF9C27B0), fontSize = 11.sp)
                            if (showDow) Text(text = "Dow: ${dowData[idx]}", color = PulseXColors.PositiveGreen, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(text = day, color = PulseXColors.Outline, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun MarketFilterChip(selected: Boolean, onClick: () -> Unit, label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) color else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = if (selected) Color.White else PulseXColors.OnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun drawLineSeries(drawScope: DrawScope, data: List<Float>, color: Color, width: Float, height: Float) {
    with(drawScope) {
        val minVal = data.minOrNull() ?: 0f
        val maxVal = data.maxOrNull() ?: 100f
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - ((value - minVal) / range * (height - 20f) - 10f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5.dp.toPx())
        )

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - ((value - minVal) / range * (height - 20f) - 10f)
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
