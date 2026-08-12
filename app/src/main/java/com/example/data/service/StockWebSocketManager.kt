package com.example.data.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class StockLivePriceUpdate(
    val symbol: String,
    val price: Double,
    val timestamp: Long,
    val volume: Long? = null
)

object StockWebSocketManager {
    private const val TAG = "StockWebSocketManager"
    private const val WS_URL = "wss://ws.twelvedata.com/v1/quotes/price"

    private val client by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(15, TimeUnit.SECONDS)
            .build()
    }

    private val _priceUpdates = MutableSharedFlow<StockLivePriceUpdate>(extraBufferCapacity = 64)
    val priceUpdates: SharedFlow<StockLivePriceUpdate> = _priceUpdates.asSharedFlow()

    private val _connectionState = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val connectionState: SharedFlow<String> = _connectionState.asSharedFlow()

    private var activeWebSocket: WebSocket? = null
    private var subscribedSymbol: String? = null

    fun connectAndSubscribe(symbol: String, apiKey: String) {
        if (subscribedSymbol.equals(symbol, ignoreCase = true) && activeWebSocket != null) return

        disconnect()
        subscribedSymbol = symbol

        if (apiKey.isBlank()) {
            _connectionState.tryEmit("NO_API_KEY")
            return
        }

        val request = Request.Builder()
            .url("$WS_URL?apikey=$apiKey")
            .build()

        _connectionState.tryEmit("CONNECTING")

        activeWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected for $symbol")
                _connectionState.tryEmit("CONNECTED")
                try {
                    val subMsg = JSONObject().apply {
                        put("action", "subscribe")
                        put("params", JSONObject().apply {
                            put("symbols", symbol.uppercase())
                        })
                    }
                    webSocket.send(subMsg.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending subscription message", e)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val event = json.optString("event")
                    if (event == "price") {
                        val sym = json.optString("symbol", symbol)
                        val price = json.optDouble("price", 0.0)
                        val timestamp = json.optLong("timestamp", System.currentTimeMillis() / 1000)
                        val volume = json.optLong("day_volume", 0L)
                        if (price > 0.0) {
                            _priceUpdates.tryEmit(
                                StockLivePriceUpdate(
                                    symbol = sym,
                                    price = price,
                                    timestamp = timestamp,
                                    volume = if (volume > 0) volume else null
                                )
                            )
                        }
                    } else if (event == "subscribe-status") {
                        val status = json.optString("status")
                        if (status == "ok") {
                            _connectionState.tryEmit("CONNECTED")
                        } else {
                            Log.w(TAG, "Subscribe status: $text")
                            _connectionState.tryEmit("SUBSCRIBE_FAILED")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing WS message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "WebSocket failure for $symbol: ${t.message}")
                _connectionState.tryEmit("ERROR")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed for $symbol: $reason")
                _connectionState.tryEmit("DISCONNECTED")
            }
        })
    }

    fun disconnect() {
        subscribedSymbol = null
        try {
            activeWebSocket?.close(1000, "Screen closed")
        } catch (e: Exception) {
            Log.w(TAG, "Error closing WebSocket", e)
        }
        activeWebSocket = null
        _connectionState.tryEmit("DISCONNECTED")
    }
}
