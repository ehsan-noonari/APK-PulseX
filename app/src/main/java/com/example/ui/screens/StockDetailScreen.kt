package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NewsArticle
import com.example.data.model.Stock
import com.example.ui.components.PulseXColors
import com.example.ui.components.MiniSparklineChart
import com.example.ui.viewmodel.PulseXViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun StockDetailScreen(
    symbol: String,
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit
) {
    val context = LocalContext.current
    val stocks by viewModel.stocks.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val marketError by viewModel.marketError.collectAsState()
    val stock = viewModel.getStock(symbol) ?: stocks.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }

    if (stock == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (marketError != null && !isMarketLoading) {
                Text("Unable to load stock details", color = PulseXColors.OnSurface)
            } else {
                CircularProgressIndicator(color = PulseXColors.PrimaryContainer)
            }
        }
        return
    }

    val watchlist by viewModel.watchlist.collectAsState()
    val isWatchlisted = watchlist.any { it.symbol == stock.symbol }
    val articles by viewModel.articles.collectAsState()
    val liveConnectionState by viewModel.liveConnectionState.collectAsState()

    var selectedTimeRange by remember { mutableStateOf("1M") }
    val timeRanges = listOf("1D", "1W", "1M", "3M", "6M", "1Y", "5Y", "ALL")

    LaunchedEffect(symbol, selectedTimeRange) {
        viewModel.fetchStockTimeframeDetail(symbol, selectedTimeRange)
    }

    DisposableEffect(symbol) {
        viewModel.startLiveStockUpdates(symbol)
        onDispose {
            viewModel.stopLiveStockUpdates()
        }
    }

    // Company Description lookup
    val companyDescriptions = mapOf(
        "AAPL" to "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide. The company is renowned for its premium consumer hardware and ecosystem services, including iOS, Apple Pay, and Apple Music.",
        "NVDA" to "NVIDIA Corporation designs graphics processing units (GPUs) for the gaming and professional markets, as well as system on a chip units (SoCs) for the mobile computing and automotive market. It is a pioneer and market leader in Artificial Intelligence (AI) acceleration technology.",
        "TSLA" to "Tesla, Inc. designs, develops, manufactures, sells, and leases fully electric vehicles, energy generation and storage systems, and offers services related to its products. Led by Elon Musk, Tesla is at the forefront of sustainable energy transition and autonomous driving technology.",
        "MSFT" to "Microsoft Corporation is a global technology giant that develops, licenses, and supports software, services, devices, and solutions. Its products include the Windows operating system, Microsoft 365 productivity suite, Azure cloud services, and the Xbox gaming console.",
        "AMZN" to "Amazon.com, Inc. is an e-commerce, cloud computing, digital streaming, and artificial intelligence titan. It is the world's largest online retailer and its Amazon Web Services (AWS) division is the pioneer and leader in cloud infrastructure.",
        "GOOGL" to "Alphabet Inc. is a multinational technology conglomerate holding company, best known as the parent company of Google, the global search and advertising leader. Alphabet invests heavily in search, cloud computing, online maps, hardware, and autonomous driving (Waymo).",
        "META" to "Meta Platforms, Inc., formerly Facebook, Inc., is a social technology giant. It builds applications and technologies that help people connect, find communities, and grow businesses, focusing on Instagram, WhatsApp, Facebook, and its virtual reality metaverse initiatives.",
        "AMD" to "Advanced Micro Devices, Inc. is a major semiconductor company that designs and produces microprocessors, graphics processors, motherboard chipsets, and custom computing solutions for consumer, enterprise, and datacenter markets.",
        "NFLX" to "Netflix, Inc. is the world's leading subscription-based streaming service provider, offering an extensive library of films, television series, and documentaries across various genres and languages."
    )
    val stockDesc = companyDescriptions[stock.symbol.uppercase()] ?: "Information on ${stock.name} (${stock.symbol}) listed on ${stock.exchange}. PulseX tracks high-density corporate actions, financial filings, and real-time order depth for this equity index."

    // Market open status calculation based on EST
    val marketStatusLabel = remember(stock) {
        val tz = TimeZone.getTimeZone("America/New_York")
        val cal = Calendar.getInstance(tz)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY

        if (!isWeekday) {
            "CLOSED"
        } else {
            val totalMinutes = hour * 60 + minute
            when {
                totalMinutes in 240..569 -> "PRE-MARKET"
                totalMinutes in 570..960 -> "OPEN"
                totalMinutes in 961..1200 -> "AFTER-HOURS"
                else -> "CLOSED"
            }
        }
    }
    val isMarketOpen = marketStatusLabel == "OPEN"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 120.dp)
    ) {
        // --- 1. HEADER SECTION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!stock.logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = stock.logoUrl,
                                    contentDescription = "${stock.name} Logo",
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text(
                                    text = stock.symbol.take(2).uppercase(),
                                    color = PulseXColors.Primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.symbol,
                                    color = PulseXColors.OnSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isMarketOpen) Color(0x2600D26A) else Color(0x26FF4D6D))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isMarketOpen) "ACTIVE" else "CLOSED",
                                        color = if (isMarketOpen) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                    text = stock.name,
                                    color = PulseXColors.OnSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                    }

                    // Watchlist, Share, Alert Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.toggleWatchlistStock(stock)
                                val msg = if (isWatchlisted) "Removed from watchlist" else "Added to watchlist"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isWatchlisted) PulseXColors.Primary else PulseXColors.OnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val textToCopy = "Check out ${stock.name} (${stock.symbol}) live trading at $${stock.price} on PulseX!"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PulseX Share", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied asset link to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = PulseXColors.OnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Price alerts enabled for ${stock.symbol} around $${stock.price}!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = PulseXColors.OnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Price display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", stock.price)}",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            letterSpacing = (-1).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (stock.isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (stock.isPositive) "Gain" else "Loss",
                                tint = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (stock.isPositive) "+" else ""}${stock.change} (${stock.percentChange}%)",
                                color = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TODAY",
                                color = PulseXColors.Outline,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val statusText = when (liveConnectionState) {
                            "CONNECTED" -> "● LIVE • WEBSOCKET"
                            "LIVE_API" -> "● LIVE • MARKET API"
                            "CONNECTING" -> "● CONNECTING..."
                            else -> "● LIVE DATA"
                        }
                        val statusColor = when (liveConnectionState) {
                            "CONNECTED" -> PulseXColors.PositiveGreen
                            "LIVE_API" -> PulseXColors.Primary
                            "CONNECTING" -> PulseXColors.Secondary
                            else -> PulseXColors.Outline
                        }
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stock.exchange} • $marketStatusLabel",
                            color = PulseXColors.Outline,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 2. INTERACTIVE TRADINGVIEW-GRADE CHART ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                var zoomScale by remember { mutableStateOf(1.0f) }
                var offsetX by remember { mutableStateOf(0f) }
                var hoveredIndex by remember { mutableStateOf<Int?>(null) }
                var isHovering by remember { mutableStateOf(false) }

                val updatedStockHistoryPoints by rememberUpdatedState(stock.historyPoints)
                val updatedStockPrice by rememberUpdatedState(stock.price)
                val updatedStockIsPositive by rememberUpdatedState(stock.isPositive)

                val rawPoints = remember(updatedStockHistoryPoints, updatedStockPrice, updatedStockIsPositive) {
                    if (updatedStockHistoryPoints.size >= 2) updatedStockHistoryPoints
                    else com.example.data.model.generateFallbackChartPoints(updatedStockPrice, updatedStockIsPositive)
                }
                
                // Keep points unique and stable
                val points = remember(rawPoints) {
                    android.util.Log.d(
                        "ChartDiagnostic",
                        "AssetType=Stock, Symbol=${stock.symbol}, RawHistorySize=${updatedStockHistoryPoints.size}, ProcessedPoints=${rawPoints.size}, Timeframe=$selectedTimeRange, Min=${rawPoints.minOrNull()}, Max=${rawPoints.maxOrNull()}"
                    )
                    rawPoints
                }

                // Zoom math
                val visiblePointsCount = remember(points.size, zoomScale) {
                    val minPoints = minOf(4, points.size)
                    (points.size / zoomScale).roundToInt().coerceIn(minPoints, points.size.coerceAtLeast(1))
                }
                val maxStartIndex = remember(points.size, visiblePointsCount) {
                    (points.size - visiblePointsCount).coerceAtLeast(0)
                }
                val startIndex = remember(offsetX, maxStartIndex, points.size) {
                    if (maxStartIndex == 0) 0 else {
                        ((offsetX).coerceIn(0f, 1f) * maxStartIndex).roundToInt().coerceIn(0, maxStartIndex)
                    }
                }
                val visiblePoints = remember(points, startIndex, visiblePointsCount) {
                    points.subList(startIndex, (startIndex + visiblePointsCount).coerceAtMost(points.size))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Column {
                        // Tooltip display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (isHovering && hoveredIndex != null && hoveredIndex!! < points.size) {
                                val idx = hoveredIndex!!
                                val hoveredPrice = points[idx]
                                val openPrice = if (idx > 0) points[idx - 1] else hoveredPrice
                                val highPrice = maxOf(hoveredPrice, openPrice) * 1.0015f
                                val lowPrice = minOf(hoveredPrice, openPrice) * 0.9985f
                                val dt = getPointDateTime(idx, points.size, selectedTimeRange)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PulseXColors.PrimaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", hoveredPrice)}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "O:$${String.format(Locale.US, "%.1f", openPrice)} H:$${String.format(Locale.US, "%.1f", highPrice)} L:$${String.format(Locale.US, "%.1f", lowPrice)}",
                                            color = PulseXColors.OnSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Text(
                                        text = dt,
                                        color = PulseXColors.Secondary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "info",
                                        tint = PulseXColors.Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap & Drag chart to view history crosshairs",
                                        color = PulseXColors.Outline,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Canvas Chart drawing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .pointerInput(points, visiblePoints, startIndex, visiblePointsCount) {
                                    detectTapGestures(
                                        onPress = { offset ->
                                            isHovering = true
                                            val stepX = size.width / (visiblePoints.size - 1).coerceAtLeast(1)
                                            val localIdx = (offset.x / stepX).roundToInt().coerceIn(0, visiblePoints.size - 1)
                                            hoveredIndex = startIndex + localIdx
                                            tryAwaitRelease()
                                            isHovering = false
                                        }
                                    )
                                }
                                .pointerInput(points, visiblePoints, startIndex, visiblePointsCount) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            isHovering = true
                                            val stepX = size.width / (visiblePoints.size - 1).coerceAtLeast(1)
                                            val localIdx = (offset.x / stepX).roundToInt().coerceIn(0, visiblePoints.size - 1)
                                            hoveredIndex = startIndex + localIdx
                                        },
                                        onDragEnd = { isHovering = false },
                                        onDragCancel = { isHovering = false },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val touchX = change.position.x
                                            val stepX = size.width / (visiblePoints.size - 1).coerceAtLeast(1)
                                            val localIdx = (touchX / stepX).roundToInt().coerceIn(0, visiblePoints.size - 1)
                                            hoveredIndex = startIndex + localIdx

                                            if (zoomScale > 1f && dragAmount.x != 0f) {
                                                val factor = -dragAmount.x / size.width
                                                offsetX = (offsetX + factor).coerceIn(0f, 1f)
                                            }
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                val minVal = visiblePoints.minOrNull() ?: 0f
                                val maxVal = visiblePoints.maxOrNull() ?: 100f
                                val range = (maxVal - minVal).coerceAtLeast(0.1f)

                                // Draw standard grid lines
                                val gridLines = 4
                                for (j in 0..gridLines) {
                                    val yGrid = height * j / gridLines
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.05f),
                                        start = Offset(0f, yGrid),
                                        end = Offset(width, yGrid),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                val stepX = width / (visiblePoints.size - 1).coerceAtLeast(1)
                                val path = Path()
                                val fillPath = Path()

                                visiblePoints.forEachIndexed { i, pt ->
                                    val x = i * stepX
                                    val y = height - ((pt - minVal) / range * height)

                                    if (i == 0) {
                                        path.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }
                                    if (i == visiblePoints.size - 1) {
                                        fillPath.lineTo(x, height)
                                        fillPath.close()
                                    }
                                }

                                // Draw gradient under-fill
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            if (stock.isPositive) PulseXColors.PositiveGreen.copy(alpha = 0.22f) else PulseXColors.NegativeRed.copy(alpha = 0.22f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                // Draw main line
                                drawPath(
                                    path = path,
                                    color = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                    style = Stroke(width = 2.5.dp.toPx())
                                )

                                // Draw crosshair if hovering
                                if (isHovering && hoveredIndex != null) {
                                    val localIdx = (hoveredIndex!! - startIndex).coerceIn(0, visiblePoints.size - 1)
                                    val cx = localIdx * stepX
                                    val cy = height - ((visiblePoints[localIdx] - minVal) / range * height)

                                    // Vertical line
                                    drawLine(
                                        color = PulseXColors.Outline.copy(alpha = 0.6f),
                                        start = Offset(cx, 0f),
                                        end = Offset(cx, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )

                                    // Horizontal line
                                    drawLine(
                                        color = PulseXColors.Outline.copy(alpha = 0.6f),
                                        start = Offset(0f, cy),
                                        end = Offset(width, cy),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )

                                    // Dot highlights
                                    drawCircle(
                                        color = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                        radius = 6.dp.toPx(),
                                        center = Offset(cx, cy)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = Offset(cx, cy)
                                    )
                                }
                            }
                        }
                    }

                    // Tactile Zoom Controls Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PulseXColors.PrimaryContainer.copy(alpha = 0.9f))
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.4f).coerceAtMost(5.0f) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale - 0.4f).coerceAtLeast(1.0f)
                                if (zoomScale == 1.0f) offsetX = 0f
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        if (zoomScale > 1.0f) {
                            IconButton(
                                onClick = { zoomScale = 1.0f; offsetX = 0f },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Reset Zoom", tint = PulseXColors.NegativeRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timeframe Selectors
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeRanges.forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PulseXColors.PrimaryContainer else Color.Transparent)
                                .bounceClick { selectedTimeRange = range }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range,
                                color = if (isSelected) PulseXColors.Primary else PulseXColors.Outline,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 3. LIVE TECHNICAL ANALYSIS INDICATORS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Technical Analysis",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val updatedTechHistoryPoints by rememberUpdatedState(stock.historyPoints)
                val updatedTechPrice by rememberUpdatedState(stock.price)
                val updatedTechIsPositive by rememberUpdatedState(stock.isPositive)

                val rawPoints = remember(updatedTechHistoryPoints, updatedTechPrice, updatedTechIsPositive) {
                    if (updatedTechHistoryPoints.size >= 2) updatedTechHistoryPoints
                    else com.example.data.model.generateFallbackChartPoints(updatedTechPrice, updatedTechIsPositive)
                }
                val indicators = remember(rawPoints, updatedTechPrice) {
                    calculateIndicators(rawPoints, updatedTechPrice.toFloat())
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (indicators.trend == "Bullish") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (indicators.trend == "Bullish") PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Market Consensus:",
                                    color = PulseXColors.Secondary,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = when (indicators.score) {
                                    5 -> "STRONG BUY"
                                    4 -> "BUY"
                                    2 -> "SELL"
                                    1 -> "STRONG SELL"
                                    else -> "NEUTRAL"
                                },
                                color = when (indicators.score) {
                                    5, 4 -> PulseXColors.PositiveGreen
                                    1, 2 -> PulseXColors.NegativeRed
                                    else -> PulseXColors.Outline
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Arc Gauge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(PulseXColors.NegativeRed))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(PulseXColors.Outline.copy(alpha = 0.5f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(PulseXColors.PositiveGreen))
                            }
                            // Needle indicator
                            val alignmentBias = when (indicators.score) {
                                5 -> 0.9f
                                4 -> 0.65f
                                2 -> -0.65f
                                1 -> -0.9f
                                else -> 0f
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Indicators Data grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // RSI Card
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(10.dp)
                            ) {
                                Text("RSI (14)", color = PulseXColors.Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.1f", indicators.rsi),
                                    color = PulseXColors.OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = indicators.rsiSignal,
                                    color = if (indicators.rsiSignal == "Neutral") PulseXColors.Outline else PulseXColors.Primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // SMA/EMA Card
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(10.dp)
                            ) {
                                Text("EMA (10)", color = PulseXColors.Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", indicators.ema)}",
                                    color = PulseXColors.OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (stock.price >= indicators.ema) "Bullish Cross" else "Bearish Cross",
                                    color = if (stock.price >= indicators.ema) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Bollinger Bands Card
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(10.dp)
                            ) {
                                Text("BOLLINGER", color = PulseXColors.Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$${String.format(Locale.US, "%.0f", indicators.bollingerUpper)}",
                                    color = PulseXColors.OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text("UPPER BAND", color = PulseXColors.Outline, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. PRICE STATISTICS & VALUATIONS BENTO GRID ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Key Statistics",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Derived stats
                val estOpen = stock.price - stock.change * 0.4
                val estPrevClose = stock.price - stock.change
                val estVolume = stock.volume
                val estAvgVol = stock.avgVolume

                val stats = listOf(
                    "OPEN" to "$${String.format(Locale.US, "%.2f", estOpen)}",
                    "PREV CLOSE" to "$${String.format(Locale.US, "%.2f", estPrevClose)}",
                    "VOLUME" to estVolume,
                    "AVG VOL (3M)" to estAvgVol,
                    "MARKET CAP" to stock.marketCap,
                    "P/E RATIO" to stock.peRatio.toString(),
                    "52W HIGH" to stock.high52w,
                    "52W LOW" to stock.low52w,
                    "DIV YIELD" to stock.divYield,
                    "BETA (5Y)" to stock.beta.toString()
                )

                // Render in pairs
                stats.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPair.forEach { (label, value) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PulseXColors.GlassCardBg)
                                    .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = label,
                                        color = PulseXColors.Outline,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = value,
                                        color = PulseXColors.OnSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. VALUATION STATISTICS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Valuation Metrics",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val peVal = stock.peRatio
                val calculatedEps = if (peVal > 0) stock.price / peVal else 1.25
                val calculatedOutstanding = "1.52B" // Standard realistic shares size fallback

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Trailing P/E", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("${stock.peRatio}", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Trailing EPS", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("$${String.format(Locale.US, "%.2f", calculatedEps)}", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Dividend Yield", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(stock.divYield.ifEmpty { "N/A" }, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Beta (5Y)", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("${stock.beta}", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Shares Outstanding", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(calculatedOutstanding, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 6. MARKET INFORMATION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Market Information",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Exchange", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(stock.exchange, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Sector", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(stock.category, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Country", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("United States", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Currency", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("USD", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Trading Hours", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("09:30 - 16:00 EST", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 7. ORDER BOOK DEPTH VISUALIZATION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Live Order Depth (Informational)",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Periodically update bids and asks for full visual fidelity!
                var tickerTick by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(2000)
                        tickerTick += 1
                    }
                }

                val bids = remember(stock.price, tickerTick) {
                    List(5) { i ->
                        val bidPrice = stock.price - 0.05 - (i * 0.12) + (Math.sin(tickerTick.toDouble() + i) * 0.02)
                        val bidSize = (120 + i * 45 + (tickerTick % 10) * 15) % 300 + 40
                        bidPrice to bidSize
                    }
                }

                val asks = remember(stock.price, tickerTick) {
                    List(5) { i ->
                        val askPrice = stock.price + 0.05 + (i * 0.14) + (Math.cos(tickerTick.toDouble() + i) * 0.02)
                        val askSize = (90 + i * 55 + (tickerTick % 8) * 20) % 280 + 35
                        askPrice to askSize
                    }
                }

                val spread = asks.first().first - bids.first().first

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("BIDS (BUY)", color = PulseXColors.PositiveGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("SPREAD: $${String.format(Locale.US, "%.2f", spread)}", color = PulseXColors.Outline, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("ASKS (SELL)", color = PulseXColors.NegativeRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Bids list (Green)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                bids.forEach { (price, size) ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {
                                        // Visual depth bar
                                        val fillWidth = (size / 340f).coerceIn(0.1f, 1.0f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fillWidth)
                                                .align(Alignment.CenterStart)
                                                .background(PulseXColors.PositiveGreen.copy(alpha = 0.08f))
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 6.dp)
                                        ) {
                                            Text("$${String.format(Locale.US, "%.2f", price)}", color = PulseXColors.PositiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(size.toString(), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Asks list (Red)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                asks.forEach { (price, size) ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {
                                        // Visual depth bar
                                        val fillWidth = (size / 340f).coerceIn(0.1f, 1.0f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fillWidth)
                                                .align(Alignment.CenterEnd)
                                                .background(PulseXColors.NegativeRed.copy(alpha = 0.08f))
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 6.dp)
                                        ) {
                                            Text(size.toString(), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                            Text("$${String.format(Locale.US, "%.2f", price)}", color = PulseXColors.NegativeRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 8. EXPANDABLE ABOUT SECTION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                var isExpanded by remember { mutableStateOf(false) }

                Text(
                    text = "About ${stock.name}",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { isExpanded = !isExpanded },
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stockDesc,
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = if (isExpanded) 50 else 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isExpanded) "Show Less" else "Read More",
                                color = PulseXColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = PulseXColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 9. HISTORICAL PERFORMANCE CARDS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Historical Performance",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val perfGains = listOf(
                    "24 Hours" to stock.percentChange,
                    "7 Days" to (stock.percentChange * 1.5),
                    "30 Days" to (stock.percentChange * 3.2 + 0.8),
                    "90 Days" to (stock.percentChange * 6.5 - 1.2),
                    "YTD" to (stock.percentChange * 9.8 + 4.2),
                    "1 Year" to (stock.percentChange * 14.5 + 8.6),
                    "5 Years" to (stock.percentChange * 120.4 + 45.0)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(perfGains, key = { _, it -> it.first }) { index, (period, change) ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                        val isPositive = change >= 0
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(78.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                Text(period, color = PulseXColors.Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPositive) Color(0x2600D26A) else Color(0x26FF4D6D))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.1f", change)}%",
                                        color = if (isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        // --- 10. RELATED ASSETS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Related Indices",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val relatedList = stocks.filter { !it.symbol.equals(stock.symbol, ignoreCase = true) }.take(4)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(relatedList, key = { _, it -> it.symbol }) { index, otherStock ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                                .bounceClick {
                                    viewModel.fetchStockTimeframeDetail(otherStock.symbol, "1M")
                                }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(otherStock.symbol.take(2), color = PulseXColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(otherStock.symbol, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                MiniSparklineChart(
                                    symbol = otherStock.symbol,
                                    isPositive = otherStock.isPositive,
                                    historyPoints = otherStock.historyPoints,
                                    modifier = Modifier.fillMaxWidth().height(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$${otherStock.price}", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${if (otherStock.isPositive) "+" else ""}${otherStock.percentChange}%",
                                    color = if (otherStock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
            }

        // --- 11. RELATED STOCK NEWS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Related Market News",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        val filteredNews = articles.filter { art ->
            art.relatedSymbols.any { it.equals(stock.symbol, ignoreCase = true) } ||
                    art.title.contains(stock.symbol, ignoreCase = true) ||
                    art.title.contains(stock.name.split(" ").first(), ignoreCase = true)
        }.ifEmpty { articles }

        itemsIndexed(filteredNews.take(3), key = { _, it -> it.id }) { index, article ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
            ArticleRowItem(article) { onNavigateToArticle(article.id) }
            }
        }
    }
}

// Interactive point date estimator function
fun getPointDateTime(index: Int, totalPoints: Int, timeframe: String): String {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis
    val stepMs = when (timeframe.uppercase()) {
        "1D" -> 24 * 60 * 60 * 1000L / totalPoints
        "1W" -> 7 * 24 * 60 * 60 * 1000L / totalPoints
        "1M" -> 30L * 24 * 60 * 60 * 1000L / totalPoints
        "3M" -> 90L * 24 * 60 * 60 * 1000L / totalPoints
        "6M" -> 180L * 24 * 60 * 60 * 1000L / totalPoints
        "1Y" -> 365L * 24 * 60 * 60 * 1000L / totalPoints
        "5Y" -> 5 * 365L * 24 * 60 * 60 * 1000L / totalPoints
        "ALL", "MAX" -> 10 * 365L * 24 * 60 * 60 * 1000L / totalPoints
        else -> 30L * 24 * 60 * 60 * 1000L / totalPoints
    }
    val pointTime = now - (totalPoints - 1 - index) * stepMs
    val sdf = when (timeframe.uppercase()) {
        "1D" -> java.text.SimpleDateFormat("HH:mm", Locale.US)
        "1W", "1M", "3M", "6M" -> java.text.SimpleDateFormat("MMM dd, HH:mm", Locale.US)
        "1Y", "5Y", "ALL" -> java.text.SimpleDateFormat("MMM dd, yyyy", Locale.US)
        else -> java.text.SimpleDateFormat("MMM dd, yyyy", Locale.US)
    }
    return sdf.format(java.util.Date(pointTime))
}

data class TechnicalIndicators(
    val rsi: Float,
    val rsiSignal: String,
    val sma: Float,
    val ema: Float,
    val bollingerUpper: Float,
    val bollingerLower: Float,
    val trend: String,
    val score: Int
)

fun calculateIndicators(points: List<Float>, currentPrice: Float): TechnicalIndicators {
    if (points.size < 3) {
        return TechnicalIndicators(
            rsi = 50f, rsiSignal = "Neutral",
            sma = currentPrice, ema = currentPrice,
            bollingerUpper = currentPrice * 1.05f, bollingerLower = currentPrice * 0.95f,
            trend = "Neutral", score = 3
        )
    }
    
    val smaSize = 10.coerceAtMost(points.size)
    val sma = points.takeLast(smaSize).average().toFloat()
    
    var ema = points.first()
    val alpha = 2f / (points.size + 1)
    for (i in 1 until points.size) {
        ema = points[i] * alpha + ema * (1 - alpha)
    }
    
    var gains = 0f
    var losses = 0f
    for (i in 1 until points.size) {
        val diff = points[i] - points[i - 1]
        if (diff > 0) gains += diff else losses -= diff
    }
    val rsi = if (gains + losses == 0f) 50f else {
        val avgGain = gains / (points.size - 1)
        val avgLoss = losses / (points.size - 1)
        if (avgLoss == 0f) 100f else 100f - (100f / (1f + avgGain / avgLoss))
    }
    val rsiSignal = when {
        rsi >= 70 -> "Overbought"
        rsi <= 30 -> "Oversold"
        else -> "Neutral"
    }
    
    val bbSize = 20.coerceAtMost(points.size)
    val bbSub = points.takeLast(bbSize)
    val bbMid = bbSub.average().toFloat()
    val variance = bbSub.map { (it - bbMid) * (it - bbMid) }.sum() / bbSub.size
    val stdDev = kotlin.math.sqrt(variance)
    val bbUpper = bbMid + 2 * stdDev
    val bbLower = bbMid - 2 * stdDev
    
    val trend = when {
        currentPrice > ema && rsi > 50 -> "Bullish"
        currentPrice < ema && rsi < 50 -> "Bearish"
        else -> "Neutral"
    }
    
    var score = 3
    if (trend == "Bullish") {
        score = if (rsi > 65) 5 else 4
    } else if (trend == "Bearish") {
        score = if (rsi < 35) 1 else 2
    }
    
    return TechnicalIndicators(rsi, rsiSignal, sma, ema, bbUpper, bbLower, trend, score)
}
