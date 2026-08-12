package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NewsBackendService {

    private const val TAG = "NewsBackendService"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    // High resolution fallback images by category
    private val categoryFallbackImages = mapOf(
        "World" to listOf(
            "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1444653614773-995cb1ef9efa?auto=format&fit=crop&q=80&w=800"
        ),
        "Markets" to listOf(
            "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1642543492481-44e81e3914a7?auto=format&fit=crop&q=80&w=800"
        ),
        "Business" to listOf(
            "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&q=80&w=800"
        ),
        "Tech" to listOf(
            "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&q=80&w=800"
        ),
        "Crypto" to listOf(
            "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1622979135225-d2ba269bc1bd?auto=format&fit=crop&q=80&w=800"
        ),
        "Economy" to listOf(
            "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&q=80&w=800"
        ),
        "Science" to listOf(
            "https://images.unsplash.com/photo-1507668077129-56e32842fceb?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&q=80&w=800"
        ),
        "Sports" to listOf(
            "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=800"
        ),
        "Animals" to listOf(
            "https://images.unsplash.com/photo-1534567153574-2b12153a87f0?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?auto=format&fit=crop&q=80&w=800"
        ),
        "Birds" to listOf(
            "https://images.unsplash.com/photo-14444653614773-995cb1ef9efa?auto=format&fit=crop&q=80&w=800",
            "https://images.unsplash.com/photo-1611689342806-0863700ce1e4?auto=format&fit=crop&q=80&w=800"
        )
    )

    private val authorAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200"
    )

    /**
     * Main entry point to fetch real news from news API / live feeds.
     */
    suspend fun fetchRealNewsArticles(category: String = "Trending"): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = try { BuildConfig.NEWS_API_KEY } catch (e: Exception) { "" }
            var articles: List<NewsArticle> = emptyList()

            // 1. Try NewsAPI if API key is provided and valid
            if (isKeyValid(apiKey)) {
                Log.d(TAG, "Fetching news via News API endpoint for category: $category")
                articles = fetchFromNewsApi(apiKey, category)
            }

            // 2. Fallback / supplementary fetch from live international RSS feeds
            if (articles.isEmpty()) {
                Log.d(TAG, "Fetching news via live international search feeds for category: $category")
                articles = fetchFromLiveRssFeeds(category)
            }

            // 3. Deduplicate articles by title and URL
            val deduplicated = articles
                .distinctBy { it.title.trim().lowercase() }
                .distinctBy { if (it.articleUrl.isNotBlank()) it.articleUrl else it.id }
                .take(40)

            if (deduplicated.isEmpty()) {
                Result.failure(Exception("No news articles returned from backend service for category: $category"))
            } else {
                // Ensure at least one breaking article flag
                val processed = deduplicated.mapIndexed { index, article ->
                    if (index == 0) article.copy(isBreaking = true) else article
                }
                Result.success(processed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching real news articles for category: $category", e)
            Result.failure(e)
        }
    }

    private fun isKeyValid(key: String?): Boolean {
        if (key == null || key.trim().isEmpty()) return false
        val lower = key.lowercase().trim()
        return !lower.contains("placeholder") && !lower.contains("default") && lower != "news_api_key_placeholder"
    }

    private suspend fun fetchFromNewsApi(apiKey: String, category: String): List<NewsArticle> {
        val url = when (category) {
            "All" -> "https://newsapi.org/v2/everything?q=world%20OR%20business%20OR%20technology%20OR%20markets%20OR%20crypto%20OR%20science%20OR%20sports%20OR%20health%20OR%20entertainment%20OR%20animals%20OR%20birds%20OR%20nature&language=en&sortBy=publishedAt&pageSize=40&apiKey=$apiKey"
            "Trending" -> "https://newsapi.org/v2/top-headlines?language=en&pageSize=35&apiKey=$apiKey"
            "World" -> "https://newsapi.org/v2/top-headlines?category=general&language=en&pageSize=35&apiKey=$apiKey"
            "Markets" -> "https://newsapi.org/v2/everything?q=stock%20market%20OR%20Nasdaq%20OR%20Dow%20OR%20S%26P%20OR%20investing%20OR%20trading&language=en&pageSize=35&apiKey=$apiKey"
            "Crypto" -> "https://newsapi.org/v2/everything?q=bitcoin%20OR%20ethereum%20OR%20crypto%20OR%20blockchain%20OR%20binance&language=en&pageSize=35&apiKey=$apiKey"
            "AI" -> "https://newsapi.org/v2/everything?q=artificial%20intelligence%20OR%20ChatGPT%20OR%20OpenAI%20OR%20Gemini%20OR%20Claude%20OR%20LLM&language=en&pageSize=35&apiKey=$apiKey"
            "Business" -> "https://newsapi.org/v2/top-headlines?category=business&language=en&pageSize=35&apiKey=$apiKey"
            "Animals" -> "https://newsapi.org/v2/everything?q=animal%20OR%20wildlife%20OR%20zoo%20OR%20pet%20OR%20endangered%20OR%20safari%20OR%20marine%20life&language=en&pageSize=35&apiKey=$apiKey"
            "Birds" -> "https://newsapi.org/v2/everything?q=bird%20OR%20eagle%20OR%20owl%20OR%20falcon%20OR%20parrot%20OR%20penguin%20OR%20flamingo%20OR%20migration%20OR%20ornithology&language=en&pageSize=35&apiKey=$apiKey"
            "Nature" -> "https://newsapi.org/v2/everything?q=nature%20OR%20environment%20OR%20forest%20OR%20rainforest%20OR%20climate%20OR%20conservation&language=en&pageSize=35&apiKey=$apiKey"
            "Science" -> "https://newsapi.org/v2/top-headlines?category=science&language=en&pageSize=35&apiKey=$apiKey"
            "Technology" -> "https://newsapi.org/v2/top-headlines?category=technology&language=en&pageSize=35&apiKey=$apiKey"
            "Health" -> "https://newsapi.org/v2/top-headlines?category=health&language=en&pageSize=35&apiKey=$apiKey"
            "Sports" -> "https://newsapi.org/v2/top-headlines?category=sports&language=en&pageSize=35&apiKey=$apiKey"
            "Entertainment" -> "https://newsapi.org/v2/top-headlines?category=entertainment&language=en&pageSize=35&apiKey=$apiKey"
            else -> "https://newsapi.org/v2/top-headlines?language=en&pageSize=35&apiKey=$apiKey"
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PulseX-AndroidApp/1.0")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "NewsApi response failed with code ${response.code}")
                return emptyList()
            }
            val responseBody = response.body?.string() ?: return emptyList()
            val json = JSONObject(responseBody)
            if (json.optString("status") != "ok") return emptyList()

            val jsonArticles = json.optJSONArray("articles") ?: return emptyList()
            val resultList = mutableListOf<NewsArticle>()

            for (i in 0 until jsonArticles.length()) {
                val item = jsonArticles.optJSONObject(i) ?: continue
                val rawTitle = item.optString("title", "")
                val title = cleanText(rawTitle)
                if (title.isBlank() || title.contains("[Removed]")) continue

                val rawDesc = item.optString("description", "")
                val description = cleanText(rawDesc)
                val articleUrl = item.optString("url", "")
                val sourceObj = item.optJSONObject("source")
                val sourceName = sourceObj?.optString("name", "Reuters") ?: "Reuters"
                val rawAuthor = item.optString("author", "")
                val author = cleanText(rawAuthor).ifBlank { sourceName }
                val publishedAtRaw = item.optString("publishedAt", "")
                val imageUrlRaw = item.optString("urlToImage", "")

                val articleCategory = category
                val imageUrl = cleanImageUrl(imageUrlRaw, articleCategory, i)
                val datePair = parseAndFormatDate(publishedAtRaw)
                val publishedAtFormatted = datePair.first
                val publishedAgo = datePair.second

                val summaryPoints = generateSummaryPoints(title, description)
                val fullContent = listOf(
                    if (description.isNotBlank()) description else title,
                    "Key implications remain focused on macroeconomic policy, market dynamics, and global sector sentiment.",
                    "Analysts urge monitoring developments as institutional perspectives continue to align with emerging trends."
                )

                val articleId = "newsapi_" + i.toString() + "_" + articleUrl.hashCode().toString()

                resultList.add(
                    NewsArticle(
                        id = articleId,
                        title = title,
                        category = articleCategory,
                        source = sourceName,
                        publishedAgo = publishedAgo,
                        views = "${Random.nextInt(1, 15)}.${Random.nextInt(1, 9)}k",
                        author = author,
                        imageUrl = imageUrl,
                        authorImageUrl = authorAvatars[i % authorAvatars.size],
                        isBreaking = i == 0,
                        summaryPoints = summaryPoints,
                        fullContent = fullContent,
                        whyItMatters = if (description.isNotBlank()) description else "Provides strategic insights into current worldwide affairs and markets.",
                        quote = "\"Adaptability and risk management remain vital as economic conditions shift globally.\"",
                        isBookmarked = false,
                        relatedSymbols = deriveRelatedSymbols(title, description),
                        description = description,
                        articleUrl = articleUrl,
                        publishedAt = publishedAtFormatted
                    )
                )
            }
            resultList
        } catch (e: Exception) {
            Log.w(TAG, "NewsApi response parsing skipped/failed: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchFromLiveRssFeeds(category: String): List<NewsArticle> {
        val queryMap = mapOf(
            "All" to "world news OR business OR technology OR markets OR crypto OR science OR sports OR health OR entertainment",
            "Trending" to "breaking news OR world news",
            "World" to "world news OR international news",
            "Markets" to "stock market OR Nasdaq OR Dow OR S&P OR investing OR trading",
            "Crypto" to "bitcoin OR ethereum OR crypto OR blockchain OR binance",
            "AI" to "artificial intelligence OR ChatGPT OR OpenAI OR Gemini OR Claude OR LLM",
            "Business" to "business OR economy OR market",
            "Animals" to "animal OR wildlife OR zoo OR pet OR endangered OR safari OR marine life",
            "Birds" to "bird OR eagle OR owl OR falcon OR parrot OR penguin OR flamingo OR migration OR ornithology",
            "Nature" to "nature OR environment OR forest OR rainforest OR climate OR conservation",
            "Science" to "science OR scientific discoveries OR research OR space",
            "Technology" to "technology OR tech OR software OR gadget",
            "Health" to "medical OR health OR medicine OR disease",
            "Sports" to "sports OR football OR basketball OR soccer",
            "Entertainment" to "entertainment OR movie OR celebrity OR music"
        )

        val query = queryMap[category] ?: "world news"
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val googleNewsUrl = "https://news.google.com/rss/search?q=$encodedQuery&hl=en-US&gl=US&ceid=US:en"

        val specificFeeds = when (category) {
            "All" -> listOf(
                Triple("https://feeds.bbci.co.uk/news/world/rss.xml", "BBC World", "World"),
                Triple("https://feeds.finance.yahoo.com/rss/2.0/headline?s=^GSPC,^DJI,^IXIC,AAPL,MSFT,NVDA,TSLA&region=US&lang=en-US", "Yahoo Finance", "Markets"),
                Triple("https://search.cnbc.com/rs/search/combined:rss?q=finance%20stock%20market", "CNBC", "Business"),
                Triple("https://techcrunch.com/category/enterprise/feed/", "TechCrunch", "Technology"),
                Triple("https://www.coindesk.com/arc/outboundfeeds/rss/", "CoinDesk", "Crypto")
            )
            "World" -> listOf(
                Triple("https://feeds.bbci.co.uk/news/world/rss.xml", "BBC World", "World"),
                Triple("https://rss.nytimes.com/services/xml/rss/nyt/World.xml", "NYT World", "World")
            )
            "Markets" -> listOf(
                Triple("https://feeds.finance.yahoo.com/rss/2.0/headline?s=^GSPC,^DJI,^IXIC,AAPL,MSFT,NVDA,TSLA&region=US&lang=en-US", "Yahoo Finance", "Markets")
            )
            "Business" -> listOf(
                Triple("https://search.cnbc.com/rs/search/combined:rss?q=finance%20stock%20market", "CNBC", "Business")
            )
            "Technology" -> listOf(
                Triple("https://techcrunch.com/category/enterprise/feed/", "TechCrunch", "Technology")
            )
            "Science" -> listOf(
                Triple("https://rss.nytimes.com/services/xml/rss/nyt/Science.xml", "NYT Science", "Science")
            )
            "Sports" -> listOf(
                Triple("https://rss.nytimes.com/services/xml/rss/nyt/Sports.xml", "NYT Sports", "Sports")
            )
            "Crypto" -> listOf(
                Triple("https://www.coindesk.com/arc/outboundfeeds/rss/", "CoinDesk", "Crypto")
            )
            else -> emptyList()
        }

        val articles = mutableListOf<NewsArticle>()

        for ((feedUrl, defaultSource, defaultCategory) in specificFeeds) {
            try {
                val request = Request.Builder()
                    .url(feedUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; PulseX)")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val xmlString = response.body?.string()
                    if (xmlString != null && xmlString.isNotBlank()) {
                        val parsed = parseRssXml(xmlString, defaultSource, category)
                        articles.addAll(parsed)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RSS fetch failed for $defaultSource: ${e.message}")
            }
        }

        if (articles.size < 10) {
            try {
                val request = Request.Builder()
                    .url(googleNewsUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; PulseX)")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val xmlString = response.body?.string()
                    if (xmlString != null && xmlString.isNotBlank()) {
                        val parsed = parseRssXml(xmlString, "PulseX News", category)
                        articles.addAll(parsed)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Google News Search RSS fetch failed for $category: ${e.message}")
            }
        }

        return articles
    }

    private fun parseRssXml(xmlData: String, defaultSource: String, defaultCategory: String): List<NewsArticle> {
        val list = mutableListOf<NewsArticle>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlData))

            var eventType = parser.eventType
            var insideItem = false

            var title = ""
            var description = ""
            var link = ""
            var pubDate = ""
            var mediaUrl = ""

            var count = 0

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName != null && tagName.equals("item", ignoreCase = true)) {
                            insideItem = true
                            title = ""
                            description = ""
                            link = ""
                            pubDate = ""
                            mediaUrl = ""
                        } else if (insideItem && tagName != null) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> {
                                    title = cleanText(parser.nextText())
                                }
                                tagName.equals("description", ignoreCase = true) -> {
                                    description = cleanHtml(parser.nextText())
                                }
                                tagName.equals("link", ignoreCase = true) -> {
                                    link = parser.nextText().trim()
                                }
                                tagName.equals("pubDate", ignoreCase = true) -> {
                                    pubDate = parser.nextText().trim()
                                }
                                tagName.equals("content", ignoreCase = true) || tagName.equals("thumbnail", ignoreCase = true) -> {
                                    val urlAttr = parser.getAttributeValue(null, "url")
                                    if (urlAttr != null && urlAttr.isNotBlank()) {
                                        mediaUrl = urlAttr
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName != null && tagName.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (title.isNotBlank() && title.length > 10) {
                                count++
                                val category = defaultCategory
                                val imageUrl = cleanImageUrl(mediaUrl, category, count)
                                val datePair = parseAndFormatDate(pubDate)
                                val pubFormatted = datePair.first
                                val pubAgo = datePair.second
                                val summaryPoints = generateSummaryPoints(title, description)

                                val articleId = "rss_" + defaultSource.lowercase() + "_" + count.toString() + "_" + title.hashCode().toString()

                                list.add(
                                    NewsArticle(
                                        id = articleId,
                                        title = title,
                                        category = category,
                                        source = defaultSource,
                                        publishedAgo = pubAgo,
                                        views = "${Random.nextInt(2, 28)}.${Random.nextInt(1, 9)}k",
                                        author = "$defaultSource Desk",
                                        imageUrl = imageUrl,
                                        authorImageUrl = authorAvatars[count % authorAvatars.size],
                                        isBreaking = count == 1 && defaultCategory == "World",
                                        summaryPoints = summaryPoints,
                                        fullContent = listOf(
                                            if (description.isNotBlank()) description else title,
                                            "Market observers and analysts are closely monitoring indicators and order flow following this development.",
                                            "Further commentary and analytical reports are anticipated during upcoming news sessions."
                                        ),
                                        whyItMatters = if (description.isNotBlank()) description else "Highlights crucial developments impacting global financial markets and international affairs.",
                                        quote = "\"Market focus shifts toward core fundamentals and international policy impact.\"",
                                        isBookmarked = false,
                                        relatedSymbols = deriveRelatedSymbols(title, description),
                                        description = description,
                                        articleUrl = link,
                                        publishedAt = pubFormatted
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing RSS XML: ${e.message}")
        }
        return list
    }

    private fun cleanText(text: String?): String {
        if (text == null) return ""
        return text.replace("\n", " ")
            .replace("\r", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .trim()
    }

    private fun cleanHtml(html: String?): String {
        if (html == null) return ""
        val stripped = html.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ")
        return cleanText(stripped)
    }

    private fun determineCategory(title: String, description: String, fallback: String = "World"): String {
        val combined = "$title $description".lowercase()
        return when {
            combined.contains("crypto") || combined.contains("bitcoin") || combined.contains("eth") || combined.contains("blockchain") -> "Crypto"
            combined.contains("tech") || combined.contains("ai ") || combined.contains("apple") || combined.contains("nvidia") || combined.contains("microsoft") || combined.contains("chip") || combined.contains("software") -> "Tech"
            combined.contains("bird") || combined.contains("eagle") || combined.contains("falcon") || combined.contains("parrot") || combined.contains("owl") || combined.contains("penguin") || combined.contains("flamingo") || combined.contains("migration") || combined.contains("avian") -> "Birds"
            combined.contains("animal") || combined.contains("wildlife") || combined.contains("pet") || combined.contains("conservation") || combined.contains("endangered") || combined.contains("marine") -> "Animals"
            combined.contains("science") || combined.contains("space") || combined.contains("nasa") || combined.contains("climate") -> "Science"
            combined.contains("sport") || combined.contains("football") || combined.contains("nba") || combined.contains("olympic") -> "Sports"
            combined.contains("fed") || combined.contains("inflation") || combined.contains("rate") || combined.contains("gdp") || combined.contains("economy") -> "Economy"
            combined.contains("market") || combined.contains("stock") || combined.contains("wall street") || combined.contains("s&p") || combined.contains("nasdaq") -> "Markets"
            combined.contains("business") || combined.contains("company") || combined.contains("ceo") || combined.contains("revenue") || combined.contains("bank") -> "Business"
            combined.contains("world") || combined.contains("china") || combined.contains("europe") || combined.contains("global") || combined.contains("ukraine") || combined.contains("president") -> "World"
            else -> fallback
        }
    }

    private fun cleanImageUrl(rawUrl: String?, category: String, index: Int): String {
        if (rawUrl != null && rawUrl.isNotBlank() && rawUrl.startsWith("http")) {
            return rawUrl
        }
        val fallbackList = categoryFallbackImages[category] ?: categoryFallbackImages["World"]!!
        return fallbackList[index % fallbackList.size]
    }

    private fun parseAndFormatDate(rawDate: String): Pair<String, String> {
        if (rawDate.isBlank()) {
            return Pair("Just now", "10m ago")
        }
        try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            )
            var parsedDate: Date? = null
            for (fmt in formats) {
                try {
                    parsedDate = fmt.parse(rawDate)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }

            if (parsedDate != null) {
                val diffMs = System.currentTimeMillis() - parsedDate.time
                val diffMin = TimeUnit.MILLISECONDS.toMinutes(diffMs)
                val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

                val agoString = when {
                    diffMin < 5 -> "Just now"
                    diffMin < 60 -> "${diffMin}m ago"
                    diffHours < 24 -> "${diffHours}h ago"
                    diffDays == 1L -> "Yesterday"
                    else -> "${diffDays}d ago"
                }

                val formattedDateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(parsedDate)
                return Pair(formattedDateStr, agoString)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Date parse fail: ${e.message}")
        }
        return Pair("Recently", "1h ago")
    }

    private fun generateSummaryPoints(title: String, description: String): List<String> {
        val list = mutableListOf<String>()
        if (title.isNotBlank()) list.add(title)
        if (description.isNotBlank() && description != title) {
            list.add(description.take(160) + if (description.length > 160) "..." else "")
        }
        list.add("Strategic implications and sector shifts monitored across international markets.")
        return list.take(3)
    }

    private fun deriveRelatedSymbols(title: String, description: String): List<String> {
        val combined = "$title $description".uppercase()
        val symbols = mutableListOf<String>()
        val checkMap = mapOf(
            "APPLE" to "AAPL", "AAPL" to "AAPL",
            "TESLA" to "TSLA", "TSLA" to "TSLA",
            "NVIDIA" to "NVDA", "NVDA" to "NVDA",
            "MICROSOFT" to "MSFT", "MSFT" to "MSFT",
            "BITCOIN" to "BTC", "BTC" to "BTC",
            "ETHEREUM" to "ETH", "ETH" to "ETH",
            "AMAZON" to "AMZN", "AMZN" to "AMZN"
        )
        for ((key, value) in checkMap) {
            if (combined.contains(key) && !symbols.contains(value)) {
                symbols.add(value)
            }
        }
        if (symbols.isEmpty()) {
            symbols.add("S&P 500")
            symbols.add("NASDAQ")
        }
        return symbols
    }
}
