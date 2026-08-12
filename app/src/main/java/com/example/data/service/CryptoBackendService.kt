package com.example.data.service

import android.util.Log
import com.example.data.model.Crypto
import com.example.data.model.generateFallbackChartPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object CryptoBackendService {

    private const val TAG = "CryptoBackendService"
    private const val CACHE_EXPIRATION_MS = 25_000L // 25 seconds cache for real-time crypto

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val cryptoCache = ConcurrentHashMap<String, Pair<Long, Crypto>>()
    private val historyCache = ConcurrentHashMap<String, Pair<Long, List<Float>>>()

    private val cryptoSymbolToCoinGeckoId = mapOf(
        "BTC" to "bitcoin",
        "ETH" to "ethereum",
        "SOL" to "solana",
        "BNB" to "binancecoin",
        "ADA" to "cardano",
        "XRP" to "ripple",
        "DOGE" to "dogecoin",
        "AVAX" to "avalanche-2"
    )

    /**
     * Fetch real-time crypto list (Bitcoin, Ethereum, Solana, BNB, etc.) with current price, 24h change & volume.
     */
    suspend fun fetchLiveCryptos(): Result<List<Crypto>> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // Check if all cached and fresh
        val cachedCryptos = cryptoSymbolToCoinGeckoId.keys.mapNotNull { symbol ->
            val cached = cryptoCache[symbol]
            if (cached != null && (now - cached.first) < CACHE_EXPIRATION_MS) {
                cached.second
            } else {
                null
            }
        }

        if (cachedCryptos.size == cryptoSymbolToCoinGeckoId.size) {
            return@withContext Result.success(cachedCryptos)
        }

        // Try primary API (CoinGecko)
        val coinGeckoResult = fetchFromCoinGecko()
        if (coinGeckoResult != null && coinGeckoResult.isNotEmpty()) {
            coinGeckoResult.forEach { crypto ->
                cryptoCache[crypto.symbol] = Pair(now, crypto)
            }
            return@withContext Result.success(coinGeckoResult)
        }

        // Fallback API (Yahoo Finance Crypto Endpoints)
        val yahooResult = fetchFromYahooFinanceCrypto()
        if (yahooResult.isNotEmpty()) {
            yahooResult.forEach { crypto ->
                cryptoCache[crypto.symbol] = Pair(now, crypto)
            }
            return@withContext Result.success(yahooResult)
        }

        Result.failure(Exception("Unable to load live crypto market data"))
    }

    /**
     * Fetch crypto detail & chart data for specified time range (1H, 24H, 1W, 1M, 1Y, ALL)
     */
    suspend fun fetchCryptoDetail(symbol: String, timeframe: String): Crypto? = withContext(Dispatchers.IO) {
        val upperSymbol = symbol.uppercase(Locale.US)
        val coinId = cryptoSymbolToCoinGeckoId[upperSymbol] ?: upperSymbol.lowercase(Locale.US)

        val days = mapTimeframeToDays(timeframe)

        val history = fetchHistoryFromCoinGecko(coinId, days)
            ?: fetchHistoryFromYahoo(upperSymbol, timeframe)

        val existing = cryptoCache[upperSymbol]?.second

        if (existing != null) {
            val updated = existing.copy(
                historyPoints = if (history.isNotEmpty()) history else existing.historyPoints
            )
            cryptoCache[upperSymbol] = Pair(System.currentTimeMillis(), updated)
            return@withContext updated
        }

        // If not present in cache, fetch single quote
        val freshList = fetchLiveCryptos().getOrNull()
        val freshCrypto = freshList?.firstOrNull { it.symbol.equals(upperSymbol, ignoreCase = true) }

        if (freshCrypto != null) {
            val updated = freshCrypto.copy(
                historyPoints = if (history.isNotEmpty()) history else freshCrypto.historyPoints
            )
            cryptoCache[upperSymbol] = Pair(System.currentTimeMillis(), updated)
            return@withContext updated
        }

        null
    }

    private fun fetchFromCoinGecko(): List<Crypto>? {
        val ids = cryptoSymbolToCoinGeckoId.values.joinToString(",")
        val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=$ids&order=market_cap_desc&sparkline=true"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PulseX/1.0")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null

            val jsonArray = JSONArray(bodyString)
            val result = mutableListOf<Crypto>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id")
                val name = item.optString("name")
                val rawSymbol = item.optString("symbol").uppercase(Locale.US)
                val price = item.optDouble("current_price", 0.0)
                val priceChange24h = item.optDouble("price_change_24h", 0.0)
                val percentChange24h = item.optDouble("price_change_percentage_24h", 0.0)
                val marketCapRaw = item.optDouble("market_cap", 0.0)
                val volume24hRaw = item.optDouble("total_volume", 0.0)
                val image = item.optString("image")

                val sparklineObj = item.optJSONObject("sparkline_in_7d")
                val priceList = mutableListOf<Float>()
                if (sparklineObj != null) {
                    val priceArray = sparklineObj.optJSONArray("price")
                    if (priceArray != null) {
                        for (j in 0 until priceArray.length()) {
                            val p = priceArray.optDouble(j, Double.NaN)
                            if (!p.isNaN() && p > 0) {
                                priceList.add(p.toFloat())
                            }
                        }
                    }
                }

                val sampledHistory = downsamplePoints(priceList, 20)

                result.add(
                    Crypto(
                        id = id,
                        name = name,
                        symbol = rawSymbol,
                        price = roundTwoDecimals(price),
                        change24h = roundTwoDecimals(priceChange24h),
                        percentChange24h = roundTwoDecimals(percentChange24h),
                        isPositive = percentChange24h >= 0,
                        marketCap = formatCompactNumber(marketCapRaw),
                        volume24h = formatCompactNumber(volume24hRaw),
                        logoUrl = image,
                        historyPoints = if (sampledHistory.size >= 2) sampledHistory else generateFallbackChartPoints(price, percentChange24h >= 0)
                    )
                )
            }
            result
        } catch (e: Exception) {
            Log.w(TAG, "CoinGecko fetch failed: ${e.message}")
            null
        }
    }

    private suspend fun fetchFromYahooFinanceCrypto(): List<Crypto> = coroutineScope {
        cryptoSymbolToCoinGeckoId.keys.map { symbol ->
            async {
                val yahooSymbol = "$symbol-USD"
                val url = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?range=1d&interval=5m"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PulseX/1.0")
                    .build()

                try {
                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) return@async null
                    val bodyString = response.body?.string() ?: return@async null

                    val json = JSONObject(bodyString)
                    val chartObj = json.optJSONObject("chart") ?: return@async null
                    val resultArray = chartObj.optJSONArray("result") ?: return@async null
                    if (resultArray.length() == 0) return@async null

                    val item = resultArray.getJSONObject(0)
                    val meta = item.getJSONObject("meta")

                    val currentPrice = meta.optDouble("regularMarketPrice", 0.0)
                    val previousClose = meta.optDouble("chartPreviousClose", meta.optDouble("previousClose", currentPrice))
                    val priceChange = currentPrice - previousClose
                    val percentChange = if (previousClose > 0) (priceChange / previousClose) * 100 else 0.0

                    val longName = meta.optString("shortName", meta.optString("longName", symbol))
                    val volumeRaw = meta.optLong("regularMarketVolume", 5000000000L).toDouble()

                    val name = when (symbol) {
                        "BTC" -> "Bitcoin"
                        "ETH" -> "Ethereum"
                        "SOL" -> "Solana"
                        "BNB" -> "BNB"
                        "ADA" -> "Cardano"
                        "XRP" -> "XRP"
                        "DOGE" -> "Dogecoin"
                        "AVAX" -> "Avalanche"
                        else -> longName
                    }

                    val historyPoints = mutableListOf<Float>()
                    val indicators = item.optJSONObject("indicators")
                    val quoteArray = indicators?.optJSONArray("quote")
                    if (quoteArray != null && quoteArray.length() > 0) {
                        val closeArray = quoteArray.getJSONObject(0).optJSONArray("close")
                        if (closeArray != null) {
                            for (k in 0 until closeArray.length()) {
                                val p = closeArray.optDouble(k, Double.NaN)
                                if (!p.isNaN() && p > 0) historyPoints.add(p.toFloat())
                            }
                        }
                    }
                    val sampled = downsamplePoints(historyPoints, 20)

                    Crypto(
                        id = symbol.lowercase(Locale.US),
                        name = name,
                        symbol = symbol,
                        price = roundTwoDecimals(currentPrice),
                        change24h = roundTwoDecimals(priceChange),
                        percentChange24h = roundTwoDecimals(percentChange),
                        isPositive = percentChange >= 0,
                        marketCap = formatCompactNumber(currentPrice * 19_000_000),
                        volume24h = formatCompactNumber(volumeRaw),
                        logoUrl = null,
                        historyPoints = if (sampled.size >= 2) sampled else generateFallbackChartPoints(currentPrice, percentChange >= 0)
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun fetchHistoryFromCoinGecko(coinId: String, days: String): List<Float>? {
        val url = "https://api.coingecko.com/api/v3/coins/$coinId/market_chart?vs_currency=usd&days=$days"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PulseX/1.0")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null

            val json = JSONObject(bodyString)
            val pricesArray = json.optJSONArray("prices") ?: return null

            val points = mutableListOf<Float>()
            for (i in 0 until pricesArray.length()) {
                val pair = pricesArray.optJSONArray(i) ?: continue
                val p = pair.optDouble(1, Double.NaN)
                if (!p.isNaN() && p > 0) {
                    points.add(p.toFloat())
                }
            }
            downsamplePoints(points, 20)
        } catch (e: Exception) {
            Log.w(TAG, "CoinGecko chart error for $coinId: ${e.message}")
            null
        }
    }

    private fun fetchHistoryFromYahoo(symbol: String, timeframe: String): List<Float> {
        val (range, interval) = mapTimeframeToYahoo(timeframe)
        val yahooSymbol = "$symbol-USD"
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?range=$range&interval=$interval"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PulseX/1.0")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()
            val bodyString = response.body?.string() ?: return emptyList()

            val json = JSONObject(bodyString)
            val chartObj = json.optJSONObject("chart") ?: return emptyList()
            val resultArray = chartObj.optJSONArray("result") ?: return emptyList()
            if (resultArray.length() == 0) return emptyList()

            val item = resultArray.getJSONObject(0)
            val indicators = item.optJSONObject("indicators")
            val quoteArray = indicators?.optJSONArray("quote")
            if (quoteArray != null && quoteArray.length() > 0) {
                val quoteObj = quoteArray.getJSONObject(0)
                val closePrices = quoteObj.optJSONArray("close")
                val points = mutableListOf<Float>()
                if (closePrices != null) {
                    for (i in 0 until closePrices.length()) {
                        val pt = closePrices.optDouble(i, Double.NaN)
                        if (!pt.isNaN() && pt > 0) {
                            points.add(pt.toFloat())
                        }
                    }
                }
                downsamplePoints(points, 20)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapTimeframeToDays(timeframe: String): String {
        return when (timeframe.uppercase(Locale.US)) {
            "1H" -> "1"
            "24H", "1D" -> "1"
            "1W" -> "7"
            "1M" -> "30"
            "3M" -> "90"
            "6M" -> "180"
            "1Y" -> "365"
            "5Y" -> "1825"
            "ALL", "MAX" -> "max"
            else -> "30"
        }
    }

    private fun mapTimeframeToYahoo(timeframe: String): Pair<String, String> {
        return when (timeframe.uppercase(Locale.US)) {
            "1H" -> Pair("1d", "2m")
            "24H", "1D" -> Pair("1d", "5m")
            "1W" -> Pair("5d", "15m")
            "1M" -> Pair("1mo", "1d")
            "3M" -> Pair("3mo", "1d")
            "6M" -> Pair("6mo", "1d")
            "1Y" -> Pair("1y", "1wk")
            "5Y" -> Pair("5y", "1mo")
            "ALL", "MAX" -> Pair("max", "1mo")
            else -> Pair("1mo", "1d")
        }
    }

    private fun downsamplePoints(points: List<Float>, maxCount: Int): List<Float> {
        if (points.size <= maxCount) return points
        val result = mutableListOf<Float>()
        val step = points.size.toDouble() / maxCount
        for (i in 0 until maxCount) {
            val idx = (i * step).toInt().coerceAtMost(points.size - 1)
            result.add(points[idx])
        }
        return result
    }

    private fun formatCompactNumber(value: Double): String {
        return when {
            value >= 1e12 -> String.format(Locale.US, "%.2fT", value / 1e12)
            value >= 1e9 -> String.format(Locale.US, "%.2fB", value / 1e9)
            value >= 1e6 -> String.format(Locale.US, "%.2fM", value / 1e6)
            value >= 1e3 -> String.format(Locale.US, "%.2fK", value / 1e3)
            else -> String.format(Locale.US, "%.2f", value)
        }
    }

    private fun roundTwoDecimals(value: Double): Double {
        return String.format(Locale.US, "%.2f", value).toDoubleOrNull() ?: value
    }
}
