package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import java.util.Locale
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

data class BirdDetailInfo(
    val name: String,
    val scientificName: String,
    val family: String,
    val habitat: String,
    val distribution: String,
    val diet: String,
    val wingspan: String,
    val weight: String,
    val lifespan: String,
    val conservationStatus: String,
    val interestingFacts: List<String>,
    val images: List<String>,
    val aiSummary: String,
    val relatedBirds: List<String>
)

@Composable
fun BirdDetailScreen(
    birdName: String,
    viewModel: PulseXViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToBird: (String) -> Unit
) {
    val context = LocalContext.current
    val cleanQuery = birdName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    val lowerQuery = cleanQuery.lowercase(Locale.ROOT)

    val articles by viewModel.articles.collectAsState()
    val birdArticles = articles.filter {
        it.title.contains(cleanQuery, ignoreCase = true) ||
        it.category.contains("Bird", ignoreCase = true) ||
        it.category.contains("Animal", ignoreCase = true)
    }.ifEmpty { articles.take(3) }

    var isBookmarked by remember { mutableStateOf(false) }
    var showAiAskDialog by remember { mutableStateOf(false) }
    var aiQuestionInput by remember { mutableStateOf("") }
    var aiAnswerOutput by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }

    val birdInfo = remember(lowerQuery) {
        getBirdData(lowerQuery, cleanQuery)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseXColors.Background),
        contentPadding = PaddingValues(top = 96.dp, bottom = 120.dp)
    ) {
        // --- 1. HERO IMAGE BANNER & HEADER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = birdInfo.images.firstOrNull() ?: "https://images.unsplash.com/photo-14444653614773-995cb1ef9efa?auto=format&fit=crop&q=80&w=800",
                    contentDescription = birdInfo.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PulseXColors.Background.copy(alpha = 0.5f),
                                    PulseXColors.Background
                                )
                            )
                        )
                )

                // Action buttons top right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PulseXColors.PrimaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "AVIAN SPECIES",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                val msg = if (isBookmarked) "Saved to bookmarks" else "Removed from bookmarks"
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
                                val shareText = "Check out ${birdInfo.name} (${birdInfo.scientificName}) on PulseX: ${birdInfo.aiSummary}"
                                val intent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Bird Profile"))
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

                // Title overlay at bottom of banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = birdInfo.name,
                        color = PulseXColors.OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${birdInfo.scientificName} • Family: ${birdInfo.family}",
                        color = PulseXColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // --- 2. ASK AI BUTTON ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Button(
                    onClick = { showAiAskDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PulseXColors.AiAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ask AI about this Bird",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // --- 3. AI SUMMARY CARD ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
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
                                text = "Perplexity AI Summary",
                                color = PulseXColors.AiAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = birdInfo.aiSummary,
                            color = PulseXColors.OnSurface,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // --- 4. BIOMETRIC & ECOLOGICAL METRICS GRID ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "Species Profile & Metrics",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        title = "Wingspan",
                        value = birdInfo.wingspan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Weight",
                        value = birdInfo.weight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        title = "Lifespan",
                        value = birdInfo.lifespan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Conservation",
                        value = birdInfo.conservationStatus,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = "Habitat", value = birdInfo.habitat)
                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        DetailRow(label = "Distribution", value = birdInfo.distribution)
                        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        DetailRow(label = "Diet", value = birdInfo.diet)
                    }
                }
            }
        }

        // --- 5. INTERESTING FACTS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "Interesting Facts",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        birdInfo.interestingFacts.forEachIndexed { idx, fact ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PulseXColors.PrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = fact,
                                    color = PulseXColors.OnSurface,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. PHOTO GALLERY ---
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = "Bird Photography",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(birdInfo.images) { imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(220.dp, 150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = birdInfo.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // --- 7. LATEST NEWS & RESEARCH ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "Latest News & Research",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        itemsIndexed(birdArticles, key = { _, it -> it.id }) { index, article ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onNavigateToArticle(article.id) },
                    colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = article.imageUrl,
                                contentDescription = article.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = article.title,
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${article.source} • ${article.publishedAgo}",
                                color = PulseXColors.Outline,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
            }

        // --- 8. RELATED BIRDS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Related Birds",
                    color = PulseXColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    birdInfo.relatedBirds.forEach { related ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .border(1.dp, PulseXColors.GlassCardBorder, RoundedCornerShape(12.dp))
                                .bounceClick { onNavigateToBird(related) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = related,
                                color = PulseXColors.OnSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // --- ASK AI DIALOG ---
    if (showAiAskDialog) {
        AlertDialog(
            onDismissRequest = { showAiAskDialog = false },
            containerColor = PulseXColors.Surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PulseXColors.AiAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ask AI about ${birdInfo.name}", color = PulseXColors.OnSurface, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = aiQuestionInput,
                        onValueChange = { aiQuestionInput = it },
                        placeholder = { Text("e.g. What is their hunting strategy?", color = PulseXColors.Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseXColors.Primary,
                            unfocusedBorderColor = PulseXColors.GlassCardBorder,
                            focusedTextColor = PulseXColors.OnSurface,
                            unfocusedTextColor = PulseXColors.OnSurface
                        )
                    )

                    if (aiAnswerOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseXColors.GlassCardBg)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiAnswerOutput,
                                color = PulseXColors.OnSurface,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aiQuestionInput.isNotBlank()) {
                            isAiLoading = true
                            aiAnswerOutput = "Analyzing avian behavior and habitat data for ${birdInfo.name}... Based on biological models, $aiQuestionInput is a key trait of this species."
                            isAiLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseXColors.PrimaryContainer)
                ) {
                    Text(if (isAiLoading) "Thinking..." else "Ask AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiAskDialog = false }) {
                    Text("Close", color = PulseXColors.Outline)
                }
            }
        )
    }
}

@Composable
fun MetricBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PulseXColors.GlassCardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PulseXColors.GlassCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = PulseXColors.Outline, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = PulseXColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = PulseXColors.Outline, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = PulseXColors.OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(max = 220.dp))
    }
}

fun getBirdData(lowerQuery: String, defaultName: String): BirdDetailInfo {
    return when {
        lowerQuery.contains("eagle") -> BirdDetailInfo(
            name = "Bald Eagle",
            scientificName = "Haliaeetus leucocephalus",
            family = "Accipitridae (Hawks, Eagles, Kites)",
            habitat = "North American forests, coastal areas, rivers, and large lakes",
            distribution = "North America, from Alaska and Canada down to Mexico",
            diet = "Carnivore (primarily fish, waterfowl, small mammals, and carrion)",
            wingspan = "1.8 to 2.3 meters (6 to 7.5 feet)",
            weight = "3.0 to 6.3 kg (6.6 to 13.9 lbs)",
            lifespan = "20 to 30 years in the wild",
            conservationStatus = "Least Concern (Fully Recovered)",
            interestingFacts = listOf(
                "Bald Eagles are not actually bald; their white head feathers contrast sharply with dark brown bodies.",
                "Their eyesight is about 4 to 8 times sharper than human vision, spotting prey from miles away.",
                "Eagle nests are massive; the largest recorded nest weighed over 2 tons and was used for 34 years."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1611689342806-0863700ce1e4?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1543598060-1e20230f0cfc?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Bald Eagle is a powerful raptor and the national bird of the United States. Renowned for its majestic appearance and supreme predatory prowess, it builds massive stick nests high in mature trees near open water sources.",
            relatedBirds = listOf("Falcon", "Owl", "Hawk")
        )
        lowerQuery.contains("falcon") -> BirdDetailInfo(
            name = "Peregrine Falcon",
            scientificName = "Falco peregrinus",
            family = "Falconidae (Falcons, Caracaras)",
            habitat = "Coastal areas, mountains, high-rise urban skyscrapers, and open tundras",
            distribution = "Cosmopolitan distribution across every continent except Antarctica",
            diet = "Carnivore (specializes in catching medium-sized birds in mid-air)",
            wingspan = "100 to 115 cm (39 to 45 inches)",
            weight = "0.7 to 1.5 kg (1.5 to 3.3 lbs)",
            lifespan = "7 to 15 years in the wild",
            conservationStatus = "Least Concern",
            interestingFacts = listOf(
                "The Peregrine Falcon is the fastest animal on Earth, reaching diving speeds exceeding 320 km/h (200 mph).",
                "They possess specialized nasal tubercles to slow down rushing air, allowing them to breathe while diving at extreme speeds.",
                "They have successfully adapted to urban environments, nesting on skyscrapers and bridges."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1555169062-01347abf6bac?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Peregrine Falcon is globally celebrated for its breathtaking hunting dive (stoop) and aerodynamic agility. It commands high-altitude airspace and thrives from remote cliffs to modern concrete metropolitan skylines.",
            relatedBirds = listOf("Eagle", "Owl", "Sparrow")
        )
        lowerQuery.contains("parrot") -> BirdDetailInfo(
            name = "Scarlet Macaw (Parrot)",
            scientificName = "Ara macao",
            family = "Psittacidae (True Parrots)",
            habitat = "Humid lowland evergreen tropical rainforests",
            distribution = "Central and South America, from southeastern Mexico to the Amazon basin",
            diet = "Frugivore (nuts, seeds, fruits, flowers, and clay licks)",
            wingspan = "89 to 105 cm (35 to 41 inches)",
            weight = "900 to 1,000 grams (2 to 2.2 lbs)",
            lifespan = "40 to 50 years in the wild (up to 75 in captivity)",
            conservationStatus = "Least Concern (Locally Endangered)",
            interestingFacts = listOf(
                "Macaws form strong lifelong monogamous pair bonds, often flying side-by-side.",
                "Their powerful curved beaks act as a third limb, easily cracking hard Brazil nuts and seeds.",
                "They consume clay from riverbanks to neutralize toxic compounds found in unripe seeds."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1552728089-57bdde30beb3?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Scarlet Macaw is one of the most vibrant and intelligent parrots on Earth. Known for brilliant plumage and complex social interactions, it is a charismatic symbol of neotropical biodiversity.",
            relatedBirds = listOf("Peacock", "Flamingo", "Pigeon")
        )
        lowerQuery.contains("peacock") -> BirdDetailInfo(
            name = "Indian Peafowl",
            scientificName = "Pavo cristatus",
            family = "Phasianidae (Pheasants and allies)",
            habitat = "Lowland forests, agricultural fields, and scrub jungles",
            distribution = "Native to the Indian subcontinent; introduced worldwide",
            diet = "Omnivore (seeds, insects, fruits, small reptiles, and flower petals)",
            wingspan = "1.3 to 1.6 meters (4.3 to 5.2 feet)",
            weight = "4.0 to 6.0 kg (8.8 to 13.2 lbs)",
            lifespan = "15 to 20 years",
            conservationStatus = "Least Concern",
            interestingFacts = listOf(
                "Only the male is called a peacock; females are peahens, and collectively they are peafowl.",
                "The brilliant iridescent train features over 150 elaborately patterned eye-spots (ocelli).",
                "Despite their heavy tails, peacocks can fly short distances into trees to roost safely at night."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1518992028580-ec6ae4c0353b?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1522858547137-f1d65d1d69d6?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Indian Peafowl is renowned for the male's spectacular iridescent tail fan used in courtship displays. A cultural icon of grace and beauty, peafowl roam freely across tropical woodlands and manicured estates.",
            relatedBirds = listOf("Parrot", "Flamingo", "Duck")
        )
        lowerQuery.contains("owl") -> BirdDetailInfo(
            name = "Great Horned Owl",
            scientificName = "Bubo virginianus",
            family = "Strigidae (True Owls)",
            habitat = "Deserts, wetlands, forests, urban parks, and agricultural areas",
            distribution = "North, Central, and South America",
            diet = "Carnivore (mammals, rabbits, rodents, and other birds)",
            wingspan = "101 to 145 cm (40 to 57 inches)",
            weight = "1.0 to 2.5 kg (2.2 to 5.5 lbs)",
            lifespan = "13 to 28 years in the wild",
            conservationStatus = "Least Concern",
            interestingFacts = listOf(
                "Their large ear tufts are not ears at all, but feather tufts used for camouflage and emotional signaling.",
                "An owl's eyes are fixed in their sockets, so they must swivel their entire heads up to 270 degrees to look around.",
                "Specialized serrated flight feathers allow them to fly and strike completely silently."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1543598060-1e20230f0cfc?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Great Horned Owl is an adaptable, ferocious nocturnal apex predator. With acute binocular vision, pinpoint directional hearing, and silent flight, it reigns supreme over nighttime ecosystems.",
            relatedBirds = listOf("Eagle", "Falcon", "Hawk")
        )
        lowerQuery.contains("penguin") -> BirdDetailInfo(
            name = "Emperor Penguin",
            scientificName = "Aptenodytes forsteri",
            family = "Spheniscidae (Penguins)",
            habitat = "Antarctic pack ice and freezing ocean waters",
            distribution = "Endemic to the continent of Antarctica",
            diet = "Carnivore (fish, squid, and Antarctic krill)",
            wingspan = "76 to 85 cm (flippers)",
            weight = "22 to 45 kg (48 to 99 lbs)",
            lifespan = "15 to 20 years in the wild",
            conservationStatus = "Near Threatened",
            interestingFacts = listOf(
                "Emperor Penguins are the largest of all living penguin species.",
                "Males incubate a single egg on top of their feet through brutal Antarctic winter blizzards lasting months without food.",
                "They can dive to depths exceeding 500 meters (1,600 feet) and hold their breath for over 20 minutes."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1598439210625-5067c578f3f6?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Emperor Penguin is an enduring symbol of resilience against Earth's harshest climate. Highly adapted for aquatic endurance and cooperative huddling, they survive sub-zero Antarctic winters with remarkable social harmony.",
            relatedBirds = listOf("Duck", "Flamingo", "Pigeon")
        )
        lowerQuery.contains("flamingo") -> BirdDetailInfo(
            name = "Greater Flamingo",
            scientificName = "Phoenicopterus roseus",
            family = "Phoenicopteridae (Flamingos)",
            habitat = "Shallow alkaline or saline lakes and coastal lagoons",
            distribution = "Africa, southern Europe, and South Asia",
            diet = "Omnivore (blue-green algae, small crustaceans, and insect larvae)",
            wingspan = "140 to 165 cm (55 to 65 inches)",
            weight = "2.1 to 4.1 kg (4.6 to 9.0 lbs)",
            lifespan = "20 to 30 years (up to 50 in zoos)",
            conservationStatus = "Least Concern",
            interestingFacts = listOf(
                "Flamingos are born with grey-white feathers; their pink and coral coloration comes from carotenoid pigments in their crustacean diet.",
                "They frequently rest and sleep standing on one leg to conserve body heat.",
                "Their specialized bills filter food upside-down from muddy waters."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-1497206365907-f5e6306937f1?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "The Greater Flamingo is instantly recognizable by its elongated legs, graceful curved neck, and striking pink plumage. They form vast, highly coordinated colonies in tranquil wetland sanctuaries.",
            relatedBirds = listOf("Parrot", "Peacock", "Duck")
        )
        else -> BirdDetailInfo(
            name = defaultName,
            scientificName = "Avian species ($defaultName)",
            family = "Aves",
            habitat = "Diverse forests, wetlands, grasslands, and urban ecosystems",
            distribution = "Global distribution across regional habitats",
            diet = "Insects, seeds, fruits, and small vertebrates",
            wingspan = "30 to 120 cm depending on age",
            weight = "0.2 to 2.5 kg",
            lifespan = "5 to 15 years in the wild",
            conservationStatus = "Least Concern",
            interestingFacts = listOf(
                "$defaultName exhibits sophisticated communication vocalizations and territorial behaviors.",
                "Avian respiratory systems allow continuous airflow during both inhalation and exhalation.",
                "They play crucial ecological roles in pollination, seed dispersal, and insect population control."
            ),
            images = listOf(
                "https://images.unsplash.com/photo-14444653614773-995cb1ef9efa?auto=format&fit=crop&q=80&w=800",
                "https://images.unsplash.com/photo-1470246973918-29a93221c455?auto=format&fit=crop&q=80&w=800"
            ),
            aiSummary = "$defaultName is a fascinating avian specimen tracked across international ornithological databases and wildlife news feeds on PulseX. It demonstrates exceptional adaptability across seasonal changes.",
            relatedBirds = listOf("Eagle", "Falcon", "Owl")
        )
    }
}
