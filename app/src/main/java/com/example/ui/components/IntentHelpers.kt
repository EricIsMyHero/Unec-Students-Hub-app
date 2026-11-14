package com.example.ui.components

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.example.ui.strings.StringProvider

object IntentHelpers {
    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, url, Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadPdf(
        context: Context,
        url: String,
        fileName: String,
        strings: StringProvider
    ) {
        try {
            val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                .ifBlank { "unec_material.pdf" }
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName.ifBlank { "UNEC Material" })
                .setDescription("UNEC Hub")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            dm?.enqueue(request)
            Toast.makeText(context, strings["downloadStarted"], Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, strings["downloadError"], Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, title: String, text: String) {
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(send, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
        }
    }

    fun parseColorOr(hex: String?, fallback: Color): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }

    fun formatDateShort(iso: String?, lang: String): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val date = java.time.LocalDate.parse(iso.take(10))
            val monthsAz = listOf("yan", "fev", "mar", "apr", "may", "iyn", "iyl", "avq", "sen", "okt", "noy", "dek")
            val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val m = if (lang == "en") monthsEn[date.monthValue - 1] else monthsAz[date.monthValue - 1]
            "${date.dayOfMonth} $m ${date.year}"
        } catch (_: Exception) {
            iso.take(10)
        }
    }

    fun compassCategoryLabel(cat: String, strings: StringProvider): String = when (cat) {
        "admissions" -> strings["catAdmissions"]
        "first-year" -> strings["catFirstYear"]
        "academic" -> strings["catAcademic"]
        "campuses" -> strings["catCampuses"]
        "documents" -> strings["catDocuments"]
        "finance" -> strings["catFinance"]
        "work" -> strings["catWork"]
        "campus" -> strings["catCampus"]
        "exchange" -> strings["catExchange"]
        "rights" -> strings["catRights"]
        "military" -> strings["catMilitary"]
        else -> cat
    }
}
