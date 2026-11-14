package com.example.data.repository

import com.example.data.model.TelegramPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NewsRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        const val TELEGRAM_CHANNEL_URL = "https://t.me/unecstudentshub"
        private const val TELEGRAM_NEWS_API = "https://ericismyhero-github-io.vercel.app/api/telegram-news?limit=3"
    }

    suspend fun fetchLatestTelegramPosts(): Result<List<TelegramPost>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(TELEGRAM_NEWS_API).get().build()
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${res.code}"))
                val body = res.body?.string().orEmpty()
                val json = JSONObject(body)
                val arr = json.optJSONArray("posts") ?: return@withContext Result.success(emptyList())
                val out = mutableListOf<TelegramPost>()
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val id = item.optLong("id", 0L)
                    val text = item.optString("text", "")
                    val url = item.optString("url", "")
                    val date = item.optString("date", "")
                    if (id > 0 && text.isNotBlank() && url.startsWith("https://t.me/")) {
                        out.add(TelegramPost(id = id, text = text, url = url, date = date))
                    }
                }
                Result.success(out.take(3))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBytes(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${res.code}"))
                val bytes = res.body?.bytes() ?: ByteArray(0)
                Result.success(bytes)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
