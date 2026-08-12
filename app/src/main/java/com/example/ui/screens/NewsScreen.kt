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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import com.example.data.model.NewsArticle
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit
) {
    val articles by viewModel.articles.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val newsError by viewModel.newsError.collectAsState()
    val isNewsEmpty by viewModel.isNewsEmpty.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = remember { listOf("ALL", "WORLD", "BUSINESS", "MARKETS", "TECH", "SCIENCE", "SPORTS", "CRYPTO", "ECONOMY") }

    val newsSortFilter by viewModel.newsSortFilter.collectAsState()

    val filteredArticles = remember(articles, selectedCategory, newsSortFilter) {
        val baseList = if (selectedCategory == "ALL") {
            articles
        } else {
            articles.filter { article ->
                val cat = article.category.uppercase()
                when (selectedCategory) {
                    "WORLD" -> cat == "WORLD" || cat == "GLOBAL"
                    "BUSINESS" -> cat == "BUSINESS" || cat == "ECONOMY"
                    "TECH" -> cat == "TECH" || cat == "TECHNOLOGY"
                    "MARKETS" -> cat == "MARKETS"
                    "SCIENCE" -> cat == "SCIENCE"
                    "SPORTS" -> cat == "SPORTS"
                    "CRYPTO" -> cat == "CRYPTO"
                    "ECONOMY" -> cat == "ECONOMY" || cat == "BUSINESS"
                    else -> cat == selectedCategory
                }
            }
        }

        // Deduplicate
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
            "Trending" -> uniqueList.sortedWith(
                compareByDescending<NewsArticle> { it.isBreaking }
                    .thenByDescending { parseViews(it.views) }
                    .thenByDescending { parseArticleTimestamp(it) }
            )
            "Editor's Picks" -> uniqueList.sortedByDescending { it.summaryPoints.size + if (it.author.isNotBlank()) 5 else 0 }
            "AI Recommended" -> uniqueList.sortedByDescending {
                val cat = it.category.lowercase()
                val title = it.title.lowercase()
                if (cat.contains("tech") || cat.contains("ai") || cat.contains("crypto") || title.contains("ai") || title.contains("technology")) 10 else 1
            }
            else -> uniqueList
        }
    }

    PullToRefreshBox(
        isRefreshing = isNewsLoading,
        onRefresh = { viewModel.refreshNews() },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Financial News Feed",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live global economic updates & market coverage",
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshNews() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PulseXColors.GlassCardBg)
                            .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh News",
                            tint = PulseXColors.PrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it }) { category ->
                        val isSelected = selectedCategory == category
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
                                    shape = RoundedCornerShape(20.dp),
                                    clip = false,
                                    ambientColor = if (isSelected) PulseXColors.Primary.copy(alpha = 0.2f) else Color.Transparent,
                                    spotColor = if (isSelected) PulseXColors.Primary.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .background(backgroundBrush)
                                .border(
                                    width = 1.dp,
                                    brush = borderBrush,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (_: Exception) {}
                                        selectedCategory = category
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else PulseXColors.OnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (isNewsLoading && articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PulseXColors.PrimaryContainer,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Fetching live financial news...",
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else if (newsError != null && articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = PulseXColors.NegativeRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Unable to load news feed",
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = newsError ?: "Check your network connection and try again.",
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshNews() },
                            colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (filteredArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles found in $selectedCategory",
                        color = PulseXColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            itemsIndexed(filteredArticles, key = { _, it -> it.id }) { index, article ->
            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                ArticleRowItem(article) { onNavigateToArticle(article.id) }
                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
            }
            }
        }
    }
}

}
