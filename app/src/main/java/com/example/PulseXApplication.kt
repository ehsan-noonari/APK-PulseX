package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.PulseXDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.MarketRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.NotificationRepository
import com.google.firebase.FirebaseApp
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.imageLoader

class PulseXApplication : Application(), ImageLoaderFactory {

    val database by lazy { PulseXDatabase.getDatabase(this) }
    val authRepository by lazy { AuthRepository(database.dao()) }
    val marketRepository by lazy { MarketRepository(database.dao()) }
    val newsRepository by lazy { NewsRepository(database.dao()) }
    val notificationRepository by lazy { NotificationRepository(database.dao()) }

    override fun onCreate() {
        super.onCreate()
        try {
            coil.Coil.setImageLoader(this)
            Log.d("PulseXApplication", "Coil custom ImageLoader set successfully")
        } catch (e: Exception) {
            Log.e("PulseXApplication", "Failed to set custom Coil ImageLoader: ${e.message}")
        }
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("PulseXApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("PulseXApplication", "Firebase initialization note: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(20 * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .allowHardware(false) // Prevents the deprecated ashmem/pinning warning on Android Q+
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("PulseXApplication", "onTrimMemory level: $level. Clearing application caches and running system garbage collection.")
        try {
            // Trim or clear Coil's memory cache under memory pressure
            if (level >= TRIM_MEMORY_RUNNING_LOW) {
                imageLoader.memoryCache?.clear()
            }
        } catch (e: Exception) {
            Log.e("PulseXApplication", "Error during onTrimMemory cache trimming: ${e.message}")
        }
        System.gc()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("PulseXApplication", "onLowMemory event triggered. Clearing all image caches.")
        try {
            imageLoader.memoryCache?.clear()
        } catch (e: Exception) {
            Log.e("PulseXApplication", "Error during onLowMemory clearing: ${e.message}")
        }
        System.gc()
    }
}
