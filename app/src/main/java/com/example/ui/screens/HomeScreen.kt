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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MarketIndex
import com.example.data.model.NewsArticle
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.data.model.Stock
import com.example.ui.components.LiveMarketStatusBar
import com.example.ui.components.PulseXColors
import com.example.ui.components.PulseXSearchBar
import com.example.ui.components.MiniSparklineChart
import com.example.ui.components.scrollFadeIn
import com.example.ui.components.shimmerEffect
import com.example.ui.viewmodel.PulseXViewModel
import com.example.ui.components.bounceClick

internal fun parseArticleTimestamp(article: NewsArticle): Long {
    val pubAt = article.publishedAt
    if (pubAt.isNotBlank() && pubAt != "Recently" && pubAt != "Just now") {
        val formats = listOf(
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()),
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US),
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.ENGLISH)
        )
        for (fmt in formats) {
            try {
                val date = fmt.parse(pubAt)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {}
        }
    }

    // Fallback to publishedAgo parsing
    val lower = article.publishedAgo.lowercase()
    val now = System.currentTimeMillis()
    val secondsAgo = when {
        lower.contains("just") || lower.contains("now") -> 0L
        lower.contains("m") -> {
            val num = lower.filter { it.isDigit() }.toLongOrNull() ?: 10L
            num * 60L
        }
        lower.contains("h") -> {
            val num = lower.filter { it.isDigit() }.toLongOrNull() ?: 1L
            num * 3600L
        }
        lower.contains("d") -> {
            val num = lower.filter { it.isDigit() }.toLongOrNull() ?: 1L
            num * 86400L
        }
        lower.contains("yesterday") -> 86400L
        else -> 3600L
    }
    return now - (secondsAgo * 1000L)
}

internal fun parseViews(views: String): Long {
    val clean = views.lowercase().replace(",", "").trim()
    return when {
        clean.endsWith("k") -> {
            val num = clean.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            (num * 1000).toLong()
        }
        clean.endsWith("m") -> {
            val num = clean.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
            (num * 1_000_000).toLong()
        }
        else -> clean.filter { it.isDigit() }.toLongOrNull() ?: 1000L
    }
}

internal fun getGreetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
    return "$greeting."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PulseXViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToMarkets: () -> Unit,
    onNavigateToCrypto: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToStock: (String) -> Unit,
    onNavigateToBird: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val greetingText by androidx.compose.runtime.produceState(initialValue = getGreetingText()) {
        while (true) {
            value = getGreetingText()
            kotlinx.coroutines.delay(60_000L)
        }
    }
    val marketIndices by viewModel.marketIndices.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val newsError by viewModel.newsError.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()
    val marketError by viewModel.marketError.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()

    val selectedCategory by viewModel.selectedHomeCategory.collectAsState()

    val categories = remember {
        listOf(
            "📰 All",
            "🔥 Trending",
            "🌍 World",
            "💹 Markets",
            "🪙 Crypto",
            "🤖 AI",
            "💼 Business",
            "🐘 Animals",
            "🐦 Birds",
            "🌿 Nature",
            "🧬 Science",
            "💻 Technology",
            "🏥 Health",
            "⚽ Sports",
            "🎬 Entertainment"
        )
    }

    val newsSortFilter by viewModel.newsSortFilter.collectAsState()

    val filteredArticles = remember(articles, selectedCategory, newsSortFilter) {
        val baseList = articles

        // Deduplicate / group similar stories to avoid duplicate news
        val uniqueList = mutableListOf<NewsArticle>()
        val seenTitles = mutableSetOf<String>()
        for (article in baseList) {
            val normalized = article.title.take(20).lowercase()
            if (!seenTitles.contains(normalized)) {
                seenTitles.add(normalized)
                uniqueList.add(article)
            }
        }

        when (newsSortFilter) {
            "Newest First" -> uniqueList.sortedByDescending { parseArticleTimestamp(it) }
            "Oldest First" -> uniqueList.sortedBy { parseArticleTimestamp(it) }
            "Most Popular", "Most Viewed" -> uniqueList.sortedByDescending { parseViews(it.views) }
            "Trending" -> uniqueList.sortedWith(compareByDescending<NewsArticle> { it.isBreaking }
                .thenByDescending { parseViews(it.views) }
                .thenByDescending { parseArticleTimestamp(it) })
            "Editor's Picks" -> uniqueList.sortedByDescending { it.summaryPoints.size + if (it.author.isNotBlank()) 5 else 0 }
            "AI Recommended" -> uniqueList.sortedByDescending {
                val cat = it.category.lowercase()
                val title = it.title.lowercase()
                if (cat.contains("tech") || cat.contains("ai") || cat.contains("crypto") || title.contains("ai") || title.contains("technology")) 10 else 1
            }
            else -> uniqueList
        }
    }

    val breakingArticle = remember(filteredArticles) { filteredArticles.firstOrNull { it.isBreaking } ?: filteredArticles.firstOrNull() }
    val latestArticles = remember(filteredArticles, breakingArticle) { filteredArticles.filter { it.id != breakingArticle?.id } }

    val listState = rememberLazyListState()
    val isRefreshing = isNewsLoading || isMarketLoading

    val scrollPending by viewModel.homeScrollToTopPending.collectAsState()
    val logoRefreshPending by viewModel.homeLogoRefreshPending.collectAsState()

    LaunchedEffect(scrollPending) {
        if (scrollPending) {
            listState.animateScrollToItem(0)
            viewModel.refreshNews()
            viewModel.refreshMarketData()
            viewModel.setHomeScrollToTopPending(false)
        }
    }

    LaunchedEffect(logoRefreshPending) {
        if (logoRefreshPending) {
            viewModel.refreshNews()
            viewModel.refreshMarketData()
            viewModel.clearLogoRefresh()
        }
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(PulseXColors.Background),
            contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
        ) {
        // Greeting & Search Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = greetingText,
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Locked Home Search Bar
                PulseXSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    placeholder = "Discover what's happening...",
                    enabled = false,
                    onClick = onNavigateToSearch
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontally scrollable 14 content categories
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) PulseXColors.PrimaryContainer else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) PulseXColors.Primary else Color.White.copy(alpha = 0.1f), CircleShape)
                                .bounceClick {
                                    viewModel.selectHomeCategory(cat)
                                    if (cat.contains("Markets")) onNavigateToMarkets()
                                    else if (cat.contains("Crypto")) onNavigateToCrypto()
                                }
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

        // Market Overview Section
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Market Overview",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.bounceClick { onNavigateToMarkets() }
                    ) {
                        Text(
                            text = "View All",
                            color = PulseXColors.Primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View All",
                            tint = PulseXColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Market Overview Horizontal Tickers
                if (isMarketLoading && marketIndices.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerEffect()
                            )
                        }
                    }
                } else if (marketError != null && marketIndices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Unable to load market data", color = PulseXColors.OnSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(marketIndices, key = { _, item -> item.symbol }) { index, marketIndex ->
                            Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                                MarketTickerCard(
                                    index = marketIndex,
                                    isWatchlisted = watchlist.any { it.symbol == marketIndex.symbol },
                                    onToggleWatchlist = { viewModel.toggleWatchlistIndex(marketIndex) }
                                ) {
                                    if (marketIndex.type == "Crypto") onNavigateToCrypto() else onNavigateToMarkets()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bird Species Profile Banner if Birds category selected
        if (selectedCategory.contains("Birds")) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { onNavigateToBird("Eagle") },
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PulseXColors.PrimaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.Primary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Bird Species Profiles & Migration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Explore Eagles, Falcons, Owls, Parrots & more", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Breaking News Hero Banner
        if (isNewsLoading && articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .shimmerEffect()
                )
            }
        } else if (newsError != null && articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load latest news", color = PulseXColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refreshNews() },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retry News", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            breakingArticle?.let { article ->
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .scrollFadeIn()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PulseXColors.Secondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Breaking News",
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(24.dp))
                                .bounceClick { onNavigateToArticle(article.id) }
                        ) {
                            AsyncImage(
                                model = article.imageUrl,
                                contentDescription = article.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Dark Gradient Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                PulseXColors.Background.copy(alpha = 0.6f),
                                                PulseXColors.Background.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PulseXColors.PrimaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = article.category,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "JUST IN",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = article.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Time",
                                        tint = PulseXColors.Outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = article.publishedAgo,
                                        color = PulseXColors.Outline,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = " • ",
                                        color = PulseXColors.Outline,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = article.source,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Latest Stories List with Filter Button
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Latest Stories",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    var showFilterMenu by remember { androidx.compose.runtime.mutableStateOf(false) }

                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                                .bounceClick { showFilterMenu = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Filter",
                                    tint = PulseXColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = newsSortFilter,
                                    color = PulseXColors.OnSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(PulseXColors.Surface)
                        ) {
                            val options = listOf(
                                "Newest First",
                                "Oldest First",
                                "Most Popular",
                                "Most Viewed",
                                "Trending",
                                "Editor's Picks",
                                "AI Recommended"
                            )
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (newsSortFilter == option) PulseXColors.Primary else PulseXColors.OnSurface,
                                            fontWeight = if (newsSortFilter == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setNewsSortFilter(option)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        itemsIndexed(latestArticles, key = { _, it -> it.id }) { index, article ->
            Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                Column {
                    ArticleRowItem(article) { onNavigateToArticle(article.id) }
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Trending Symbols
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .scrollFadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Trending Symbols",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isMarketLoading && stocks.isEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .shimmerEffect()
                                    )
                                }
                            }
                        } else if (marketError != null && stocks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Unable to load trending symbols", color = PulseXColors.OnSurfaceVariant, fontSize = 12.sp)
                            }
                        } else {
                            stocks.take(5).forEachIndexed { index, stock ->
                                StockCardRow(
                                    stock = stock,
                                    index = index,
                                    onNavigateToStock = onNavigateToStock
                                )
                                if (index < stocks.take(5).size - 1) {
                                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // PulseX Pro Promo Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PulseXColors.GlassCardBg)
                    .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Pro Insights",
                            tint = PulseXColors.Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PulseX Pro",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Unlock real-time market data, advanced charting, and exclusive analyst reports.",
                        color = PulseXColors.Outline,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Upgrade Now",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .bounceClick { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = PulseXColors.OnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickChipWithIcon(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .bounceClick { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = PulseXColors.AiAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = PulseXColors.OnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MarketTickerCard(index: MarketIndex, isWatchlisted: Boolean, onToggleWatchlist: () -> Unit, onClick: () -> Unit) {
    var previousValue by remember { mutableStateOf(index.value) }
    var isPriceUpdated by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(index.value) {
        if (previousValue != index.value) {
            isPriceUpdated = true
            previousValue = index.value
            kotlinx.coroutines.delay(300)
            isPriceUpdated = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isPriceUpdated -> 1.03f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.3f else 0.05f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "borderAlpha"
    )

    val changeValue = remember(index.change) {
        try {
            index.change.replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    val trendColor = remember(changeValue) {
        when {
            changeValue > 0.0 -> PulseXColors.PositiveGreen
            changeValue < 0.0 -> PulseXColors.NegativeRed
            else -> PulseXColors.Secondary
        }
    }

    Box(
        modifier = Modifier
            .width(190.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                cameraDistance = 8f * density
            }
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141824).copy(alpha = 0.88f))
            .border(1.dp, if (isPressed) PulseXColors.Primary.copy(alpha = borderAlpha) else Color.White.copy(alpha = borderAlpha), RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = index.name,
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isWatchlisted) PulseXColors.Primary else PulseXColors.Outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = index.value,
                color = if (isPriceUpdated) trendColor else PulseXColors.OnSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (index.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = "Trend",
                    tint = trendColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = index.change,
                    color = trendColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StockCardRow(
    stock: Stock,
    index: Int,
    onNavigateToStock: (String) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var previousPrice by remember { mutableStateOf(stock.price) }
    var isPriceUpdated by remember { mutableStateOf(false) }

    LaunchedEffect(stock.price) {
        if (previousPrice != stock.price) {
            isPriceUpdated = true
            previousPrice = stock.price
            kotlinx.coroutines.delay(350)
            isPriceUpdated = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isPriceUpdated -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stockCardScale"
    )

    val elevationAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0.02f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevationAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = elevationAlpha))
            .border(
                width = 1.dp,
                color = if (isPressed) PulseXColors.Primary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onNavigateToStock(stock.symbol)
                        }
                    }
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PulseXColors.Surface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$${stock.symbol}",
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stock.name,
                    color = PulseXColors.OnSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(90.dp)
                )
            }

            MiniSparklineChart(
                symbol = stock.symbol,
                isPositive = stock.isPositive,
                historyPoints = stock.historyPoints,
                modifier = Modifier
                    .width(60.dp)
                    .height(28.dp)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${stock.price}",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${if (stock.isPositive) "+" else ""}${stock.percentChange}%",
                    color = if (stock.isPositive) PulseXColors.PositiveGreen else PulseXColors.NegativeRed,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ArticleRowItem(article: NewsArticle, onClick: () -> Unit) {
    val readingTime = remember(article.fullContent) {
        val words = article.fullContent.sumOf { it.split(" ").size }
        val mins = (words / 150).coerceAtLeast(2)
        "$mins min read"
    }

    var isImageLoaded by remember { mutableStateOf(false) }
    val imageAlpha by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0f,
        animationSpec = tween(450),
        label = "articleImageFade"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                onSuccess = { isImageLoaded = true },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = imageAlpha }
            )
            if (article.summaryPoints.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PulseXColors.Primary.copy(alpha = 0.9f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Summary",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.category.uppercase(),
                    color = PulseXColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                AsyncImage(
                    model = article.authorImageUrl,
                    contentDescription = article.source,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = article.title,
                color = PulseXColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.source,
                    color = PulseXColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " • ",
                    color = PulseXColors.Outline,
                    fontSize = 12.sp
                )
                Text(
                    text = article.publishedAgo,
                    color = PulseXColors.Outline,
                    fontSize = 12.sp
                )
                Text(
                    text = " • ",
                    color = PulseXColors.Outline,
                    fontSize = 12.sp
                )
                Text(
                    text = readingTime,
                    color = PulseXColors.Outline,
                    fontSize = 12.sp
                )
            }
        }
    }
}
