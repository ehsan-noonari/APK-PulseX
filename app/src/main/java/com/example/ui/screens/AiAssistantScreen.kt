package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.data.service.GeminiApiService
import com.example.ui.components.PulseXColors
import com.example.ui.viewmodel.PulseXViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import com.example.ui.components.bounceClick
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.scrollFadeIn

// Message model for chat history
data class AiChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

// Markdown parsing representation
sealed class MessageBlock {
    data class TextBlock(val annotatedText: AnnotatedString) : MessageBlock()
    data class CodeBlock(val language: String, val code: String) : MessageBlock()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : MessageBlock()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    contextParam: String,
    viewModel: PulseXViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 1. Resolve Smart Context from Parameter
    val stocks by viewModel.stocks.collectAsState()
    val cryptos by viewModel.cryptos.collectAsState()
    val articles by viewModel.articles.collectAsState()

    var activeContextType by remember { mutableStateOf("") }
    var activeContextTitle by remember { mutableStateOf("") }
    var activeContextSubtitle by remember { mutableStateOf("") }
    var activeContextValue by remember { mutableStateOf("") }
    var activeContextPrompt by remember { mutableStateOf("") }

    LaunchedEffect(contextParam, stocks, cryptos, articles) {
        if (contextParam.isBlank() || activeContextTitle.isNotEmpty()) return@LaunchedEffect

        val parts = contextParam.split(":", limit = 2)
        if (parts.size == 2) {
            val type = parts[0]
            val value = parts[1]
            when (type) {
                "stock" -> {
                    val stock = stocks.firstOrNull { it.symbol.equals(value, ignoreCase = true) }
                    if (stock != null) {
                        activeContextType = "Stock"
                        activeContextTitle = "${stock.symbol} • ${stock.name}"
                        activeContextSubtitle = "Stock Stats: Price ${stock.price}, Change ${stock.change} (${stock.percentChange}%)"
                        activeContextValue = stock.symbol
                        activeContextPrompt = "System Context: User is viewing details for stock ${stock.name} (${stock.symbol}). Current stats are: Price: ${stock.price}, Change: ${stock.change} (${stock.percentChange}%), 52W High: ${stock.high52w}, 52W Low: ${stock.low52w}, Volume: ${stock.volume}."
                    }
                }
                "crypto" -> {
                    val crypto = cryptos.firstOrNull { it.symbol.equals(value, ignoreCase = true) }
                    if (crypto != null) {
                        activeContextType = "Crypto"
                        activeContextTitle = "${crypto.symbol} • ${crypto.name}"
                        activeContextSubtitle = "Crypto Stats: Price ${crypto.price}, 24h Change ${crypto.change24h}%"
                        activeContextValue = crypto.symbol
                        activeContextPrompt = "System Context: User is viewing details for cryptocurrency ${crypto.name} (${crypto.symbol}). Current stats are: Price: ${crypto.price}, 24h Change: ${crypto.change24h}%, Market Cap: ${crypto.marketCap}, Volume: ${crypto.volume24h}."
                    }
                }
                "article" -> {
                    val article = articles.firstOrNull { it.id == value }
                    if (article != null) {
                        activeContextType = "Article"
                        activeContextTitle = article.title
                        activeContextSubtitle = "Source: ${article.source} • ${article.category}"
                        activeContextValue = article.id
                        activeContextPrompt = "System Context: User is viewing news article titled '${article.title}' from category '${article.category}' and source '${article.source}'. Article body: ${article.description}"
                    }
                }
            }
        }
    }

    // 2. Chat state
    var inputQuery by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            AiChatMessage(
                id = "welcome",
                sender = MessageSender.AI,
                text = "Hello! I am **PulseX Copilot**, your premium financial assistant.\n\nAsk me anything about stocks, crypto, market trends, financial statements, news, economy, general tech, science, or general knowledge!"
            )
        )
    }

    var isAiLoading by remember { mutableStateOf(false) }
    var currentStreamingText by remember { mutableStateOf("") }

    // Scroll to bottom helper
    fun scrollToBottom() {
        scope.launch {
            if (chatMessages.isNotEmpty()) {
                listState.animateScrollToItem(chatMessages.size - (if (currentStreamingText.isNotEmpty()) 0 else 1))
            }
        }
    }

    // AI response stream trigger
    fun sendQuery(queryText: String) {
        if (queryText.isBlank() || isAiLoading) return
        
        // Add User Message
        chatMessages.add(AiChatMessage(id = "user-${System.currentTimeMillis()}", sender = MessageSender.USER, text = queryText))
        inputQuery = ""
        isAiLoading = true
        currentStreamingText = ""
        keyboardController?.hide()
        scrollToBottom()

        // Formulate complete prompt with Smart Context if loaded
        val systemInstruction = "You are a professional financial advisor and intelligent assistant built into PulseX. " +
                "Provide extremely accurate, concise, premium financial analysis, news explanation, comparisons, or general knowledge. " +
                "Utilize Markdown format, bold text (**), bullet points, and elegant Markdown structured tables where comparisons or stats are useful. " +
                "If the query asks about technical terms, explain them in simple words."

        val promptBuilder = StringBuilder()
        if (activeContextPrompt.isNotEmpty()) {
            promptBuilder.append(activeContextPrompt).append("\n\n")
        }
        
        // Append a simple history context for the API call (last 6 turns for safety)
        val relevantMessages = chatMessages.takeLast(6)
        relevantMessages.forEach { msg ->
            val senderLabel = if (msg.sender == MessageSender.USER) "User" else "Assistant"
            promptBuilder.append("$senderLabel: ${msg.text}\n")
        }
        promptBuilder.append("User: $queryText\nAssistant:")

        scope.launch {
            try {
                GeminiApiService.chatWithGeminiStream(
                    prompt = promptBuilder.toString(),
                    systemInstruction = systemInstruction
                )
                .onStart {
                    currentStreamingText = ""
                    scrollToBottom()
                }
                .catch { throwable ->
                    Log.e("PulseXCopilot", "Copilot submission stream error: ${throwable.message}", throwable)
                    chatMessages.add(
                        AiChatMessage(
                            id = "ai-error-${System.currentTimeMillis()}",
                            sender = MessageSender.AI,
                            text = "Error during Copilot submission: ${throwable.localizedMessage ?: "Unknown error"}"
                        )
                    )
                }
                .onCompletion {
                    isAiLoading = false
                    if (currentStreamingText.isNotEmpty()) {
                        chatMessages.add(
                            AiChatMessage(
                                id = "ai-${System.currentTimeMillis()}",
                                sender = MessageSender.AI,
                                text = currentStreamingText
                            )
                        )
                    }
                    currentStreamingText = ""
                    scrollToBottom()
                }
                .collect { chunk ->
                    currentStreamingText += chunk
                    scrollToBottom()
                }
            } catch (e: Exception) {
                Log.e("PulseXCopilot", "Copilot chat submission try-catch caught exception: ${e.message}", e)
                isAiLoading = false
                currentStreamingText = ""
                chatMessages.add(
                    AiChatMessage(
                        id = "ai-error-${System.currentTimeMillis()}",
                        sender = MessageSender.AI,
                        text = "Submission error: ${e.localizedMessage ?: "Request failed"}"
                    )
                )
            }
        }
    }

    // UI Structure
    Scaffold(
        topBar = {
            Column {
                // Top Header Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .background(PulseXColors.Background)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PulseXColors.OnSurface
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            PulseXColors.AiAccent.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(1.dp, PulseXColors.AiAccent.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = Path().apply {
                                    moveTo(w * 0.5f, h * 0.12f)
                                    quadraticTo(w * 0.5f, w * 0.5f, w * 0.88f, h * 0.5f)
                                    quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.88f)
                                    quadraticTo(w * 0.5f, w * 0.5f, w * 0.12f, h * 0.5f)
                                    quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.12f)
                                    close()
                                }
                                drawPath(path, PulseXColors.AiAccent)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PulseX Copilot",
                            color = PulseXColors.OnSurface,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Smart Context Active Banner
                AnimatedVisibility(
                    visible = activeContextTitle.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PulseXColors.PrimaryContainer.copy(alpha = 0.3f))
                            .border(1.dp, PulseXColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (activeContextType) {
                                "Stock" -> Icons.Default.TrendingUp
                                "Crypto" -> Icons.Default.CurrencyBitcoin
                                else -> Icons.Default.Newspaper
                            },
                            contentDescription = "Smart Context icon",
                            tint = PulseXColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Context: $activeContextTitle",
                                color = PulseXColors.OnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = activeContextSubtitle,
                                color = PulseXColors.Secondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = {
                                activeContextTitle = ""
                                activeContextPrompt = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Context",
                                tint = PulseXColors.Secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Message input field
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = PulseXColors.Background,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Ask Copilot anything...", color = PulseXColors.Secondary) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PulseXColors.Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = PulseXColors.OnSurface,
                            unfocusedTextColor = PulseXColors.OnSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputQuery.isNotBlank() && !isAiLoading) {
                                    sendQuery(inputQuery)
                                }
                            }
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    val isSendEnabled = inputQuery.isNotBlank() && !isAiLoading
                    val sendScale by animateFloatAsState(
                        targetValue = if (isSendEnabled) 1f else 0.9f,
                        label = "SendButtonScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(sendScale)
                            .clip(CircleShape)
                            .background(
                                if (isSendEnabled) PulseXColors.PrimaryGradient else Brush.linearGradient(
                                    colors = listOf(PulseXColors.Surface, PulseXColors.Surface)
                                )
                            )
                            .clickable(enabled = isSendEnabled) {
                                sendQuery(inputQuery)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAiLoading && currentStreamingText.isEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PulseXColors.Primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (isSendEnabled) Color.White else PulseXColors.Outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PulseXColors.Background),
            contentPadding = PaddingValues(16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chat history items
            itemsIndexed(chatMessages, key = { _, it -> it.id }) { index, message ->
                Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {
                    ChatBubbleRow(message)
                }
            }

            // Real-time streaming response bubble
            if (currentStreamingText.isNotEmpty()) {
                item {
                    ChatBubbleRow(
                        AiChatMessage(
                            id = "streaming",
                            sender = MessageSender.AI,
                            text = currentStreamingText
                        )
                    )
                }
            }

            // Thinking shimmer / skeleton load
            if (isAiLoading && currentStreamingText.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 40.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PulseXColors.AiAccent.copy(alpha = 0.2f))
                                    .border(1.dp, PulseXColors.AiAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = PulseXColors.AiAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copilot is analyzing...",
                                color = PulseXColors.Secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Beautiful Skeleton Cards
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                .background(PulseXColors.GlassCardBg.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                        ) {
                            ShimmerPulse()
                        }
                    }
                }
            }

            // Quick Actions suggestions (Only show when input is empty or conversation is just starting)
            if (chatMessages.size == 1 && !isAiLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "QUICK COPILOT SUGGESTIONS",
                            color = PulseXColors.Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        val quickActions = listOf(
                            "Summarize today's markets" to Icons.Default.TrendingUp,
                            "Explain current economy" to Icons.Default.Analytics,
                            "Analyze Bitcoin vs Ethereum" to Icons.Default.CurrencyBitcoin,
                            "Compare Apple vs Microsoft" to Icons.Default.CompareArrows,
                            "Trending tech stocks today" to Icons.Default.Bolt,
                            "Explain what is an AI agent" to Icons.Default.SmartButton
                        )

                        quickActions.forEach { (title, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PulseXColors.GlassCardBg.copy(alpha = 0.4f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                                    .bounceClick {
                                        sendQuery(title)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PulseXColors.Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = PulseXColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    color = PulseXColors.OnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Arrow",
                                    tint = PulseXColors.Outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleRow(message: AiChatMessage) {
    val isAi = message.sender == MessageSender.AI
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PulseXColors.AiAccent.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.dp, PulseXColors.AiAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            val w = size.width
                            val h = size.height
                            val path = Path().apply {
                                moveTo(w * 0.5f, h * 0.12f)
                                quadraticTo(w * 0.5f, w * 0.5f, w * 0.88f, h * 0.5f)
                                quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.88f)
                                quadraticTo(w * 0.5f, w * 0.5f, w * 0.12f, h * 0.5f)
                                quadraticTo(w * 0.5f, w * 0.5f, w * 0.5f, h * 0.12f)
                                close()
                            }
                            drawPath(path, PulseXColors.AiAccent)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PulseX Copilot",
                        color = PulseXColors.Secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Render Rich Markdown Blocks
                val parsedBlocks = remember(message.text) { parseMarkdown(message.text) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                        .background(PulseXColors.GlassCardBg.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        parsedBlocks.forEach { block ->
                            when (block) {
                                is MessageBlock.TextBlock -> {
                                    Text(
                                        text = block.annotatedText,
                                        color = PulseXColors.OnSurface,
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp
                                    )
                                }
                                is MessageBlock.CodeBlock -> {
                                    CodeBlockCard(block.language, block.code)
                                }
                                is MessageBlock.TableBlock -> {
                                    TableBlockView(block.headers, block.rows)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // User Chat Bubble (translucent glass visual)
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PulseXColors.Primary.copy(alpha = 0.35f),
                                PulseXColors.PrimaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .border(1.dp, PulseXColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = PulseXColors.OnSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Custom code block card Composable
@Composable
fun CodeBlockCard(language: String, code: String) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val displayLanguage = if (language.isBlank()) "code" else language.lowercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B15))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayLanguage,
                color = PulseXColors.Secondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.bounceClick {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Copilot Code", code))
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = PulseXColors.Primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy",
                    color = PulseXColors.Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Code Block Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFA5F3FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// Custom data tables Composable for parsed tables
@Composable
fun TableBlockView(headers: List<String>, rows: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
    ) {
        // Table Headers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            headers.forEachIndexed { idx, header ->
                Text(
                    text = header,
                    color = PulseXColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Table Rows
        rows.forEachIndexed { rowIdx, rowData ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Render cells
                headers.forEachIndexed { colIdx, _ ->
                    val cellText = rowData.getOrNull(colIdx) ?: ""
                    Text(
                        text = cellText,
                        color = PulseXColors.OnSurface,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (rowIdx < rows.size - 1) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

// Beautiful soft shimmer pulse loading component
@Composable
fun ShimmerPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = shimmerAlpha }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.5f)
                    )
                )
            )
    )
}

// Markdown parser helper logic
fun parseMarkdown(text: String): List<MessageBlock> {
    val blocks = mutableListOf<MessageBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // 1. Code block detection
        if (line.trim().startsWith("```")) {
            val lang = line.trim().substringAfter("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            blocks.add(MessageBlock.CodeBlock(lang, codeBuilder.toString().trimEnd()))
            i++ // skip ending ```
            continue
        }

        // 2. Table detection (starts with | and has at least two |)
        if (line.trim().startsWith("|") && line.trim().endsWith("|") && line.count { it == '|' } > 2) {
            val nextLine = lines.getOrNull(i + 1)
            if (nextLine != null && nextLine.trim().startsWith("|") && nextLine.contains("-")) {
                val headers = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                val rows = mutableListOf<List<String>>()
                i += 2 // skip header and separator lines
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    val rowCells = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (rowCells.isNotEmpty()) {
                        rows.add(rowCells)
                    }
                    i++
                }
                blocks.add(MessageBlock.TableBlock(headers, rows))
                continue
            }
        }

        // 3. Regular lines: collect consecutive non-code, non-table lines to merge into an annotated text block
        val textBuilder = StringBuilder()
        while (i < lines.size && !lines[i].trim().startsWith("```") && !(lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|") && lines[i].count { it == '|' } > 2 && lines.getOrNull(i + 1)?.trim()?.startsWith("|") == true && lines.getOrNull(i + 1)?.contains("-") == true)) {
            textBuilder.append(lines[i]).append("\n")
            i++
        }

        val fullText = textBuilder.toString().trimEnd()
        if (fullText.isNotEmpty()) {
            blocks.add(MessageBlock.TextBlock(parseAnnotatedText(fullText)))
        }
    }

    return blocks
}

fun parseAnnotatedText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.lines()
        lines.forEachIndexed { index, line ->
            var currentLine = line

            // Handle Headings
            if (currentLine.startsWith("### ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PulseXColors.Primary)) {
                    append(currentLine.removePrefix("### "))
                }
            } else if (currentLine.startsWith("## ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PulseXColors.Primary)) {
                    append(currentLine.removePrefix("## "))
                }
            } else if (currentLine.startsWith("# ")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 19.sp, color = PulseXColors.Primary)) {
                    append(currentLine.removePrefix("# "))
                }
            } else {
                // Parse bullet points
                if (currentLine.trim().startsWith("- ") || currentLine.trim().startsWith("* ")) {
                    append(" • ")
                    currentLine = currentLine.trim().substring(2)
                }

                // Parse bold (**text**)
                var lastIndex = 0
                val boldRegex = """\*\*(.*?)\*\*""".toRegex()
                val matches = boldRegex.findAll(currentLine)

                for (match in matches) {
                    val start = match.range.first
                    val end = match.range.last + 1

                    // append normal text before bold
                    if (start > lastIndex) {
                        append(currentLine.substring(lastIndex, start))
                    }

                    // append bold text
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PulseXColors.OnSurface)) {
                        append(match.groupValues[1])
                    }

                    lastIndex = end
                }

                if (lastIndex < currentLine.length) {
                    append(currentLine.substring(lastIndex))
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}
