package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val type: String, // "STOCK" or "CRYPTO"
    val price: String,
    val change: String,
    val isPositive: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val targetType: String? = null, // "ARTICLE", "STOCK", "CRYPTO"
    val targetId: String? = null
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val name: String,
    val avatarUrl: String,
    val isLoggedIn: Boolean,
    val isPro: Boolean = true
)

@Entity(tableName = "cached_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val source: String,
    val publishedAgo: String,
    val imageUrl: String,
    val isBookmarked: Boolean = false,
    val views: String = "14.2k",
    val author: String = "Elena Rostova"
)
