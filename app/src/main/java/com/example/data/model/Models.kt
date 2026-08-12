package com.example.data.model

data class Stock(
    val symbol: String,
    val name: String,
    val exchange: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val isPositive: Boolean,
    val marketCap: String,
    val peRatio: Double,
    val volume: String,
    val avgVolume: String,
    val high52w: String,
    val low52w: String,
    val divYield: String,
    val beta: Double,
    val category: String,
    val logoUrl: String? = null,
    val historyPoints: List<Float> = emptyList()
)

data class Crypto(
    val id: String,
    val name: String,
    val symbol: String,
    val price: Double,
    val change24h: Double,
    val percentChange24h: Double,
    val isPositive: Boolean,
    val marketCap: String,
    val volume24h: String,
    val logoUrl: String? = null,
    val historyPoints: List<Float> = emptyList()
)

data class NewsArticle(
    val id: String,
    val title: String,
    val category: String,
    val source: String,
    val publishedAgo: String,
    val views: String,
    val author: String,
    val imageUrl: String,
    val authorImageUrl: String,
    val isBreaking: Boolean = false,
    val summaryPoints: List<String> = emptyList(),
    val fullContent: List<String> = emptyList(),
    val whyItMatters: String = "",
    val quote: String = "",
    val isBookmarked: Boolean = false,
    val relatedSymbols: List<String> = emptyList(),
    val description: String = "",
    val articleUrl: String = "",
    val publishedAt: String = ""
)

data class MarketIndex(
    val symbol: String,
    val name: String,
    val value: String,
    val change: String,
    val isPositive: Boolean,
    val type: String // "Index", "Crypto"
)

data class NotificationModel(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val timestamp: String,
    val isRead: Boolean,
    val targetType: String? = null,
    val targetId: String? = null
)

data class UserPreferences(
    val currency: String = "USD",
    val riskTolerance: String = "Moderate",
    val notificationsEnabled: Boolean = true,
    val breakingNewsEnabled: Boolean = true,
    val marketAlertsEnabled: Boolean = true,
    val cryptoAlertsEnabled: Boolean = true,
    val generalNewsEnabled: Boolean = true,
    val theme: String = "Dark",
    val autoSyncWatchlist: Boolean = true
)

data class UserProfile(
    val uid: String = "",
    val name: String = "John Doe",
    val email: String = "ahsanali320n@gmail.com",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
    val isLoggedIn: Boolean = false,
    val isPro: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = System.currentTimeMillis()
)

fun generateFallbackChartPoints(price: Double, isPositive: Boolean, count: Int = 15): List<Float> {
    val base = if (price <= 0.0) 100.0 else price
    val factor = if (isPositive) 0.04 else -0.04
    val startPrice = base * (1.0 - factor)
    val waveOffsets = floatArrayOf(
        0.00f, 0.12f, -0.05f, 0.22f, 0.10f, 0.38f, 0.25f, 0.52f, 0.40f, 0.70f, 0.58f, 0.82f, 0.75f, 0.92f, 1.00f
    )
    return List(count) { i ->
        val progress = waveOffsets[i % waveOffsets.size]
        (startPrice + (base - startPrice) * progress).toFloat()
    }
}

data class UserProgressData(
    val uid: String = "",
    val progressPoints: Int = 150,
    val levelName: String = "Pro Investor",
    val articlesReadCount: Int = 5,
    val streakDays: Int = 4,
    val watchlistCount: Int = 3,
    val bookmarkedArticleIds: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
