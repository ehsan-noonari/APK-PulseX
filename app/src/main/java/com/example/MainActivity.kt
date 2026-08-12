package com.example

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.messaging.FirebaseMessaging
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PulseXBottomBar
import com.example.ui.components.PulseXColors
import com.example.ui.components.PulseXDrawerSheet
import com.example.ui.components.PulseXTopHeader
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.BirdDetailScreen
import com.example.ui.screens.CryptoDetailScreen
import com.example.ui.screens.CryptoMarketsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SmartSearchDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StockDetailScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PulseXViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import com.example.ui.components.SparkleAiIcon

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PulseXApplication

        val initialArticleId = intent.getStringExtra("articleId")

        setContent {
            MyApplicationTheme {
                val viewModel: PulseXViewModel = viewModel(
                    factory = PulseXViewModel.Factory(
                        application = app,
                        authRepository = app.authRepository,
                        marketRepository = app.marketRepository,
                        newsRepository = app.newsRepository,
                        notificationRepository = app.notificationRepository
                    )
                )

                PulseXAppContent(viewModel, initialArticleId)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PulseXAppContent(viewModel: PulseXViewModel, initialArticleId: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var scrollOffset by remember { mutableStateOf(0f) }
    val maxScroll = 120f
    val collapseFraction = (scrollOffset / maxScroll).coerceIn(0f, 1f)

    val navBackStackEntryTemp by navController.currentBackStackEntryAsState()
    val currentRouteTemp = navBackStackEntryTemp?.destination?.route ?: "splash"
    val isStandaloneScreenTemp = currentRouteTemp == "splash" || currentRouteTemp == "welcome" || currentRouteTemp == "login"

    var isScrollingActive by remember { mutableStateOf(false) }
    var scrollDirectionDown by remember { mutableStateOf(false) }
    var lastScrollTimestamp by remember { mutableStateOf(0L) }

    LaunchedEffect(lastScrollTimestamp) {
        if (lastScrollTimestamp > 0) {
            delay(800)
            isScrollingActive = false
        }
    }

    val nestedScrollConnection = remember(isStandaloneScreenTemp) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isStandaloneScreenTemp) return Offset.Zero
                val delta = available.y
                
                if (kotlin.math.abs(delta) > 0.5f) {
                    scrollDirectionDown = delta < 0
                    isScrollingActive = true
                    lastScrollTimestamp = System.currentTimeMillis()
                }

                val newOffset = (scrollOffset - delta).coerceIn(0f, maxScroll)
                scrollOffset = newOffset
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(currentRouteTemp) {
        scrollOffset = 0f
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val playServicesAvailable = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS

            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (playServicesAvailable && currentUser != null) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        android.util.Log.d("FCM", "FCM Token: $token")
                        try {
                            val uid = currentUser.uid
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                        } catch (fe: Exception) {
                            android.util.Log.w("FCM", "Failed to update Firestore with FCM token: ${fe.message}")
                        }
                    } else {
                        android.util.Log.w("FCM", "FCM Token retrieval failed: ${task.exception?.message}")
                    }
                }
            } else {
                android.util.Log.w("FCM", "Google Play Services not available or no logged-in user for FCM")
            }
        } catch (e: Exception) {
            android.util.Log.e("FCM", "FCM Token fetch error: ${e.message}")
        }
    }

    LaunchedEffect(initialArticleId) {
        if (initialArticleId != null) {
            navController.navigate("article/$initialArticleId")
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    val userProfile by viewModel.userProfile.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifications = notifications.count { !it.isRead }

    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()

    val isStandaloneScreen = currentRoute == "splash" || currentRoute == "welcome" || currentRoute == "login"
    val isDetailScreen = currentRoute.startsWith("article/") || currentRoute.startsWith("stock/") || currentRoute.startsWith("cryptoDetail/") || currentRoute.startsWith("birdDetail/") || currentRoute.startsWith("aiAssistant")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isStandaloneScreen,
        drawerContent = {
            PulseXDrawerSheet(
                currentRoute = currentRoute,
                userProfile = userProfile,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!isStandaloneScreen && !currentRoute.startsWith("aiAssistant")) {
                    val currentArticleId = navBackStackEntry?.arguments?.getString("articleId")
                    val isArticleBookmarked = currentArticleId?.let { bookmarkedIds.contains(it) } ?: false

                    PulseXTopHeader(
                        title = when {
                            currentRoute == "home" -> "PulseX"
                            currentRoute == "markets" -> "Markets"
                            currentRoute == "search" -> "Search"
                            currentRoute == "crypto" -> "Crypto"
                            currentRoute == "news" -> "News"
                            currentRoute == "notifications" -> "Alerts"
                            currentRoute == "profile" -> "Profile"
                            currentRoute == "watchlist" -> "Watchlist"
                            currentRoute.startsWith("article/") -> "Article"
                            currentRoute.startsWith("stock/") -> navBackStackEntry?.arguments?.getString("symbol") ?: "Stock"
                            currentRoute.startsWith("cryptoDetail/") -> navBackStackEntry?.arguments?.getString("symbol") ?: "Crypto"
                            currentRoute.startsWith("birdDetail/") -> navBackStackEntry?.arguments?.getString("birdName") ?: "Bird"
                            else -> "PulseX"
                        },
                        showBack = isDetailScreen,
                        unreadNotifications = unreadNotifications,
                        isBookmarked = isArticleBookmarked,
                        onBackClick = { navController.popBackStack() },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { navController.navigate("search") },
                        onNotificationClick = { navController.navigate("notifications") },
                        onProfileClick = { navController.navigate("profile") },
                        onAiClick = {
                            val smartContextParam = when {
                                currentRoute.startsWith("stock/") -> {
                                    val symbol = navBackStackEntry?.arguments?.getString("symbol") ?: ""
                                    "stock:$symbol"
                                }
                                currentRoute.startsWith("cryptoDetail/") -> {
                                    val symbol = navBackStackEntry?.arguments?.getString("symbol") ?: ""
                                    "crypto:$symbol"
                                }
                                currentRoute.startsWith("article/") -> {
                                    val articleId = navBackStackEntry?.arguments?.getString("articleId") ?: ""
                                    "article:$articleId"
                                }
                                else -> ""
                            }
                            navController.navigate("aiAssistant?context=$smartContextParam")
                        },
                        onBookmarkClick = if (currentArticleId != null) {
                            { viewModel.toggleBookmarkArticle(currentArticleId) }
                        } else null,
                        onShareClick = if (currentRoute.startsWith("article/")) {
                            val article = currentArticleId?.let { viewModel.getArticle(it) }
                            if (article != null) {
                                val isValidUrl = article.articleUrl.isNotBlank() && 
                                                 (article.articleUrl.startsWith("http://") || article.articleUrl.startsWith("https://"))
                                if (isValidUrl) {
                                    {
                                        try {
                                            val shareIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                type = "text/plain"
                                                val shareTextBuilder = StringBuilder()
                                                shareTextBuilder.append("Article:\n").append(article.title)
                                                if (article.description.isNotBlank()) {
                                                    shareTextBuilder.append("\n").append(article.description)
                                                }
                                                shareTextBuilder.append("\n\nSource:\n").append(article.source)
                                                shareTextBuilder.append("\n\nRead More:\n").append(article.articleUrl)
                                                
                                                putExtra(android.content.Intent.EXTRA_TEXT, shareTextBuilder.toString())
                                            }
                                            val chooser = android.content.Intent.createChooser(shareIntent, "Share Article")
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Unable to share article.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    {
                                        android.widget.Toast.makeText(context, "This article cannot be shared.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                { }
                            }
                        } else null,
                        onLogoClick = if (currentRoute == "home") {
                            { viewModel.triggerLogoRefresh() }
                        } else null,
                        collapseFraction = collapseFraction
                    )
                }
            },
            bottomBar = {
                if (!isStandaloneScreen && !isDetailScreen) {
                    PulseXBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { targetRoute ->
                            if (targetRoute == "home") {
                                if (currentRoute == "home") {
                                    viewModel.setHomeScrollToTopPending(true)
                                } else {
                                    viewModel.setHomeScrollToTopPending(true)
                                    navController.navigate(targetRoute) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            } else {
                                navController.navigate(targetRoute) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .background(PulseXColors.Background)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    enterTransition = {
                        fadeIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) +
                        scaleIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow), initialScale = 0.98f) +
                        slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) { it / 10 }
                    },
                    exitTransition = {
                        fadeOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) +
                        scaleOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow), targetScale = 1.02f) +
                        slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) { -it / 10 }
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) +
                        scaleIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow), initialScale = 1.02f) +
                        slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) { -it / 10 }
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) +
                        scaleOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow), targetScale = 0.98f) +
                        slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)) { it / 10 }
                    }
                ) {
                    composable("splash") {
                        SplashScreen(
                            isLoggedIn = userProfile.isLoggedIn,
                            onNavigateNext = { target ->
                                navController.navigate(target) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("welcome") {
                        WelcomeScreen(
                            onGetStartedClick = { navController.navigate("login") }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToSearch = { navController.navigate("search") },
                            onNavigateToMarkets = { navController.navigate("markets") },
                            onNavigateToCrypto = { navController.navigate("crypto") },
                            onNavigateToArticle = { id -> navController.navigate("article/$id") },
                            onNavigateToStock = { symbol -> navController.navigate("stock/$symbol") },
                            onNavigateToBird = { birdName -> navController.navigate("birdDetail/$birdName") }
                        )
                    }

                    composable("markets") {
                        MarketsScreen(
                            viewModel = viewModel,
                            onStockClick = { symbol -> navController.navigate("stock/$symbol") }
                        )
                    }

                    composable("search") {
                        SearchScreen(
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") },
                            onNavigateToStock = { symbol -> navController.navigate("stock/$symbol") },
                            onNavigateToCrypto = { symbol -> navController.navigate("cryptoDetail/$symbol") },
                            onNavigateToSmartSearch = { query -> navController.navigate("smartSearch/$query") },
                            onNavigateToBird = { birdName -> navController.navigate("birdDetail/$birdName") }
                        )
                    }

                    composable("crypto") {
                        CryptoMarketsScreen(
                            viewModel = viewModel,
                            onCryptoClick = { symbol -> navController.navigate("cryptoDetail/$symbol") }
                        )
                    }

                    composable("news") {
                        NewsScreen(
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") }
                        )
                    }

                    composable("notifications") {
                        NotificationsScreen(
                            viewModel = viewModel,
                            onNavigateToTarget = { type, id ->
                                if (type == "ARTICLE" && id != null) {
                                    navController.navigate("article/$id")
                                } else if (type == "STOCK" && id != null) {
                                    navController.navigate("stock/$id")
                                } else if (type == "CRYPTO" && id != null) {
                                    navController.navigate("cryptoDetail/$id")
                                }
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            viewModel = viewModel,
                            onNavigateToWatchlist = { navController.navigate("watchlist") },
                            onNavigateToNotifications = { navController.navigate("notifications") },
                            onLogoutClick = {
                                viewModel.logout()
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("watchlist") {
                        WatchlistScreen(
                            viewModel = viewModel,
                            onStockClick = { symbol -> navController.navigate("stock/$symbol") },
                            onCryptoClick = { symbol -> navController.navigate("cryptoDetail/$symbol") }
                        )
                    }

                    composable(
                        route = "article/{articleId}",
                        arguments = listOf(navArgument("articleId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val articleId = backStackEntry.arguments?.getString("articleId") ?: "art-1"
                        ArticleDetailScreen(
                            articleId = articleId,
                            viewModel = viewModel,
                            onStockClick = { symbol -> navController.navigate("stock/$symbol") }
                        )
                    }

                    composable(
                        route = "stock/{symbol}",
                        arguments = listOf(navArgument("symbol") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val symbol = backStackEntry.arguments?.getString("symbol") ?: "AAPL"
                        StockDetailScreen(
                            symbol = symbol,
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") }
                        )
                    }

                    composable(
                        route = "cryptoDetail/{symbol}",
                        arguments = listOf(navArgument("symbol") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val symbol = backStackEntry.arguments?.getString("symbol") ?: "BTC"
                        CryptoDetailScreen(
                            symbol = symbol,
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") }
                        )
                    }

                    composable(
                        route = "smartSearch/{query}",
                        arguments = listOf(navArgument("query") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val query = backStackEntry.arguments?.getString("query") ?: "Bitcoin"
                        SmartSearchDetailScreen(
                            query = query,
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") },
                            onNavigateToStock = { symbol -> navController.navigate("stock/$symbol") },
                            onNavigateToCrypto = { symbol -> navController.navigate("cryptoDetail/$symbol") }
                        )
                    }

                    composable(
                        route = "birdDetail/{birdName}",
                        arguments = listOf(navArgument("birdName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val birdName = backStackEntry.arguments?.getString("birdName") ?: "Eagle"
                        BirdDetailScreen(
                            birdName = birdName,
                            viewModel = viewModel,
                            onNavigateToArticle = { id -> navController.navigate("article/$id") },
                            onNavigateToBird = { name -> navController.navigate("birdDetail/$name") }
                        )
                    }

                    composable(
                        route = "aiAssistant?context={context}",
                        arguments = listOf(navArgument("context") { 
                            type = NavType.StringType
                            defaultValue = ""
                        })
                    ) { backStackEntry ->
                        val contextParam = backStackEntry.arguments?.getString("context") ?: ""
                        AiAssistantScreen(
                            contextParam = contextParam,
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                val showFloatingAi = !isStandaloneScreen && !currentRoute.startsWith("aiAssistant")
                if (showFloatingAi) {
                    FloatingAiIcon(
                        onClick = {
                            val smartContextParam = when {
                                currentRoute.startsWith("stock/") -> {
                                    val symbol = navBackStackEntry?.arguments?.getString("symbol") ?: ""
                                    "stock:$symbol"
                                }
                                currentRoute.startsWith("cryptoDetail/") -> {
                                    val symbol = navBackStackEntry?.arguments?.getString("symbol") ?: ""
                                    "crypto:$symbol"
                                }
                                currentRoute.startsWith("article/") -> {
                                    val articleId = navBackStackEntry?.arguments?.getString("articleId") ?: ""
                                    "article:$articleId"
                                }
                                else -> ""
                            }
                            navController.navigate("aiAssistant?context=$smartContextParam")
                        },
                        isScrollingActive = isScrollingActive,
                        scrollDirectionDown = scrollDirectionDown,
                        isDetailScreen = isDetailScreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 18.dp,
                                bottom = innerPadding.calculateBottomPadding() + 12.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingAiIcon(
    onClick: () -> Unit,
    isScrollingActive: Boolean,
    scrollDirectionDown: Boolean,
    isDetailScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation states
    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasEntered = true
    }

    // Determine target opacity and translation based on scrolling
    val targetAlpha = when {
        isDetailScreen -> {
            if (isScrollingActive && scrollDirectionDown) 0f else 1f
        }
        else -> {
            if (isScrollingActive && scrollDirectionDown) 0.4f else 1f
        }
    }

    val targetTranslationY = when {
        isDetailScreen -> {
            if (isScrollingActive && scrollDirectionDown) 40f else 0f
        }
        else -> {
            if (isScrollingActive && scrollDirectionDown) 16f else 0f
        }
    }

    val enterAlpha by animateFloatAsState(
        targetValue = if (hasEntered) targetAlpha else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "AiEnterAlpha"
    )

    val enterTranslationY by animateFloatAsState(
        targetValue = if (hasEntered) targetTranslationY else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "AiEnterTranslation"
    )

    // Continuous floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingAi")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiFloat"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "AiPressScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 0.2f,
        animationSpec = tween(durationMillis = 300),
        label = "AiGlowAlpha"
    )

    if (enterAlpha > 0.01f) {
        Box(
            modifier = modifier
                .size(44.dp)
                .graphicsLayer {
                    alpha = enterAlpha
                    translationY = enterTranslationY + floatOffset
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = PulseXColors.Primary.copy(alpha = 0.3f)
                )
                .background(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (_: Exception) {}
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PulseXColors.Primary.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            )

            SparkleAiIcon(
                modifier = Modifier.size(22.dp),
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
