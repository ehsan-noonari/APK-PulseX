package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import java.util.Locale
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@Composable
fun SmartSearchDetailScreen(
    query: String,
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToStock: (String) -> Unit,
    onNavigateToCrypto: (String) -> Unit
) {
    val context = LocalContext.current
    val articles by viewModel.articles.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val cryptos by viewModel.cryptos.collectAsState()

    val cleanQuery = query.trim()
    val lowerQuery = cleanQuery.lowercase(Locale.ROOT)

    // Match stock or crypto if any
    val matchedStock = stocks.firstOrNull { it.symbol.equals(cleanQuery, ignoreCase = true) || it.name.contains(cleanQuery, ignoreCase = true) }
    val matchedCrypto = cryptos.firstOrNull { it.symbol.equals(cleanQuery, ignoreCase = true) || it.name.contains(cleanQuery, ignoreCase = true) }
    val matchedArticles = articles.filter { it.title.contains(cleanQuery, ignoreCase = true) || it.category.contains(cleanQuery, ignoreCase = true) }

    // Intelligent AI Summary based on topic
    val aiSummary = remember(cleanQuery) {
        when {
            lowerQuery.contains("tesla") || lowerQuery.contains("tsla") ->
                "Tesla, Inc. (TSLA) is an American multinational automotive and clean energy company headquartered in Austin, Texas. Tesla designs and manufactures electric vehicles, solar roof tiles, and stationary battery energy storage products. Led by Elon Musk, Tesla remains a bellwether for institutional EV sentiment, autonomous driving (FSD) milestones, and AI robotics."
            lowerQuery.contains("bitcoin") || lowerQuery.contains("btc") ->
                "Bitcoin is the pioneer decentralized cryptographic digital asset operating on an immutable peer-to-peer ledger. As institutional adoption surges through spot ETFs and sovereign balance sheet allocations, Bitcoin acts as the primary reserve asset of the decentralized financial era."
            lowerQuery.contains("nvidia") || lowerQuery.contains("nvda") ->
                "NVIDIA Corporation designs graphics processing units (GPUs) for the gaming and professional markets, as well as system on a chip units (SoCs) for the mobile computing and automotive market. NVIDIA is currently the dominant foundational infrastructure provider for generative AI large language models."
            lowerQuery.contains("apple") || lowerQuery.contains("aapl") ->
                "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories, and sells a variety of related services. Apple Intelligence integrates on-device foundation models deeply across iOS and macOS ecosystems."
            lowerQuery.contains("inflation") ->
                "Economic inflation measures the rate of rising prices for goods and services across an economy. Central banks such as the Federal Reserve monitor core CPI and PCE indices to calibrate monetary policy, benchmark interest rates, and manage macroeconomic liquidity cycles."
            lowerQuery.contains("gold") ->
                "Gold functions as the premier traditional safe-haven hedge against geopolitical instability, currency debasement, and inflationary pressures. Central banks and institutional portfolios maintain strategic gold reserves to preserve capital during market volatility."
            lowerQuery.contains("ethereum") || lowerQuery.contains("eth") ->
                "Ethereum is a decentralized, open-source blockchain with smart contract functionality. Ether is the native cryptocurrency powering decentralized finance (DeFi), layer-2 rollups, and enterprise Web3 application architectures."
            else ->
                "'$cleanQuery' is actively tracked across global financial markets, macroeconomic indicators, and institutional news wires on PulseX. Real-time consensus indicates heightened trading volumes and dynamic volatility shifts across related asset classes."
        }
    }

    var isBookmarked by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 120.dp)
    ) {
        // --- 1. HEADER & TOPIC BADGE ---
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PulseXColors.PrimaryContainer.copy(alpha = 0.5f))
                                .border(1.dp, PulseXColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PulseXColors.AiAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cleanQuery.uppercase(Locale.ROOT),
                                    color = PulseXColors.OnSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PulseXColors.AiAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI INTELLIGENCE",
                                        color = PulseXColors.AiAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Perplexity & Bloomberg Universal Search",
                                color = PulseXColors.OnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                val msg = if (isBookmarked) "Saved to intelligent bookmarks" else "Removed from bookmarks"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) PulseXColors.Primary else PulseXColors.OnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val textToCopy = "PulseX AI Insight on '$cleanQuery': $aiSummary"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PulseX AI Share", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied intelligence summary!", Toast.LENGTH_SHORT).show()
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
                    }
                }
            }
        }

        // --- 2. MATCHED STOCK OR CRYPTO CARD (IF APPLICABLE) ---
        if (matchedStock != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { onNavigateToStock(matchedStock.symbol) },
                        colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${matchedStock.name} (${matchedStock.symbol})", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Live Stock Ticker • Tap for Chart & Financials", color = PulseXColors.Primary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "$${matchedStock.price}", color = PulseXColors.OnSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text(text = "${matchedStock.change} (${matchedStock.percentChange}%)", color = if (matchedStock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (matchedCrypto != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { onNavigateToCrypto(matchedCrypto.symbol) },
                        colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${matchedCrypto.name} (${matchedCrypto.symbol})", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Live Crypto Asset • Tap for Orderbook & Technicals", color = PulseXColors.Secondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "$${matchedCrypto.price}", color = PulseXColors.OnSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text(text = "${matchedCrypto.change24h} (${matchedCrypto.percentChange24h}%)", color = if (matchedCrypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. PERPLEXITY AI DEEP EXPLANATION CARD ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PulseXColors.AiAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perplexity AI Synthesis",
                                color = PulseXColors.AiAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = aiSummary,
                            color = PulseXColors.OnSurface,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mini Interactive Analytics Chart Visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Consensus Trend Indicator", color = PulseXColors.Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Bullish Momentum", color = PulseXColors.PositiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Canvas(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val p = Path().apply {
                                        moveTo(0f, h * 0.8f)
                                        lineTo(w * 0.25f, h * 0.5f)
                                        lineTo(w * 0.5f, h * 0.65f)
                                        lineTo(w * 0.75f, h * 0.2f)
                                        lineTo(w, h * 0.3f)
                                    }
                                    drawPath(
                                        path = p,
                                        color = PulseXColors.PositiveGreen,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. RELATED LATEST NEWS ARTICLES ---
        if (matchedArticles.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Related News & Filings (${matchedArticles.size})",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            items(matchedArticles.take(4)) { article ->
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { onNavigateToArticle(article.id) },
                        colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = article.source, color = PulseXColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = article.publishedAt, color = PulseXColors.Outline, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.title,
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // --- 5. RELATED MARKETS & TOPICS ---
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Related Market Assets",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stocks.take(2).forEach { s ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                                .bounceClick { onNavigateToStock(s.symbol) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(s.symbol, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$${s.price}", color = PulseXColors.Primary, fontSize = 12.sp)
                            }
                        }
                    }
                    cryptos.take(2).forEach { c ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                                .bounceClick { onNavigateToCrypto(c.symbol) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(c.symbol, color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$${c.price}", color = PulseXColors.Secondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
