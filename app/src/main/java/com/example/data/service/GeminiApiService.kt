package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object GeminiApiService {

    private const val TAG = "GeminiApiService"

    private fun getValidApiKey(): String {
        val envKeyName = "GEMINI_API_KEY"
        val envKeyValue = System.getenv(envKeyName)
        val buildConfigKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { null }
        val altKey = System.getenv("API_KEY")

        Log.d(TAG, "Environment variable name verified: $envKeyName")
        Log.d(TAG, "System.getenv(\"$envKeyName\") detected: ${!envKeyValue.isNullOrBlank()}")
        Log.d(TAG, "BuildConfig.GEMINI_API_KEY detected: ${!buildConfigKey.isNullOrBlank()}")

        val candidates = listOfNotNull(
            buildConfigKey,
            envKeyValue,
            altKey
        )

        for (key in candidates) {
            if (!key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY_PLACEHOLDER") {
                val masked = if (key.length > 8) "${key.take(4)}...${key.takeLast(4)}" else "****"
                Log.d(TAG, "API key successfully detected and loaded (masked: $masked). Length: ${key.length}")
                return key
            }
        }
        Log.e(TAG, "API key detection FAILED: No valid API key found.")
        return ""
    }

    suspend fun generateArticleSummary(title: String, content: String): List<String>? = withContext(Dispatchers.IO) {
        val apiKey = getValidApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "API Key is missing")
            return@withContext null
        }

        val models = listOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview", "gemini-flash-latest")
        for (model in models) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val url = URL(urlString)
                Log.d(TAG, "generateArticleSummary Request URL: $urlString, Model: $model")

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val promptText = "Provide exactly 3 concise bullet points summarizing the key market insights of this article titled '$title':\n$content"
                val requestBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", promptText)
                        ))
                    ))
                }

                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "generateArticleSummary model $model HTTP Status Code: $responseCode")

                if (responseCode == 200) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "generateArticleSummary Response body: $responseString")
                    val json = JSONObject(responseString)
                    val text = json.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val lines = text.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
                        if (lines.isNotEmpty()) {
                            return@withContext lines.take(3)
                        }
                    }
                } else {
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    } catch (e: Exception) {
                        "Failed to read error body: ${e.message}"
                    }
                    Log.e(TAG, "generateArticleSummary model $model error: HTTP $responseCode - $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "generateArticleSummary model $model exception stack trace:", e)
            } finally {
                connection?.disconnect()
            }
        }

        null
    }

    fun chatWithGeminiStream(
        prompt: String,
        systemInstruction: String = ""
    ): Flow<String> = flow {
        val apiKey = getValidApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "API Key is missing")
            emit("Invalid API configuration. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel.")
            return@flow
        }

        val models = listOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview", "gemini-flash-latest")
        var success = false
        var lastErrorMsg = "Unable to contact the AI service."

        for (model in models) {
            if (success) break
            var connection: HttpURLConnection? = null
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?key=$apiKey&alt=sse"
                val url = URL(urlString)
                Log.d(TAG, "Attempting stream request with model: $model, Endpoint URL: $urlString")

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 20000
                    readTimeout = 40000
                }

                val requestBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                    if (systemInstruction.isNotBlank()) {
                        put("systemInstruction", JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", systemInstruction)
                        )))
                    }
                }

                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Model $model HTTP Status Code: $responseCode")

                if (responseCode == 200) {
                    success = true
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val currentLine = line ?: continue
                            var cleanLine = currentLine.trim()
                            if (cleanLine.startsWith("data:")) {
                                cleanLine = cleanLine.removePrefix("data:").trim()
                            }
                            if (cleanLine.isBlank() || cleanLine == "[DONE]") continue

                            if (cleanLine.startsWith("[")) cleanLine = cleanLine.substring(1)
                            if (cleanLine.startsWith(",")) cleanLine = cleanLine.substring(1)
                            if (cleanLine.endsWith("]")) cleanLine = cleanLine.substring(0, cleanLine.length - 1)
                            if (cleanLine.endsWith(",")) cleanLine = cleanLine.substring(0, cleanLine.length - 1)
                            cleanLine = cleanLine.trim()

                            if (cleanLine.startsWith("{") && cleanLine.endsWith("}")) {
                                try {
                                    val json = JSONObject(cleanLine)
                                    val candidates = json.optJSONArray("candidates")
                                    if (candidates != null && candidates.length() > 0) {
                                        val candidate = candidates.optJSONObject(0)
                                        val content = candidate?.optJSONObject("content")
                                        val parts = content?.optJSONArray("parts")
                                        if (parts != null) {
                                            val sb = StringBuilder()
                                            for (p in 0 until parts.length()) {
                                                val partObj = parts.optJSONObject(p) ?: continue
                                                if (!partObj.optBoolean("thought", false)) {
                                                    val text = partObj.optString("text", "")
                                                    if (text.isNotEmpty()) {
                                                        sb.append(text)
                                                    }
                                                }
                                            }
                                            val extractedText = sb.toString()
                                            if (extractedText.isNotEmpty()) {
                                                emit(extractedText)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "JSON parsing error in stream chunk: ${e.message}", e)
                                }
                            }
                        }
                    }
                } else {
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    } catch (e: Exception) {
                        "Failed to read error body: ${e.message}"
                    }
                    Log.e(TAG, "Error for model $model: HTTP $responseCode - Error Body: $errorBody")

                    lastErrorMsg = when (responseCode) {
                        400 -> "Bad request (400) for model $model: $errorBody"
                        401, 403 -> "Invalid API configuration or unauthorized API key (HTTP $responseCode). Please check your API key."
                        404 -> "Model $model not found (404)."
                        429 -> "Rate limit exceeded for model $model (429). Please try again later."
                        500, 502, 503, 504 -> "AI server error ($responseCode): $errorBody"
                        else -> "Server error ($responseCode): $errorBody"
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Timeout exception for model $model stack trace:", e)
                lastErrorMsg = "Network timeout. The AI service took too long to respond."
            } catch (e: UnknownHostException) {
                Log.e(TAG, "DNS / UnknownHost exception for model $model stack trace:", e)
                lastErrorMsg = "Network connection error: Unable to resolve host. Please check your internet connection."
            } catch (e: SSLException) {
                Log.e(TAG, "SSL exception for model $model stack trace:", e)
                lastErrorMsg = "SSL security error connecting to AI service."
            } catch (e: Exception) {
                Log.e(TAG, "Exception for model $model stack trace:", e)
                val msg = e.message ?: "Unknown error"
                lastErrorMsg = when {
                    msg.contains("timeout", ignoreCase = true) -> "Network timeout. Please check your connection."
                    msg.contains("unable to resolve host", ignoreCase = true) -> "Network connection error. Please check your internet."
                    else -> "Network error: $msg (${e.javaClass.simpleName})"
                }
            } finally {
                connection?.disconnect()
            }
        }

        if (!success) {
            emit(lastErrorMsg)
        }
    }.flowOn(Dispatchers.IO)
}
