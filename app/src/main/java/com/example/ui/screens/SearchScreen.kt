package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.PulseXColors
import com.example.ui.components.PulseXSearchBar
import com.example.ui.components.MiniSparklineChart
import com.example.ui.viewmodel.PulseXViewModel
import java.util.Locale
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToStock: (String) -> Unit,
    onNavigateToCrypto: (String) -> Unit,
    onNavigateToSmartSearch: (String) -> Unit,
    onNavigateToBird: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.searchFilter.collectAsState()
    val isGlowActive by viewModel.isSearchGlowActive.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.updateSearchQuery("")
        }
    }

    val filteredNews by viewModel.filteredNews.collectAsState()
    val filteredStocks by viewModel.filteredStocks.collectAsState()
    val filteredCryptos by viewModel.filteredCryptos.collectAsState()

    val filters = remember { listOf("ALL", "NEWS", "STOCKS", "CRYPTO") }

    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val isRefreshing = isNewsLoading || isMarketLoading

    // Live search suggestions as user types
    val liveSuggestions = remember(query) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.lowercase(Locale.ROOT)
            listOf(
                "$query Stock",
                "$query News",
                "$query AI Analysis",
                "$query Earnings",
                "$query Market Report",
                "AAPL",
                "NVDA",
                "BTC"
            ).filter { it.contains(q, ignoreCase = true) || q.length >= 2 }.take(5)
        }
    }

    // Expanded Trending Topics with ranking, trend indicator, and percentage increase
    val trendingTopics = remember {
        listOf(
            Triple(1, "NVIDIA Earnings", "+24.5%"),
            Triple(2, "Bitcoin Halving", "+18.2%"),
            Triple(3, "Apple Intelligence", "+15.8%"),
            Triple(4, "Tesla Robotaxi", "+14.1%"),
            Triple(5, "Gold Price Record", "+12.4%"),
            Triple(6, "Ethereum ETF Staking", "+10.9%"),
            Triple(7, "Artificial Intelligence", "+9.5%"),
            Triple(8, "Nasdaq 100 Index", "+8.2%"),
            Triple(9, "Inflation CPI Data", "+7.4%"),
            Triple(10, "OpenAI Sora Models", "+6.8%")
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refreshNews()
            viewModel.refreshMarketData()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PulseXColors.Background),
            contentPadding = PaddingValues(top = 96.dp, bottom = 120.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Spotlight Intelligence",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Bloomberg & Perplexity Universal Search",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Glowing Search Input
                    PulseXSearchBar(
                        query = query,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        placeholder = "Discover what's happening..."
                    )

                    // Live Suggestions Underneath when typing
                    AnimatedVisibility(
                        visible = query.isNotBlank() && liveSuggestions.isNotEmpty(),
                        enter = fadeIn() + androidx.compose.animation.expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Live Suggestions",
                                color = PulseXColors.Outline,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            liveSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .bounceClick {
                                            viewModel.updateSearchQuery(suggestion)
                                            onNavigateToSmartSearch(suggestion)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = PulseXColors.Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = suggestion,
                                            color = PulseXColors.OnSurface,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = PulseXColors.Outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Filter Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters, key = { it }) { f ->
                            val isSelected = filter == f
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) PulseXColors.PrimaryContainer else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSelected) PulseXColors.Primary else Color.White.copy(alpha = 0.1f), CircleShape)
                                    .bounceClick { viewModel.setSearchFilter(f) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = f,
                                    color = if (isSelected) Color.White else PulseXColors.OnSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Search Results
            if (query.isNotBlank()) {
                val birdKeywords = listOf("eagle", "falcon", "parrot", "peacock", "owl", "penguin", "sparrow", "crow", "pigeon", "duck", "flamingo", "bird", "hawk", "swan", "vulture", "hummingbird", "cardinal", "robin", "finch", "woodpecker", "macaw")
                val isBirdQuery = birdKeywords.any { query.lowercase(Locale.ROOT).contains(it) }

                if (isBirdQuery) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick { onNavigateToBird(query) },
                                colors = CardDefaults.cardColors(containerColor = PulseXColors.PrimaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.Primary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = PulseXColors.AiAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Dedicated Bird Species Profile",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Explore habitat, wingspan, AI summary, and facts for '$query'",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Quick Smart Search Deep Dive Banner
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { onNavigateToSmartSearch(query) },
                            colors = CardDefaults.cardColors(containerColor = PulseXColors.PrimaryContainer.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.Primary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Open Perplexity AI Synthesis",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Deep dive into '$query' with AI and live markets",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Stocks Section
                if (filter == "ALL" || filter == "STOCKS") {
                    if (filteredStocks.isNotEmpty()) {
                        item {
                            SectionHeader("Stocks (${filteredStocks.size})")
                        }
                        itemsIndexed(filteredStocks, key = { _, it -> it.symbol }) { index, stock ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick { onNavigateToStock(stock.symbol) }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${stock.name} (${stock.symbol})",
                                        color = PulseXColors.OnSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(130.dp)
                                    )
                                    Text(
                                        text = stock.category,
                                        color = PulseXColors.OnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                MiniSparklineChart(
                                    symbol = stock.symbol,
                                    isPositive = stock.isPositive,
                                    historyPoints = stock.historyPoints,
                                    modifier = Modifier.width(60.dp).height(28.dp)
                                )
                                Text(
                                    text = "$${stock.price}",
                                    color = PulseXColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
                        }
                    }
                    }
                }

                // Cryptos Section
                if (filter == "ALL" || filter == "CRYPTO") {
                    if (filteredCryptos.isNotEmpty()) {
                        item {
                            SectionHeader("Crypto (${filteredCryptos.size})")
                        }
                        itemsIndexed(filteredCryptos, key = { _, it -> it.symbol }) { index, crypto ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick { onNavigateToCrypto(crypto.symbol) }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${crypto.name} (${crypto.symbol})",
                                        color = PulseXColors.OnSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Cap ${crypto.marketCap}",
                                        color = PulseXColors.OnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = "$${crypto.price}",
                                    color = PulseXColors.Secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
                        }
                    }
                    }
                }

                // News Section
                if (filter == "ALL" || filter == "NEWS") {
                    if (filteredNews.isNotEmpty()) {
                        item {
                            SectionHeader("News Articles (${filteredNews.size})")
                        }
                        itemsIndexed(filteredNews, key = { _, it -> it.id }) { index, article ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                            ArticleRowItem(article) { onNavigateToArticle(article.id) }
                            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
                        }
                    }
                    }
                }
            } else {
                // Expanded Trending Searches Section when empty
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Trending Topics & Markets",
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "LIVE RANKINGS",
                                color = PulseXColors.Primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        trendingTopics.forEach { (rank, topic, pct) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .bounceClick {
                                        viewModel.updateSearchQuery(topic)
                                        onNavigateToSmartSearch(topic)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PulseXColors.GlassCardBg)
                                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#$rank",
                                            color = if (rank <= 3) PulseXColors.Primary else PulseXColors.Outline,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = topic,
                                        color = PulseXColors.OnSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = PulseXColors.PositiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pct,
                                        color = PulseXColors.PositiveGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Divider(color = Color.White.copy(alpha = 0.03f))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    androidx.compose.material3.Text(
        text = title,
        color = com.example.ui.components.PulseXColors.Outline,
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}
