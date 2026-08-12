package com.example.data.repository

import com.example.data.SampleData
import com.example.data.local.PulseXDao
import com.example.data.local.UserSessionEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.Crypto
import com.example.data.model.MarketIndex
import com.example.data.model.NewsArticle
import com.example.data.model.NotificationModel
import com.example.data.model.Stock
import com.example.data.model.UserProfile
import com.example.data.model.UserPreferences
import com.example.data.model.UserProgressData
import com.example.data.service.CryptoBackendService
import com.example.data.service.FirebaseService
import com.example.data.service.GeminiApiService
import com.example.data.service.NewsBackendService
import com.example.data.service.StockBackendService
import com.example.data.service.StockWebSocketManager
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AuthRepository(private val dao: PulseXDao) {

    private val _userProgress = MutableStateFlow(UserProgressData())
    val userProgress: StateFlow<UserProgressData> = _userProgress.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val firebaseUserFlow: Flow<com.google.firebase.auth.FirebaseUser?> = kotlinx.coroutines.flow.callbackFlow {
        val auth = FirebaseService.auth
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            try {
                trySend(firebaseAuth.currentUser)
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "AuthStateListener callback error: ${e.message}")
            }
        }
        try {
            auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to add AuthStateListener: ${e.message}")
            trySend(null)
        }
        awaitClose {
            try {
                auth.removeAuthStateListener(listener)
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Failed to remove AuthStateListener: ${e.message}")
            }
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            firebaseUserFlow.collect { currentUser ->
                if (currentUser != null) {
                    val currentSession = dao.getUserSessionSync()
                    if (currentSession == null || !currentSession.isLoggedIn) {
                        val uid = currentUser.uid
                        val email = currentUser.email ?: ""
                        val name = currentUser.displayName ?: ""
                        try {
                            // Fetch latest profile from Firestore or use default, then save to local database
                            val profile = FirebaseService.initializeOrFetchUserProfile(uid, email, name)
                            _userPreferences.value = profile.preferences
                            dao.saveUserSession(
                                UserSessionEntity(
                                    id = 1,
                                    email = profile.email,
                                    name = profile.name,
                                    avatarUrl = profile.avatarUrl,
                                    isLoggedIn = true,
                                    isPro = profile.isPro
                                )
                            )
                            fetchAndSetFirestoreProgress(uid)
                        } catch (e: Exception) {
                            android.util.Log.e("AuthRepository", "Auto-login session restoration failed: ${e.message}")
                        }
                    } else {
                        // User is logged in, let's make sure progress and preferences are loaded!
                        fetchAndSetFirestoreProgress(currentUser.uid)
                        try {
                            val profile = FirebaseService.initializeOrFetchUserProfile(currentUser.uid, currentUser.email ?: "", currentUser.displayName ?: "")
                            _userPreferences.value = profile.preferences
                        } catch (e: Exception) {
                            android.util.Log.e("AuthRepository", "Failed to fetch user preferences on startup: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    val userProfile: Flow<UserProfile> = combine(
        dao.getUserSession(),
        firebaseUserFlow
    ) { session, currentUser ->
        val isGuest = currentUser == null && session?.email == "guest@pulsex.com"
        if (session != null && session.isLoggedIn) {
            UserProfile(
                uid = if (isGuest) "guest" else (currentUser?.uid ?: "user_1"),
                name = if (isGuest) "Guest User" else session.name,
                email = if (isGuest) "Guest Session" else session.email,
                avatarUrl = session.avatarUrl,
                isLoggedIn = true,
                isPro = if (isGuest) false else session.isPro,
                preferences = _userPreferences.value
            )
        } else if (currentUser != null) {
            UserProfile(
                uid = currentUser.uid,
                name = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "PulseX Member",
                email = currentUser.email ?: "",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                isLoggedIn = true,
                isPro = true,
                preferences = _userPreferences.value
            )
        } else {
            UserProfile(isLoggedIn = false)
        }
    }

    suspend fun initializeOrFetchUserProfile(uid: String, email: String, name: String): UserProfile {
        val fetchedProfile = FirebaseService.initializeOrFetchUserProfile(uid, email, name)
        _userPreferences.value = fetchedProfile.preferences
        fetchAndSetFirestoreProgress(uid)
        return fetchedProfile
    }

    suspend fun signUpWithFirebase(
        email: String,
        password: String,
        name: String
    ): Result<UserProfile> {
        val result = FirebaseService.signUpWithFirebase(email, password, name)
        result.onSuccess { profile ->
            _userPreferences.value = profile.preferences
            dao.clearWatchlist()
            dao.saveUserSession(
                UserSessionEntity(
                    id = 1,
                    email = profile.email,
                    name = profile.name,
                    avatarUrl = profile.avatarUrl,
                    isLoggedIn = true,
                    isPro = profile.isPro
                )
            )
            fetchAndSetFirestoreProgress(profile.uid)
        }
        return result
    }

    suspend fun loginWithFirebase(
        email: String,
        password: String
    ): Result<UserProfile> {
        val result = FirebaseService.loginWithFirebase(email, password)
        result.onSuccess { profile ->
            _userPreferences.value = profile.preferences
            dao.clearWatchlist()
            dao.saveUserSession(
                UserSessionEntity(
                    id = 1,
                    email = profile.email,
                    name = profile.name,
                    avatarUrl = profile.avatarUrl,
                    isLoggedIn = true,
                    isPro = profile.isPro
                )
            )
            fetchAndSetFirestoreProgress(profile.uid)
        }
        return result
    }

    suspend fun fetchAndSetFirestoreProgress(uid: String) {
        if (uid.isNotBlank()) {
            val firestoreProgress = FirebaseService.getUserProgressFromFirestore(uid)
            if (firestoreProgress != null) {
                _userProgress.value = firestoreProgress
            }
        }
    }

    suspend fun updateUserPreferences(preferences: UserPreferences) {
        _userPreferences.value = preferences
        val uid = FirebaseService.auth?.currentUser?.uid ?: ""
        if (uid.isNotBlank()) {
            FirebaseService.saveUserPreferencesToFirestore(uid, preferences)
        }
    }

    suspend fun updateUserProgress(progress: UserProgressData) {
        _userProgress.value = progress
        val uid = FirebaseService.auth?.currentUser?.uid ?: progress.uid
        if (uid.isNotBlank()) {
            FirebaseService.saveUserProgressToFirestore(uid, progress)
        }
    }

    suspend fun updatePreferences(preferences: UserPreferences) {
        _userPreferences.value = preferences
        val currentFbUser = FirebaseService.auth?.currentUser
        if (currentFbUser != null) {
            FirebaseService.saveUserPreferencesToFirestore(currentFbUser.uid, preferences)
        }
    }

    suspend fun loginAsGuest() {
        dao.clearWatchlist()
        dao.saveUserSession(
            UserSessionEntity(
                id = 1,
                email = "guest@pulsex.com",
                name = "Guest User",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                isLoggedIn = true,
                isPro = false
            )
        )
    }

    suspend fun login(email: String, name: String = "John Doe") {
        val cleanEmail = email.ifBlank { "ahsanali320n@gmail.com" }
        val cleanName = name.ifBlank { "John Doe" }
        dao.clearWatchlist()
        dao.saveUserSession(
            UserSessionEntity(
                id = 1,
                email = cleanEmail,
                name = cleanName,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                isLoggedIn = true,
                isPro = true
            )
        )
    }

    suspend fun logout() {
        FirebaseService.signOut()
        dao.clearUserSession()
        dao.clearWatchlist()
        _userProgress.value = UserProgressData()
    }
}

class MarketRepository(private val dao: PulseXDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _stocks = MutableStateFlow<List<Stock>>(SampleData.stocks)
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _cryptos = MutableStateFlow<List<Crypto>>(SampleData.cryptos)
    val cryptos: StateFlow<List<Crypto>> = _cryptos.asStateFlow()

    private val _marketIndices = MutableStateFlow<List<MarketIndex>>(SampleData.marketIndices)
    val marketIndices: StateFlow<List<MarketIndex>> = _marketIndices.asStateFlow()

    private val _isMarketLoading = MutableStateFlow(false)
    val isMarketLoading: StateFlow<Boolean> = _isMarketLoading.asStateFlow()

    private val _marketError = MutableStateFlow<String?>(null)
    val marketError: StateFlow<String?> = _marketError.asStateFlow()

    init {
        refreshRealMarketData()
        startPeriodicAutoRefresh()
    }

    private fun startPeriodicAutoRefresh() {
        repositoryScope.launch {
            while (true) {
                delay(30_000L) // Auto refresh every 30 seconds
                val cryptoRes = CryptoBackendService.fetchLiveCryptos()
                cryptoRes.onSuccess { list ->
                    if (list.isNotEmpty()) {
                        _cryptos.value = list
                        updateWatchlistPrices()
                    }
                }
            }
        }
    }

    private suspend fun updateWatchlistPrices() {
        val currentList = dao.getWatchlistSync()
        currentList.forEach { entity ->
            if (entity.type == "STOCK") {
                val updatedStock = _stocks.value.find { it.symbol == entity.symbol }
                if (updatedStock != null) {
                    dao.addToWatchlist(
                        entity.copy(
                            price = "$${updatedStock.price}",
                            change = "${if (updatedStock.isPositive) "+" else ""}${updatedStock.percentChange}%",
                            isPositive = updatedStock.isPositive
                        )
                    )
                }
            } else if (entity.type == "CRYPTO") {
                val updatedCrypto = _cryptos.value.find { it.symbol == entity.symbol }
                if (updatedCrypto != null) {
                    dao.addToWatchlist(
                        entity.copy(
                            price = "$${updatedCrypto.price}",
                            change = "${if (updatedCrypto.isPositive) "+" else ""}${updatedCrypto.percentChange24h}%",
                            isPositive = updatedCrypto.isPositive
                        )
                    )
                }
            } else if (entity.type == "INDEX") {
                val updatedIndex = _marketIndices.value.find { it.symbol == entity.symbol }
                if (updatedIndex != null) {
                    dao.addToWatchlist(
                        entity.copy(
                            price = updatedIndex.value,
                            change = "${if (updatedIndex.isPositive) "+" else ""}${updatedIndex.change}%",
                            isPositive = updatedIndex.isPositive
                        )
                    )
                }
            }
        }
    }

    fun refreshRealMarketData() {
        repositoryScope.launch {
            _isMarketLoading.value = true
            _marketError.value = null

            val indicesResult = StockBackendService.fetchMarketIndices()
            val stocksResult = StockBackendService.fetchPopularStocks()
            val cryptoResult = CryptoBackendService.fetchLiveCryptos()

            indicesResult.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _marketIndices.value = list
                }
            }

            stocksResult.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _stocks.value = list
                }
            }.onFailure { err ->
                _marketError.value = err.message
            }

            cryptoResult.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _cryptos.value = list
                }
            }.onFailure { err ->
                if (_marketError.value == null) {
                    _marketError.value = err.message
                }
            }

            updateWatchlistPrices()

            _isMarketLoading.value = false
        }
    }

    suspend fun fetchCryptoTimeframeDetail(symbol: String, timeframe: String): Crypto? {
        val updatedCrypto = CryptoBackendService.fetchCryptoDetail(symbol, timeframe)
        if (updatedCrypto != null) {
            val currentList = _cryptos.value.toMutableList()
            val index = currentList.indexOfFirst { it.symbol.equals(symbol, ignoreCase = true) }
            if (index != -1) {
                currentList[index] = updatedCrypto
            } else {
                currentList.add(updatedCrypto)
            }
            _cryptos.value = currentList
        }
        return updatedCrypto ?: getCrypto(symbol)
    }

    suspend fun fetchStockTimeframeDetail(symbol: String, timeframe: String): Stock? {
        val updatedStock = StockBackendService.fetchStockDetail(symbol, timeframe)
        if (updatedStock != null) {
            val currentList = _stocks.value.toMutableList()
            val index = currentList.indexOfFirst { it.symbol.equals(symbol, ignoreCase = true) }
            if (index != -1) {
                currentList[index] = updatedStock
            } else {
                currentList.add(updatedStock)
            }
            _stocks.value = currentList
        }
        return updatedStock ?: getStock(symbol)
    }

    private val _liveConnectionState = MutableStateFlow("OFFLINE")
    val liveConnectionState: StateFlow<String> = _liveConnectionState.asStateFlow()

    private var activeStreamJob: Job? = null

    fun startLiveStockUpdates(symbol: String) {
        stopLiveStockUpdates()

        val apiKey = StockBackendService.twelveDataApiKey
        StockWebSocketManager.connectAndSubscribe(symbol, apiKey)

        activeStreamJob = repositoryScope.launch {
            launch {
                StockWebSocketManager.connectionState.collect { state ->
                    _liveConnectionState.value = state
                }
            }

            launch {
                StockWebSocketManager.priceUpdates.collect { update ->
                    if (update.symbol.equals(symbol, ignoreCase = true)) {
                        updateStockWithLivePrice(update.symbol, update.price, update.volume)
                    }
                }
            }

            // Periodic real REST refresh every 10s as a seamless fallback
            launch {
                while (true) {
                    delay(10_000L)
                    val realStock = StockBackendService.fetchStockDetail(symbol, "1D")
                    if (realStock != null) {
                        updateStockInList(realStock)
                        if (_liveConnectionState.value != "CONNECTED") {
                            _liveConnectionState.value = "LIVE_API"
                        }
                    }
                }
            }
        }
    }

    fun stopLiveStockUpdates() {
        activeStreamJob?.cancel()
        activeStreamJob = null
        StockWebSocketManager.disconnect()
        _liveConnectionState.value = "OFFLINE"
    }

    private fun updateStockWithLivePrice(symbol: String, newPrice: Double, volume: Long?) {
        val currentList = _stocks.value.toMutableList()
        val index = currentList.indexOfFirst { it.symbol.equals(symbol, ignoreCase = true) }
        if (index != -1) {
            val oldStock = currentList[index]
            val prevClose = if (oldStock.price > 0 && oldStock.change != 0.0) {
                oldStock.price - oldStock.change
            } else oldStock.price
            val newChange = newPrice - prevClose
            val newPctChange = if (prevClose > 0) (newChange / prevClose) * 100 else 0.0

            val baseHistory = if (oldStock.historyPoints.size >= 2) oldStock.historyPoints else com.example.data.model.generateFallbackChartPoints(newPrice, newChange >= 0)
            val updatedHistory = baseHistory.toMutableList().apply {
                add(newPrice.toFloat())
            }

            val updatedStock = oldStock.copy(
                price = String.format(Locale.US, "%.2f", newPrice).toDoubleOrNull() ?: newPrice,
                change = String.format(Locale.US, "%.2f", newChange).toDoubleOrNull() ?: newChange,
                percentChange = String.format(Locale.US, "%.2f", newPctChange).toDoubleOrNull() ?: newPctChange,
                isPositive = newChange >= 0,
                volume = volume?.let { String.format(Locale.US, "%,d", it) } ?: oldStock.volume,
                historyPoints = updatedHistory
            )
            currentList[index] = updatedStock
            _stocks.value = currentList
        }
    }

    private fun updateStockInList(stock: Stock) {
        val currentList = _stocks.value.toMutableList()
        val index = currentList.indexOfFirst { it.symbol.equals(stock.symbol, ignoreCase = true) }
        if (index != -1) {
            currentList[index] = stock
        } else {
            currentList.add(stock)
        }
        _stocks.value = currentList
    }

    val watchlist: Flow<List<WatchlistEntity>> = dao.getWatchlist()

    fun isWatchlisted(symbol: String): Flow<Boolean> = dao.isWatchlisted(symbol)

    suspend fun syncWatchlistWithFirestore(uid: String) {
        if (uid.isBlank()) return
        val remoteSymbols = FirebaseService.fetchWatchlistFromFirestore(uid)
        for (symbol in remoteSymbols) {
            val stock = getStock(symbol)
            if (stock != null) {
                dao.addToWatchlist(
                    WatchlistEntity(
                        symbol = stock.symbol,
                        name = stock.name,
                        type = "STOCK",
                        price = "$${stock.price}",
                        change = "${if (stock.isPositive) "+" else ""}${stock.percentChange}%",
                        isPositive = stock.isPositive
                    )
                )
            } else {
                val crypto = getCrypto(symbol)
                if (crypto != null) {
                    dao.addToWatchlist(
                        WatchlistEntity(
                            symbol = crypto.symbol,
                            name = crypto.name,
                            type = "CRYPTO",
                            price = "$${crypto.price}",
                            change = "${if (crypto.isPositive) "+" else ""}${crypto.percentChange24h}%",
                            isPositive = crypto.isPositive
                        )
                    )
                } else {
                    val index = _marketIndices.value.find { it.symbol == symbol }
                    if (index != null) {
                        dao.addToWatchlist(
                            WatchlistEntity(
                                symbol = index.symbol,
                                name = index.name,
                                type = "INDEX",
                                price = index.value,
                                change = "${if (index.isPositive) "+" else ""}${index.change}%",
                                isPositive = index.isPositive
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun syncFirestoreWatchlist() {
        val uid = FirebaseService.auth?.currentUser?.uid ?: return
        if (uid.isNotBlank()) {
            val currentWatchlist = dao.getWatchlistSync()
            val symbols = currentWatchlist.map { it.symbol }
            FirebaseService.syncWatchlistToFirestore(uid, symbols)
        }
    }

    suspend fun toggleWatchlist(symbol: String, name: String, type: String, price: String, change: String, isPositive: Boolean) {
        dao.addToWatchlist(
            WatchlistEntity(
                symbol = symbol,
                name = name,
                type = type,
                price = price,
                change = change,
                isPositive = isPositive
            )
        )
        syncFirestoreWatchlist()
    }

    suspend fun removeFromWatchlist(symbol: String) {
        dao.removeFromWatchlist(symbol)
        syncFirestoreWatchlist()
    }

    fun getStock(symbol: String): Stock? {
        return _stocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
    }

    fun getCrypto(symbolOrId: String): Crypto? {
        return _cryptos.value.find {
            it.symbol.equals(symbolOrId, ignoreCase = true) || it.id.equals(symbolOrId, ignoreCase = true)
        }
    }
}

class NewsRepository(private val dao: PulseXDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    private val _isNewsLoading = MutableStateFlow(true)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading.asStateFlow()

    private val _newsError = MutableStateFlow<String?>(null)
    val newsError: StateFlow<String?> = _newsError.asStateFlow()

    val isNewsEmpty: StateFlow<Boolean> = combine(_articles, _isNewsLoading) { articles, isLoading ->
        !isLoading && articles.isEmpty()
    }.stateIn(repositoryScope, SharingStarted.Eagerly, false)

    private val _bookmarks = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarks.asStateFlow()

    private val categoryCache = java.util.concurrent.ConcurrentHashMap<String, List<NewsArticle>>()
    private var lastRequestedCategory: String = "All"
    private var currentFetchJob: Job? = null
    private var currentFetchingCategory: String? = null

    init {
        // Initial fetch & start automatic news refresh every 60 seconds
        repositoryScope.launch {
            fetchNewsForCategory("All", isForceRefresh = false)
            while (isActive) {
                delay(60_000L)
                fetchNewsForCategory(lastRequestedCategory, isForceRefresh = true, isAutoRefresh = true)
            }
        }
    }

    suspend fun fetchRealNews(isAutoRefresh: Boolean = false) {
        fetchNewsForCategory(lastRequestedCategory, isForceRefresh = !isAutoRefresh, isAutoRefresh = isAutoRefresh)
    }

    fun fetchNewsForCategory(category: String, isForceRefresh: Boolean = false, isAutoRefresh: Boolean = false) {
        val cleanCategory = category.replace(Regex("[^A-Za-z]"), "").trim()
        lastRequestedCategory = cleanCategory

        // Cache check: return cached results instantly if they exist and we are not forcing refresh!
        val cached = categoryCache[cleanCategory]
        if (cached != null && !isForceRefresh) {
            currentFetchJob?.cancel()
            _articles.value = cached
            _isNewsLoading.value = false
            _newsError.value = null
            return
        }

        // Prevent duplicate concurrent request for the exact same category
        if (currentFetchingCategory == cleanCategory && currentFetchJob?.isActive == true && !isForceRefresh) {
            return
        }

        // Cancel any previous network request when switching categories
        currentFetchJob?.cancel()
        currentFetchingCategory = cleanCategory

        currentFetchJob = repositoryScope.launch {
            try {
                if (!isAutoRefresh) {
                    _isNewsLoading.value = true
                }
                _newsError.value = null

                val result = NewsBackendService.fetchRealNewsArticles(cleanCategory)
                result.fold(
                    onSuccess = { fetchedArticles ->
                        categoryCache[cleanCategory] = fetchedArticles
                        if (lastRequestedCategory == cleanCategory) {
                            _articles.value = fetchedArticles
                        }
                        _isNewsLoading.value = false
                        _newsError.value = null
                    },
                    onFailure = { error ->
                        if (error is java.util.concurrent.CancellationException) throw error
                        _isNewsLoading.value = false
                        if (_articles.value.isEmpty() || lastRequestedCategory == cleanCategory) {
                            _newsError.value = error.localizedMessage ?: "Failed to load news for category: $category"
                        }
                    }
                )
            } finally {
                if (currentFetchingCategory == cleanCategory) {
                    currentFetchingCategory = null
                }
            }
        }
    }

    fun getArticle(id: String): NewsArticle? {
        return _articles.value.find { it.id == id }
    }

    fun toggleBookmark(id: String) {
        val current = _bookmarks.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _bookmarks.value = current
    }

    private val summaryCache = java.util.concurrent.ConcurrentHashMap<String, List<String>>()

    fun logGeminiApiKeyDiagnostics() {
        val buildConfigKey = try { com.example.BuildConfig.GEMINI_API_KEY } catch (e: Exception) { null }
        val envKey = System.getenv("GEMINI_API_KEY")
        val altEnvKey = System.getenv("API_KEY")

        val mask: (String?) -> String = { k ->
            if (k.isNullOrBlank() || k == "MY_GEMINI_API_KEY") "MISSING/PLACEHOLDER"
            else if (k.length <= 8) "****"
            else "${k.take(4)}...${k.takeLast(4)}"
        }

        android.util.Log.d("NewsRepository", "=== GEMINI API KEY DIAGNOSTICS (Repository Layer) ===")
        android.util.Log.d("NewsRepository", "BuildConfig.GEMINI_API_KEY exists: ${!buildConfigKey.isNullOrBlank()}, status: ${mask(buildConfigKey)}")
        android.util.Log.d("NewsRepository", "System.getenv(\"GEMINI_API_KEY\") exists: ${!envKey.isNullOrBlank()}, status: ${mask(envKey)}")
        android.util.Log.d("NewsRepository", "System.getenv(\"API_KEY\") exists: ${!altEnvKey.isNullOrBlank()}, status: ${mask(altEnvKey)}")
    }

    fun logGeminiHttp403Error(errorDetails: String) {
        android.util.Log.e("NewsRepository", "=== GEMINI HTTP 403 AUTHENTICATION ERROR (Repository Layer) ===")
        android.util.Log.e("NewsRepository", "Specific HTTP 403 error message: $errorDetails")
        android.util.Log.e("NewsRepository", "Troubleshooting: HTTP 403 indicates unauthorized API key or Generative Language API not enabled for this key.")
        logGeminiApiKeyDiagnostics()
    }

    suspend fun fetchAiSummary(article: NewsArticle): List<String>? {
        logGeminiApiKeyDiagnostics()
        val existing = summaryCache[article.id]
        if (existing != null) {
            return existing
        }
        
        // Use title and description instead of fullContent to avoid summarizing boilerplate text
        val articleContent = if (article.description.isNotBlank()) {
            article.description
        } else {
            article.fullContent.joinToString("\n")
        }
        
        val summary = GeminiApiService.generateArticleSummary(
            title = article.title,
            content = articleContent
        )
        if (summary != null) {
            summaryCache[article.id] = summary
        } else {
            logGeminiHttp403Error("Received null summary from GeminiApiService (possible HTTP 403 / unauthorized key or network failure)")
        }
        return summary
    }
}

class NotificationRepository(private val dao: PulseXDao) {

    private val _notifications = MutableStateFlow(SampleData.notifications)
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun addNotification(notification: NotificationModel) {
        val current = _notifications.value.toMutableList()
        current.add(0, notification)
        _notifications.value = current
    }
}
