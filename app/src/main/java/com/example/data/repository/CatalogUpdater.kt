package com.example.data.repository

import android.content.Context
import android.util.AtomicFile
import com.example.data.model.MobileCatalogJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

/** Downloads only public data; a bad or unavailable response leaves the last good snapshot intact. */
class CatalogUpdater(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(CATALOG_URL).header("Cache-Control", "no-cache").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val body = response.body ?: return@withContext false
                if (body.contentLength() > CatalogRepository.MAX_CATALOG_BYTES) return@withContext false
                val bytes = body.byteStream().use { stream ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var count: Int
                    while (stream.read(buffer).also { count = it } != -1) {
                        if (output.size().toLong() + count > CatalogRepository.MAX_CATALOG_BYTES) return@withContext false
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                }
                val catalog = json.decodeFromString<MobileCatalogJson>(bytes.toString(Charsets.UTF_8))
                if (!catalog.isUsable()) return@withContext false
                val target = AtomicFile(File(context.filesDir, "mobile-catalog.json"))
                val output = target.startWrite()
                try {
                    output.write(bytes)
                    target.finishWrite(output)
                } catch (error: Exception) {
                    target.failWrite(output)
                    throw error
                }
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val CATALOG_URL = "https://unecstudentshub.com/data/mobile-catalog.json"
    }
}
