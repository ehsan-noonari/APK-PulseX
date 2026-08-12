package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.WatchlistEntity
import com.example.data.model.Crypto
import com.example.data.model.MarketIndex
import com.example.data.model.NewsArticle
import com.example.data.model.NotificationModel
import com.example.data.model.Stock
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.MarketRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.NotificationRepository
import com.example.data.service.FirebaseService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.UserPreferences
import com.example.data.model.UserProgressData

class PulseXViewModel(
    private val application: android.app.Application,
    private val authRepository: AuthRepository,
    private val marketRepository: MarketRepository,
    private val newsRepository: NewsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // User Profile & Session
    val userProfile: StateFlow<UserProfile> = authRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    // Firestore User Progress & Preferences Data
    val userProgress: StateFlow<UserProgressData> = authRepository.userProgress
    val userPreferences: StateFlow<UserPreferences> = authRepository.userPreferences

    // Auth state for Login/SignUp UI
    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Watchlist
    val watchlist: StateFlow<List<WatchlistEntity>> = marketRepository.watchlist.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Market Data
    val stocks: StateFlow<List<Stock>> = marketRepository.stocks
    val cryptos: StateFlow<List<Crypto>> = marketRepository.cryptos
    val marketIndices = marketRepository.marketIndices
    val isMarketLoading: StateFlow<Boolean> = marketRepository.isMarketLoading
    val marketError: StateFlow<String?> = marketRepository.marketError
    val liveConnectionState: StateFlow<String> = marketRepository.liveConnectionState

    // Scroll and Refresh triggers
    private val _homeScrollToTopPending = MutableStateFlow(false)
    val homeScrollToTopPending: StateFlow<Boolean> = _homeScrollToTopPending.asStateFlow()

    private val _selectedHomeCategory = MutableStateFlow("📰 All")
    val selectedHomeCategory: StateFlow<String> = _selectedHomeCategory.asStateFlow()

    private val _newsSortFilter = MutableStateFlow("Newest First")
    val newsSortFilter: StateFlow<String> = _newsSortFilter.asStateFlow()

    fun selectHomeCategory(category: String) {
        _selectedHomeCategory.value = category
        viewModelScope.launch {
            newsRepository.fetchNewsForCategory(category, isForceRefresh = false)
        }
    }

    fun setNewsSortFilter(filter: String) {
        _newsSortFilter.value = filter
        viewModelScope.launch {
            newsRepository.fetchRealNews(isAutoRefresh = false)
        }
    }

    fun setHomeScrollToTopPending(pending: Boolean) {
        _homeScrollToTopPending.value = pending
    }

    private val _homeLogoRefreshPending = MutableStateFlow(false)
    val homeLogoRefreshPending: StateFlow<Boolean> = _homeLogoRefreshPending.asStateFlow()

    fun triggerLogoRefresh() {
        _homeLogoRefreshPending.value = true
    }

    fun clearLogoRefresh() {
        _homeLogoRefreshPending.value = false
    }

    fun refreshMarketData() {
        marketRepository.refreshRealMarketData()
    }

    fun fetchStockTimeframeDetail(symbol: String, timeframe: String) {
        viewModelScope.launch {
            marketRepository.fetchStockTimeframeDetail(symbol, timeframe)
        }
    }

    fun startLiveStockUpdates(symbol: String) {
        marketRepository.startLiveStockUpdates(symbol)
    }

    fun stopLiveStockUpdates() {
        marketRepository.stopLiveStockUpdates()
    }

    fun fetchCryptoTimeframeDetail(symbol: String, timeframe: String) {
        viewModelScope.launch {
            marketRepository.fetchCryptoTimeframeDetail(symbol, timeframe)
        }
    }

    // News Data
    val articles: StateFlow<List<NewsArticle>> = newsRepository.articles
    val bookmarkedIds: StateFlow<Set<String>> = newsRepository.bookmarkedIds
    val isNewsLoading: StateFlow<Boolean> = newsRepository.isNewsLoading
    val newsError: StateFlow<String?> = newsRepository.newsError
    val isNewsEmpty: StateFlow<Boolean> = newsRepository.isNewsEmpty

    fun refreshNews() {
        viewModelScope.launch {
            newsRepository.fetchRealNews()
        }
    }

    // Notifications
    val notifications: StateFlow<List<NotificationModel>> = notificationRepository.notifications

    private val notifiedArticleIds = mutableSetOf<String>()

    init {
        android.util.Log.d("PulseXViewModel", "Copilot ViewModel init: BuildConfig.GEMINI_API_KEY length=${com.example.BuildConfig.GEMINI_API_KEY.length}, startsWith=${com.example.BuildConfig.GEMINI_API_KEY.take(5)}")
        viewModelScope.launch {
            articles.collect { newsList ->
                val prefs = userPreferences.value
                if (prefs.notificationsEnabled && prefs.breakingNewsEnabled) {
                    newsList.forEach { article ->
                        if (article.isBreaking && !notifiedArticleIds.contains(article.id)) {
                            notifiedArticleIds.add(article.id)
                            sendLocalNotification(article)
                            // Also add to in-app notifications
                            notificationRepository.addNotification(
                                NotificationModel(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "Breaking News: ${article.source}",
                                    description = article.title,
                                    category = "NEWS",
                                    timestamp = "Just now",
                                    isRead = false,
                                    targetType = "ARTICLE",
                                    targetId = article.id
                                )
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            val fbUser = FirebaseService.auth?.currentUser
            if (fbUser != null) {
                marketRepository.syncWatchlistWithFirestore(fbUser.uid)
            }
        }
    }

    private fun sendLocalNotification(article: NewsArticle) {
        val intent = android.content.Intent(application, com.example.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("articleId", article.id)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            application, 
            article.id.hashCode(), 
            intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "breaking_news"
        val builder = androidx.core.app.NotificationCompat.Builder(application, channelId)
            .setSmallIcon(com.example.R.drawable.pulsex_logo)
            .setContentTitle(article.title)
            .setContentText(article.source)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        val nm = application.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Breaking News", android.app.NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        nm.notify(article.id.hashCode(), builder.build())
    }

    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            authRepository.updatePreferences(preferences)
        }
    }

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search filter tab: "ALL", "NEWS", "STOCKS", "CRYPTO"
    private val _searchFilter = MutableStateFlow("ALL")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    // Glowing interaction rule: 0 characters -> normal, >=1 characters -> glowing active blue!
    val isSearchGlowActive: StateFlow<Boolean> = combine(_searchQuery) { (query) ->
        query.trim().isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Search Results with Debounce
    @OptIn(FlowPreview::class)
    val filteredNews: StateFlow<List<NewsArticle>> = combine(_searchQuery.debounce(200), articles) { query, list ->
        if (query.isBlank()) list else list.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.source.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class)
    val filteredStocks: StateFlow<List<Stock>> = combine(_searchQuery.debounce(200), stocks) { query, list ->
        if (query.isBlank()) list else list.filter {
            it.symbol.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class)
    val filteredCryptos: StateFlow<List<Crypto>> = combine(_searchQuery.debounce(200), cryptos) { query, list ->
        if (query.isBlank()) list else list.filter {
            it.symbol.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Summary Loading State
    private val _aiSummaryState = MutableStateFlow<List<String>>(emptyList())
    val aiSummaryState: StateFlow<List<String>> = _aiSummaryState.asStateFlow()

    private val _isAiSummaryLoading = MutableStateFlow(false)
    val isAiSummaryLoading: StateFlow<Boolean> = _isAiSummaryLoading.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: String) {
        _searchFilter.value = filter
    }

    fun clearAuthError() {
        _authError.value = null
    }

    private fun mapAuthExceptionToMessage(e: Throwable): String {
        return when (e) {
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> 
                "No account found with this email. Please sign up."
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> 
                "Incorrect password or invalid email format. Please try again."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> 
                "This email is already registered. Try signing in instead."
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> 
                "Password is too weak. It must be at least 6 characters long."
            is com.google.firebase.FirebaseNetworkException -> 
                "Network error. Please check your internet connection."
            else -> {
                val msg = e.localizedMessage ?: ""
                when {
                    msg.contains("password", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                        "Incorrect password. Please try again."
                    msg.contains("email", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                        "Please enter a valid email address."
                    msg.contains("no user", ignoreCase = true) || msg.contains("user not found", ignoreCase = true) ->
                        "No account found with this email."
                    msg.contains("already in use", ignoreCase = true) || msg.contains("already registered", ignoreCase = true) ->
                        "This email is already in use by another account."
                    msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true) ->
                        "Network error. Please check your internet connection."
                    else -> e.localizedMessage ?: "An unexpected authentication error occurred."
                }
            }
        }
    }

    fun signUpWithFirebase(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit
    ) {
        if (name.trim().isBlank()) {
            _authError.value = "Please enter your full name"
            return
        }
        if (email.trim().isBlank()) {
            _authError.value = "Please enter your email address"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authError.value = "Please enter a valid email address"
            return
        }
        if (password.length < 6) {
            _authError.value = "Password must be at least 6 characters long"
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepository.signUpWithFirebase(email.trim(), password, name.trim())
            _isAuthLoading.value = false
            result.fold(
                onSuccess = { profile ->
                    marketRepository.syncWatchlistWithFirestore(profile.uid)
                    onSuccess()
                },
                onFailure = { error ->
                    _authError.value = mapAuthExceptionToMessage(error)
                }
            )
        }
    }

    fun loginWithFirebase(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (email.trim().isBlank()) {
            _authError.value = "Please enter your email address"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authError.value = "Please enter a valid email address"
            return
        }
        if (password.isBlank()) {
            _authError.value = "Please enter your password"
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepository.loginWithFirebase(email.trim(), password)
            _isAuthLoading.value = false
            result.fold(
                onSuccess = { profile ->
                    marketRepository.syncWatchlistWithFirestore(profile.uid)
                    onSuccess()
                },
                onFailure = { error ->
                    _authError.value = mapAuthExceptionToMessage(error)
                }
            )
        }
    }

    fun updateUserPreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            authRepository.updateUserPreferences(preferences)
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            authRepository.loginAsGuest()
        }
    }

    fun login(email: String, name: String = "John Doe") {
        viewModelScope.launch {
            authRepository.login(email, name)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun toggleWatchlistStock(stock: Stock) {
        viewModelScope.launch {
            val isListed = watchlist.value.any { it.symbol == stock.symbol }
            if (isListed) {
                marketRepository.removeFromWatchlist(stock.symbol)
            } else {
                marketRepository.toggleWatchlist(
                    symbol = stock.symbol,
                    name = stock.name,
                    type = "STOCK",
                    price = "$${stock.price}",
                    change = "${if (stock.isPositive) "+" else ""}${stock.percentChange}%",
                    isPositive = stock.isPositive
                )
            }
        }
    }

    fun toggleWatchlistIndex(index: MarketIndex) {
        viewModelScope.launch {
            val isListed = watchlist.value.any { it.symbol == index.symbol }
            if (isListed) {
                marketRepository.removeFromWatchlist(index.symbol)
            } else {
                marketRepository.toggleWatchlist(
                    symbol = index.symbol,
                    name = index.name,
                    type = "INDEX",
                    price = index.value,
                    change = "${if (index.isPositive) "+" else ""}${index.change}%",
                    isPositive = index.isPositive
                )
            }
        }
    }

    fun toggleWatchlistCrypto(crypto: Crypto) {
        viewModelScope.launch {
            val isListed = watchlist.value.any { it.symbol == crypto.symbol }
            if (isListed) {
                marketRepository.removeFromWatchlist(crypto.symbol)
            } else {
                marketRepository.toggleWatchlist(
                    symbol = crypto.symbol,
                    name = crypto.name,
                    type = "CRYPTO",
                    price = "$${crypto.price}",
                    change = "${if (crypto.isPositive) "+" else ""}${crypto.percentChange24h}%",
                    isPositive = crypto.isPositive
                )
            }
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            marketRepository.removeFromWatchlist(symbol)
        }
    }

    fun toggleBookmarkArticle(articleId: String) {
        newsRepository.toggleBookmark(articleId)
    }

    fun markNotificationAsRead(id: String) {
        notificationRepository.markAsRead(id)
    }

    fun markAllNotificationsAsRead() {
        notificationRepository.markAllAsRead()
    }

    fun loadAiSummary(article: NewsArticle) {
        viewModelScope.launch {
            _isAiSummaryLoading.value = true
            val summary = newsRepository.fetchAiSummary(article)
            _aiSummaryState.value = summary ?: emptyList()
            _isAiSummaryLoading.value = false
        }
    }

    fun getStock(symbol: String): Stock? = marketRepository.getStock(symbol)
    fun getCrypto(symbolOrId: String): Crypto? = marketRepository.getCrypto(symbolOrId)
    fun getArticle(id: String): NewsArticle? = newsRepository.getArticle(id)

    class Factory(
        private val application: android.app.Application,
        private val authRepository: AuthRepository,
        private val marketRepository: MarketRepository,
        private val newsRepository: NewsRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PulseXViewModel(application, authRepository, marketRepository, newsRepository, notificationRepository) as T
        }
    }
}
