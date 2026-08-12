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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PulseXColors
import com.example.ui.components.scrollFadeIn
import com.example.ui.viewmodel.PulseXViewModel
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun ArticleDetailScreen(
    articleId: String,
    viewModel: PulseXViewModel,
    onStockClick: (String) -> Unit
) {
    val article = viewModel.getArticle(articleId) ?: viewModel.articles.collectAsState().value.first()
    val aiSummary by viewModel.aiSummaryState.collectAsState()
    val isAiLoading by viewModel.isAiSummaryLoading.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val isBookmarked = bookmarkedIds.contains(article.id)

    LaunchedEffect(article.id) {
        viewModel.loadAiSummary(article)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
    ) {
        // Article Header & Title
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).scrollFadeIn()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PulseXColors.PrimaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = article.title,
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Author Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = article.authorImageUrl,
                        contentDescription = article.author,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, PulseXColors.Primary, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = article.author,
                            color = PulseXColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PulseXColors.Outline,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${article.source} • ${article.publishedAgo}",
                                color = PulseXColors.Outline,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = PulseXColors.Outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.views,
                            color = PulseXColors.Outline,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Hero Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(220.dp)
                    .scrollFadeIn()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // PulseX AI Summary Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .scrollFadeIn()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PulseXColors.GlassCardBg)
                    .border(1.dp, PulseXColors.BlueGlow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = PulseXColors.AiAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PULSEX AI SUMMARY",
                                color = PulseXColors.AiAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = PulseXColors.AiAccent,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isAiLoading && aiSummary.isEmpty()) {
                        Text(
                            text = "AI summary unavailable.",
                            color = PulseXColors.OnSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        aiSummary.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PulseXColors.PositiveGreen,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = point,
                                    color = PulseXColors.OnSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Related Tickers
        if (article.relatedSymbols.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Related Tickers",
                        color = PulseXColors.Outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        article.relatedSymbols.forEach { sym ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PulseXColors.Surface)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                    .bounceClick { onStockClick(sym) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$$sym",
                                    color = PulseXColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Body Paragraphs
        items(article.fullContent) { paragraph ->
            Text(
                text = paragraph,
                color = PulseXColors.OnSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .scrollFadeIn()
            )
        }

        // Quote Box
        if (article.quote.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PulseXColors.Surface)
                        .border(1.dp, PulseXColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = article.quote,
                        color = PulseXColors.OnSurface,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Why It Matters
        if (article.whyItMatters.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PulseXColors.GlassCardBg)
                        .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "WHY IT MATTERS",
                            color = PulseXColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.whyItMatters,
                            color = PulseXColors.OnSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
