package com.example.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveMarketStatusBar
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoMarketsScreen(
    viewModel: PulseXViewModel,
    onCryptoClick: (String) -> Unit
) {
    val cryptos by viewModel.cryptos.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val marketError by viewModel.marketError.collectAsState()

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
                    text = "Cryptocurrency",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track live crypto prices, market caps & 24h volume",
                    color = PulseXColors.OnSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LiveMarketStatusBar()
            }
        }

        if (isMarketLoading && cryptos.isEmpty()) {
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
        } else if (marketError != null && cryptos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load live data", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold)
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
            items(cryptos, key = { it.symbol }) { crypto ->
            val isWatchlisted = watchlist.any { it.symbol == crypto.symbol }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onCryptoClick(crypto.symbol) }
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
                            text = crypto.symbol,
                            color = PulseXColors.Secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = crypto.name,
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Cap ${crypto.marketCap} • Vol ${crypto.volume24h}",
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${crypto.price}",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (crypto.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (crypto.isPositive) "+" else ""}${crypto.percentChange24h}%",
                                color = if (crypto.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleWatchlistCrypto(crypto) },
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
        }
    }
}
}
