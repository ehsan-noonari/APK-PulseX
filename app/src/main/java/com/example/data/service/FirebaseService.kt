package com.example.data.service

import android.util.Log
import com.example.data.model.UserPreferences
import com.example.data.model.UserProgressData
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseService {

    private const val TAG = "FirebaseService"

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth instance error: ${e.message}")
            null
        }

    val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore instance error: ${e.message}")
            null
        }

    suspend fun initializeOrFetchUserProfile(
        uid: String,
        email: String,
        name: String
    ): UserProfile {
        val firestore = db
        val defaultProfile = UserProfile(
            uid = uid,
            name = name.ifBlank { email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }.ifBlank { "PulseX Member" },
            email = email,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
            isLoggedIn = true,
            isPro = true,
            preferences = UserPreferences(
                currency = "USD",
                riskTolerance = "Moderate",
                notificationsEnabled = true,
                theme = "Dark",
                autoSyncWatchlist = true
            ),
            createdAt = System.currentTimeMillis()
        )

        if (firestore == null || uid.isBlank()) return defaultProfile

        return try {
            val userRef = firestore.collection("users").document(uid)
            val docSnapshot = userRef.get().await()

            if (docSnapshot.exists()) {
                val firestoreName = docSnapshot.getString("name") ?: defaultProfile.name
                val firestoreEmail = docSnapshot.getString("email") ?: defaultProfile.email
                val avatar = docSnapshot.getString("avatarUrl") ?: defaultProfile.avatarUrl
                val isPro = docSnapshot.getBoolean("isPro") ?: true
                val createdAt = docSnapshot.getLong("createdAt") ?: defaultProfile.createdAt

                @Suppress("UNCHECKED_CAST")
                val prefMap = docSnapshot.get("preferences") as? Map<String, Any>
                    val prefs = UserPreferences(
                        currency = prefMap?.get("currency") as? String ?: "USD",
                        riskTolerance = prefMap?.get("riskTolerance") as? String ?: "Moderate",
                        notificationsEnabled = prefMap?.get("notificationsEnabled") as? Boolean ?: true,
                        breakingNewsEnabled = prefMap?.get("breakingNewsEnabled") as? Boolean ?: true,
                        marketAlertsEnabled = prefMap?.get("marketAlertsEnabled") as? Boolean ?: true,
                        cryptoAlertsEnabled = prefMap?.get("cryptoAlertsEnabled") as? Boolean ?: true,
                        generalNewsEnabled = prefMap?.get("generalNewsEnabled") as? Boolean ?: true,
                        theme = prefMap?.get("theme") as? String ?: "Dark",
                        autoSyncWatchlist = prefMap?.get("autoSyncWatchlist") as? Boolean ?: true
                    )

                // Update last login timestamp
                userRef.set(mapOf("lastLoginAt" to System.currentTimeMillis()), SetOptions.merge())

                defaultProfile.copy(
                    name = firestoreName,
                    email = firestoreEmail,
                    avatarUrl = avatar,
                    isPro = isPro,
                    preferences = prefs,
                    createdAt = createdAt
                )
            } else {
                // Document does not exist: Initialize new user profile & preferences in Firestore
                val profileData = hashMapOf(
                    "uid" to uid,
                    "name" to defaultProfile.name,
                    "email" to defaultProfile.email,
                    "avatarUrl" to defaultProfile.avatarUrl,
                    "isPro" to defaultProfile.isPro,
                    "createdAt" to defaultProfile.createdAt,
                    "lastLoginAt" to System.currentTimeMillis(),
                    "preferences" to hashMapOf(
                        "currency" to defaultProfile.preferences.currency,
                        "riskTolerance" to defaultProfile.preferences.riskTolerance,
                        "notificationsEnabled" to defaultProfile.preferences.notificationsEnabled,
                        "breakingNewsEnabled" to defaultProfile.preferences.breakingNewsEnabled,
                        "marketAlertsEnabled" to defaultProfile.preferences.marketAlertsEnabled,
                        "cryptoAlertsEnabled" to defaultProfile.preferences.cryptoAlertsEnabled,
                        "generalNewsEnabled" to defaultProfile.preferences.generalNewsEnabled,
                        "theme" to defaultProfile.preferences.theme,
                        "autoSyncWatchlist" to defaultProfile.preferences.autoSyncWatchlist
                    )
                )
                userRef.set(profileData, SetOptions.merge()).await()

                // Initialize progress
                val initialProgress = UserProgressData(
                    uid = uid,
                    progressPoints = 250,
                    levelName = "Pro Investor",
                    articlesReadCount = 1,
                    streakDays = 1,
                    watchlistCount = 0,
                    lastUpdated = System.currentTimeMillis()
                )
                saveUserProgressToFirestore(uid, initialProgress)

                defaultProfile
            }
        } catch (e: Exception) {
            Log.w(TAG, "initializeOrFetchUserProfile offline/error: ${e.message}")
            defaultProfile
        }
    }

    suspend fun signUpWithFirebase(
        email: String,
        password: String,
        name: String
    ): Result<UserProfile> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Failed to create user"))
            val uid = firebaseUser.uid

            // Update display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.ifBlank { "PulseX Member" })
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                Log.w(TAG, "Could not set display name: ${e.message}")
            }

            // Initialize profile in Firestore
            val userProfile = initializeOrFetchUserProfile(uid, email, name)
            Result.success(userProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Sign Up failed", e)
            Result.failure(e)
        }
    }

    suspend fun loginWithFirebase(
        email: String,
        password: String
    ): Result<UserProfile> {
        val firebaseAuth = auth ?: return Result.failure(Exception("Firebase Auth not initialized"))

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Failed to sign in"))
            val uid = firebaseUser.uid
            val displayName = firebaseUser.displayName ?: email.substringBefore("@")

            // Fetch or initialize profile in Firestore
            val userProfile = initializeOrFetchUserProfile(uid, firebaseUser.email ?: email, displayName)
            Result.success(userProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun saveUserPreferencesToFirestore(uid: String, preferences: UserPreferences) {
        val firestore = db ?: return
        if (uid.isBlank()) return
        try {
            val prefMap = hashMapOf(
                "currency" to preferences.currency,
                "riskTolerance" to preferences.riskTolerance,
                "notificationsEnabled" to preferences.notificationsEnabled,
                "breakingNewsEnabled" to preferences.breakingNewsEnabled,
                "marketAlertsEnabled" to preferences.marketAlertsEnabled,
                "cryptoAlertsEnabled" to preferences.cryptoAlertsEnabled,
                "generalNewsEnabled" to preferences.generalNewsEnabled,
                "theme" to preferences.theme,
                "autoSyncWatchlist" to preferences.autoSyncWatchlist
            )
            firestore.collection("users").document(uid)
                .set(mapOf("preferences" to prefMap), SetOptions.merge()).await()
            Log.d(TAG, "Saved user preferences to Firestore for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user preferences to Firestore", e)
        }
    }

    suspend fun saveUserProgressToFirestore(uid: String, progress: UserProgressData) {
        val firestore = db ?: return
        if (uid.isBlank()) return
        try {
            val map = hashMapOf(
                "uid" to uid,
                "progressPoints" to progress.progressPoints,
                "levelName" to progress.levelName,
                "articlesReadCount" to progress.articlesReadCount,
                "streakDays" to progress.streakDays,
                "watchlistCount" to progress.watchlistCount,
                "bookmarkedArticleIds" to progress.bookmarkedArticleIds,
                "lastUpdated" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .collection("data").document("progress")
                .set(map, SetOptions.merge()).await()
            Log.d(TAG, "Saved progress data to Firestore for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save progress to Firestore", e)
        }
    }

    suspend fun getUserProgressFromFirestore(uid: String): UserProgressData? {
        val firestore = db ?: return null
        if (uid.isBlank()) return null
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("data").document("progress").get().await()
            if (doc.exists()) {
                val points = doc.getLong("progressPoints")?.toInt() ?: 150
                val level = doc.getString("levelName") ?: "Pro Investor"
                val readCount = doc.getLong("articlesReadCount")?.toInt() ?: 5
                val streak = doc.getLong("streakDays")?.toInt() ?: 4
                val watchlistCount = doc.getLong("watchlistCount")?.toInt() ?: 0
                @Suppress("UNCHECKED_CAST")
                val bookmarks = doc.get("bookmarkedArticleIds") as? List<String> ?: emptyList()

                UserProgressData(
                    uid = uid,
                    progressPoints = points,
                    levelName = level,
                    articlesReadCount = readCount,
                    streakDays = streak,
                    watchlistCount = watchlistCount,
                    bookmarkedArticleIds = bookmarks,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get progress from Firestore: ${e.message}")
            null
        }
    }

    suspend fun fetchWatchlistFromFirestore(uid: String): List<String> {
        val firestore = db ?: return emptyList()
        if (uid.isBlank()) return emptyList()
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("data").document("watchlist").get().await()
            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                (doc.get("symbols") as? List<String>) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch watchlist from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncWatchlistToFirestore(uid: String, symbols: List<String>) {
        val firestore = db ?: return
        if (uid.isBlank()) return
        try {
            firestore.collection("users").document(uid)
                .collection("data").document("watchlist")
                .set(mapOf("symbols" to symbols, "updatedAt" to System.currentTimeMillis()), SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Watchlist sync error: ${e.message}")
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
    }
}
