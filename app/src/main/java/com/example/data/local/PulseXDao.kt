package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseXDao {
    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY timestamp DESC")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY timestamp DESC")
    suspend fun getWatchlistSync(): List<WatchlistEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    fun isWatchlisted(symbol: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // User Session
    @Query("SELECT * FROM user_session WHERE id = 1")
    fun getUserSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE id = 1")
    suspend fun getUserSessionSync(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearUserSession()

    // Articles
    @Query("SELECT * FROM cached_articles")
    fun getArticles(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticles(articles: List<NewsArticleEntity>)

    @Query("UPDATE cached_articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkState(id: String, isBookmarked: Boolean)
}
