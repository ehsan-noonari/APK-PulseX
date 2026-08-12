package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
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

        val candidates = listOfNotNull(
            buildConfigKey,
            envKeyValue,
            altKey
        )

        for (key in candidates) {
            if (!key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY_PLACEHOLDER") {
                return key
            }
        }
        Log.e(TAG, "API key detection FAILED: No valid GEMINI_API_KEY found.")
        return ""
    }

    suspend fun generateArticleSummary(title: String, content: String): List<String>? = withContext(Dispatchers.IO) {
        val apiKey = getValidApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "API Key is missing for article summary")
            return@withContext null
        }

        val models = listOf("gemini-2.5-flash", "gemini-flash-latest", "gemini-3.5-flash", "gemini-3.1-flash-lite-preview")
        for (model in models) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val url = URL(urlString)

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val promptText = "Provide exactly 3 concise bullet points summarizing the key market insights of this article titled '$title':\n$content"
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", promptText)
                        ))
                    ))
                }

                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "generateArticleSummary model $model HTTP Status Code: $responseCode")

                if (responseCode == 200) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
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
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available"
                    } catch (e: Exception) {
                        "Failed to read error body: ${e.message}"
                    }
                    Log.e(TAG, "generateArticleSummary model $model error: HTTP $responseCode - $errorBody")
                    if (responseCode == 401 || responseCode == 403) {
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "generateArticleSummary exception for model $model:", e)
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
            Log.e(TAG, "GEMINI_API_KEY is missing or invalid")
            emit("API Configuration Error: GEMINI_API_KEY is missing. Please configure your API key in the AI Studio Secrets panel.")
            return@flow
        }

        val models = listOf("gemini-2.5-flash", "gemini-flash-latest", "gemini-3.5-flash", "gemini-3.1-flash-lite-preview")
        var success = false
        var lastErrorMsg = "Unable to connect to the Gemini AI service. Please check your network connection and try again."

        for (model in models) {
            if (success) break
            var connection: HttpURLConnection? = null
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?key=$apiKey&alt=sse"
                val url = URL(urlString)
                Log.d(TAG, "Attempting stream request with model: $model")

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 20000
                    readTimeout = 40000
                }

                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                    if (systemInstruction.isNotBlank()) {
                        put("systemInstruction", JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", systemInstruction)
                        )))
                    }
                }

                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Model $model HTTP Status Code: $responseCode")

                if (responseCode == 200) {
                    success = true
                    BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
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
                                    Log.e(TAG, "JSON parsing error in stream chunk: ${e.message}")
                                }
                            }
                        }
                    }
                } else {
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available"
                    } catch (e: Exception) {
                        "Error reading response body: ${e.message}"
                    }
                    Log.e(TAG, "HTTP $responseCode Error for model $model - Body: $errorBody")

                    lastErrorMsg = when (responseCode) {
                        401 -> "Authentication failed (HTTP 401): Invalid GEMINI_API_KEY. Please verify your key configuration in Secrets."
                        403 -> "Access denied (HTTP 403): Your GEMINI_API_KEY does not have permission to access the Gemini API service."
                        429 -> "Rate limit exceeded (HTTP 429): Too many requests to Gemini API. Please wait a moment before trying again."
                        500, 502, 503, 504 -> "Gemini server error (HTTP $responseCode): The AI service is temporarily unavailable. Please try again later."
                        400 -> "Bad Request (HTTP 400): Request parameters or prompt formatting error."
                        404 -> "Model $model not found (HTTP 404)."
                        else -> "API Error (HTTP $responseCode): $errorBody"
                    }

                    if (responseCode == 401 || responseCode == 403) {
                        break
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Timeout connecting to model $model:", e)
                lastErrorMsg = "Request timed out (SocketTimeoutException). The Gemini service took too long to respond. Please try again."
            } catch (e: UnknownHostException) {
                Log.e(TAG, "DNS / Host resolution error for model $model:", e)
                lastErrorMsg = "Network connection failed (UnknownHostException). Unable to resolve host. Please check your internet connection."
                break
            } catch (e: SSLException) {
                Log.e(TAG, "SSL Exception for model $model:", e)
                lastErrorMsg = "SSL security error while establishing connection to Gemini service."
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception for model $model:", e)
                val msg = e.localizedMessage ?: e.message ?: "Unknown error"
                lastErrorMsg = when {
                    msg.contains("timeout", ignoreCase = true) -> "Network timeout while communicating with Gemini API."
                    msg.contains("unable to resolve host", ignoreCase = true) -> "Network connection error. Please verify your internet connection."
                    else -> "Network error (${e.javaClass.simpleName}): $msg"
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
