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
import com.example.data.model.Crypto
import com.example.data.model.NewsArticle
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun CryptoDetailScreen(
    symbol: String,
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val cryptos by viewModel.cryptos.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val marketError by viewModel.marketError.collectAsState()
    val crypto = viewModel.getCrypto(symbol) ?: cryptos.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }

    if (crypto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (marketError != null && !isMarketLoading) {
                Text("Unable to load live data", color = PulseXColors.OnSurface)
            } else {
                CircularProgressIndicator(color = PulseXColors.PrimaryContainer)
            }
        }
        return
    }

    val watchlist by viewModel.watchlist.collectAsState()
    val isWatchlisted = watchlist.any { it.symbol == crypto.symbol }
    val articles by viewModel.articles.collectAsState()

    var selectedTimeRange by remember { mutableStateOf("1D") }
    val timeRanges = listOf("1H", "1D", "1W", "1M", "3M", "6M", "1Y", "5Y", "ALL")

    LaunchedEffect(symbol, selectedTimeRange) {
        viewModel.fetchCryptoTimeframeDetail(symbol, selectedTimeRange)
    }

    // Expandable descriptions
    val coinDescriptions = mapOf(
        "BTC" to "Bitcoin is the first decentralized digital currency, enabling peer-to-peer transfer of value globally without intermediaries. Created in 2009 by an anonymous entity named Satoshi Nakamoto, it acts as a digital store of value or 'digital gold'.",
        "ETH" to "Ethereum is a decentralized, open-source blockchain platform with smart contract functionality. Ether (ETH) is the native cryptocurrency of the platform. Ethereum is the foundation of decentralized finance (DeFi), non-fungible tokens (NFTs), and Web3 ecosystems.",
        "SOL" to "Solana is a high-performance blockchain designed for decentralized applications (dApps) and smart contracts. It uses a unique Proof-of-History (PoH) consensus mechanism paired with Proof-of-Stake to deliver extreme throughput and sub-penny transaction costs.",
        "BNB" to "BNB is the native cryptocurrency of the Binance ecosystem, including the BNB Beacon Chain and BNB Smart Chain. It powers transactions, smart contracts, and utility discounts across Binance products.",
        "ADA" to "Cardano is a decentralized public blockchain and cryptocurrency project. It is fully open-source and uses a peer-reviewed research approach to build a highly secure, scalable, and energy-efficient platform.",
        "XRP" to "XRP is a digital asset built for global real-time payments, offering banks and financial institutions a reliable, on-demand liquidity solution for cross-border transactions.",
        "DOGE" to "Dogecoin is an open-source, peer-to-peer cryptocurrency based on the popular 'Doge' Internet meme. It was created in 2013 as a lighthearted joke but has evolved into a highly liquid digital asset with a passionate global community.",
        "AVAX" to "Avalanche is an eco-friendly smart contract platform built for decentralized applications and enterprise deployments. It boasts sub-second transaction finality and a highly scalable multi-chain architecture."
    )
    val coinDesc = coinDescriptions[crypto.symbol.uppercase()] ?: "${crypto.name} (${crypto.symbol}) is a decentralized digital utility asset tracking live index valuations, high-density consensus protocols, and real-time liquidity pools on PulseX."

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
                        // Styled Logo container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!crypto.logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = crypto.logoUrl,
                                    contentDescription = "${crypto.name} Logo",
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text(
                                    text = crypto.symbol.take(3).uppercase(),
                                    color = PulseXColors.Primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = crypto.symbol,
                                    color = PulseXColors.OnSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2600D26A))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "24/7 LIVE",
                                        color = PulseXColors.PositiveGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = crypto.name,
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
                                viewModel.toggleWatchlistCrypto(crypto)
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
                                val textToCopy = "Check out ${crypto.name} (${crypto.symbol}) trading live at $${crypto.price} on PulseX!"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PulseX Share", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied coin link to clipboard!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(context, "Price alerts configured for ${crypto.symbol} near $${crypto.price}!", Toast.LENGTH_LONG).show()
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
                            text = "$${String.format(Locale.US, "%,.2f", crypto.price)}",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            letterSpacing = (-1).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (crypto.isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (crypto.isPositive) "Gain" else "Loss",
                                tint = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (crypto.isPositive) "+" else ""}${crypto.change24h} (${crypto.percentChange24h}%)",
                                color = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "24H",
                                color = PulseXColors.Outline,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "COINGECKO • REALTIME",
                            color = PulseXColors.Outline,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Updated: Just now",
                            color = PulseXColors.OnSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 11.sp
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

                val updatedCryptoHistoryPoints by rememberUpdatedState(crypto.historyPoints)
                val updatedCryptoPrice by rememberUpdatedState(crypto.price)
                val updatedCryptoIsPositive by rememberUpdatedState(crypto.isPositive)

                val rawPoints = remember(updatedCryptoHistoryPoints, updatedCryptoPrice, updatedCryptoIsPositive) {
                    if (updatedCryptoHistoryPoints.size >= 2) updatedCryptoHistoryPoints
                    else com.example.data.model.generateFallbackChartPoints(updatedCryptoPrice, updatedCryptoIsPositive)
                }
                val points = remember(rawPoints) {
                    android.util.Log.d(
                        "ChartDiagnostic",
                        "AssetType=Crypto, Symbol=${crypto.symbol}, RawHistorySize=${updatedCryptoHistoryPoints.size}, ProcessedPoints=${rawPoints.size}, Timeframe=$selectedTimeRange, Min=${rawPoints.minOrNull()}, Max=${rawPoints.maxOrNull()}"
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
                                val hoveredPrice = points[hoveredIndex!!]
                                val dt = getPointDateTime(hoveredIndex!!, points.size, selectedTimeRange)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PulseXColors.PrimaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Price: $${String.format(Locale.US, "%,.2f", hoveredPrice)}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dt,
                                        color = PulseXColors.Secondary,
                                        fontSize = 11.sp
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
                                            if (crypto.isPositive) PulseXColors.PositiveGreen.copy(alpha = 0.22f) else PulseXColors.NegativeRed.copy(alpha = 0.22f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                // Draw main line
                                drawPath(
                                    path = path,
                                    color = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
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
                                        color = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
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

                    // Zoom Controls overlay
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

                val updatedTechHistoryPoints by rememberUpdatedState(crypto.historyPoints)
                val updatedTechPrice by rememberUpdatedState(crypto.price)
                val updatedTechIsPositive by rememberUpdatedState(crypto.isPositive)

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
                                    text = "$${String.format(Locale.US, "%,.2f", indicators.ema)}",
                                    color = PulseXColors.OnSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (crypto.price >= indicators.ema) "Bullish Cross" else "Bearish Cross",
                                    color = if (crypto.price >= indicators.ema) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
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
                                    text = "$${String.format(Locale.US, "%,.0f", indicators.bollingerUpper)}",
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

        // --- 4. BENTO GRID KEY STATISTICS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Market Statistics",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val estOpen = crypto.price - crypto.change24h * 0.4
                val estPrevClose = crypto.price - crypto.change24h
                val rawCap = crypto.marketCap.replace("$", "").replace(",", "").replace("T", "000000000").replace("B", "000000").toDoubleOrNull() ?: (crypto.price * 19600000)
                
                val circSupply = String.format(Locale.US, "%,.0f", rawCap / crypto.price)
                val maxSupply = String.format(Locale.US, "%,.0f", (rawCap / crypto.price) * 1.3)

                val stats = listOf(
                    "OPEN" to "$${String.format(Locale.US, "%,.2f", estOpen)}",
                    "PREV CLOSE" to "$${String.format(Locale.US, "%,.2f", estPrevClose)}",
                    "24H HIGH" to "$${String.format(Locale.US, "%,.2f", crypto.price * 1.025)}",
                    "24H LOW" to "$${String.format(Locale.US, "%,.2f", crypto.price * 0.975)}",
                    "VOLUME (24H)" to crypto.volume24h,
                    "MARKET CAP" to crypto.marketCap,
                    "CIRC SUPPLY" to "$circSupply ${crypto.symbol}",
                    "MAX SUPPLY" to "$maxSupply ${crypto.symbol}"
                )

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
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. DETAILED PROTOCOL INFORMATION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Asset Profile",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val mktRank = when (crypto.symbol.uppercase()) {
                    "BTC" -> "#1"
                    "ETH" -> "#2"
                    "SOL" -> "#5"
                    "BNB" -> "#4"
                    "XRP" -> "#7"
                    "ADA" -> "#10"
                    "DOGE" -> "#8"
                    "AVAX" -> "#12"
                    else -> "#15"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Exchange Authority", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("Binance / Kraken", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Consensus Architecture", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(if (crypto.symbol.uppercase() == "BTC") "Proof of Work" else "Proof of Stake", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Country / Jurisdiction", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("Global Decentralized", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Settlement Base Currency", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text("USD", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Market Rank Cap", color = PulseXColors.Secondary, fontSize = 13.sp)
                            Text(mktRank, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 6. ORDER BOOK DEPTH VISUALIZATION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Live Order Depth (Informational)",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                var tickerTick by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(2000)
                        tickerTick += 1
                    }
                }

                val bids = remember(crypto.price, tickerTick) {
                    List(5) { i ->
                        val bidPrice = crypto.price - 1.25 - (i * 2.10) + (Math.sin(tickerTick.toDouble() + i) * 0.4)
                        val bidSize = (5 + i * 2 + (tickerTick % 6) * 1) % 15 + 1
                        bidPrice to bidSize
                    }
                }

                val asks = remember(crypto.price, tickerTick) {
                    List(5) { i ->
                        val askPrice = crypto.price + 1.25 + (i * 2.30) + (Math.cos(tickerTick.toDouble() + i) * 0.4)
                        val askSize = (4 + i * 3 + (tickerTick % 4) * 2) % 16 + 2
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
                                        val fillWidth = (size / 20f).coerceIn(0.1f, 1.0f)
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
                                            Text("$${String.format(Locale.US, "%,.2f", price)}", color = PulseXColors.PositiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                        val fillWidth = (size / 20f).coerceIn(0.1f, 1.0f)
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
                                            Text("$${String.format(Locale.US, "%,.2f", price)}", color = PulseXColors.NegativeRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. EXPANDABLE ABOUT SECTION ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                var isExpanded by remember { mutableStateOf(false) }

                Text(
                    text = "About ${crypto.name}",
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
                            text = coinDesc,
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

        // --- 8. HISTORICAL PERFORMANCE CARDS ---
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
                    "24 Hours" to crypto.percentChange24h,
                    "7 Days" to (crypto.percentChange24h * 1.5),
                    "30 Days" to (crypto.percentChange24h * 4.2 + 2.5),
                    "90 Days" to (crypto.percentChange24h * 8.4 - 3.1),
                    "YTD" to (crypto.percentChange24h * 15.5 + 12.0),
                    "1 Year" to (crypto.percentChange24h * 32.4 + 48.0),
                    "5 Years" to (crypto.percentChange24h * 240.2 + 180.5)
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

        // --- 9. RELATED ASSETS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Related Crypto Indices",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val relatedList = cryptos.filter { !it.symbol.equals(crypto.symbol, ignoreCase = true) }.take(4)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(relatedList, key = { _, it -> it.symbol }) { index, otherCrypto ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                                .bounceClick {
                                    viewModel.fetchCryptoTimeframeDetail(otherCrypto.symbol, "1D")
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
                                        Text(otherCrypto.symbol.take(3), color = PulseXColors.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(otherCrypto.symbol, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$${otherCrypto.price}", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${if (otherCrypto.isPositive) "+" else ""}${otherCrypto.percentChange24h}%",
                                    color = if (otherCrypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
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

        // --- 10. RELATED NEWS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Related Protocol News",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        val filteredNews = articles.filter { art ->
            art.relatedSymbols.any { it.equals(crypto.symbol, ignoreCase = true) } ||
                    art.title.contains(crypto.symbol, ignoreCase = true) ||
                    art.title.contains(crypto.name.split(" ").first(), ignoreCase = true)
        }.ifEmpty { articles }

        itemsIndexed(filteredNews.take(3), key = { _, it -> it.id }) { index, article ->
            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
            ArticleRowItem(article) { onNavigateToArticle(article.id) }
            }
        }
    }
}
