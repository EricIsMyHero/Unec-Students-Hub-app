package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.CatalogRepository
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

@Composable
fun MaterialDetailScreen(
    favKey: String,
    catalogRepo: CatalogRepository,
    favoriteKeys: Set<String>,
    currentRating: Int,
    onToggleFavorite: (String) -> Unit,
    onRate: (String, Int) -> Unit,
    onOpenMaterial: (String) -> Unit,
    onOpenSubject: (String) -> Unit,
    onPdfOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    val item = remember(favKey) {
        catalogRepo.allMaterials.firstOrNull { it.favKey == favKey }
    }
    var showReportDialog by remember { mutableStateOf(false) }

    if (item == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s("matEmpty"), color = pal.textMuted)
        }
        return
    }

    val isFav = item.favKey in favoriteKeys
    val sameSubject = remember(item) {
        val subj = item.subject ?: return@remember emptyList()
        catalogRepo.subjectCatalog[subj]?.entries?.filter { it.favKey != item.favKey }?.take(6).orEmpty()
    }
    val sameSemesterSubjects = remember(item) {
        val sem = item.semester ?: return@remember emptyList()
        catalogRepo.subjectCatalog.values.filter { sub ->
            sub.name != item.subject && sem in sub.semesters
        }.take(6)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = pal.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (!item.subject.isNullOrBlank()) {
                            Text(
                                text = item.subject,
                                style = MaterialTheme.typography.bodyMedium,
                                color = pal.accent2
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onPdfOpened()
                                    IntentHelpers.openUrl(context, item.url)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("detail_open_pdf"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = pal.accent, contentColor = Color.White)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s("appOpen"), fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onPdfOpened()
                                    IntentHelpers.downloadPdf(context, item.url, item.file, strings)
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("detail_download_pdf"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = s("appDownload"))
                            }
                            IconButton(
                                onClick = { onToggleFavorite(item.favKey) },
                                modifier = Modifier.testTag("detail_favorite_btn")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                    contentDescription = s("moreFavorites"),
                                    tint = if (isFav) pal.warning else pal.textMuted
                                )
                            }
                            IconButton(
                                onClick = { IntentHelpers.shareText(context, item.name, "${item.name}\n${item.url}") },
                                modifier = Modifier.testTag("detail_share_btn")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = s("btnShare"), tint = pal.textPrimary)
                            }
                        }
                    }
                }
            }

            // Summary info
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s("mdShortInfo"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        InfoLine(s("mdCourse"), item.course)
                        if (item.semester != null) InfoLine(s("mdSemester"), s("semesterNum", "s" to item.semester))
                        if (item.format.isNotBlank()) {
                            InfoLine(s("mdFormat"), if (item.format == "test") s("formatTest") else s("formatWritten"))
                        }
                        if (item.code.isNotBlank()) InfoLine(s("mdCode"), item.code)
                        InfoLine(s("mdFile"), item.file)
                    }
                }
            }

            // Rating & Report
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s("mdRateMaterial"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { onRate(item.favKey, star) },
                                    modifier = Modifier.testTag("rate_star_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= currentRating) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "$star",
                                        tint = if (star <= currentRating) pal.warning else pal.textMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            if (currentRating > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = s("mdYourRating", "r" to currentRating),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = pal.accent2
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showReportDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_report_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(18.dp), tint = pal.danger)
                            Spacer(Modifier.width(8.dp))
                            Text(s("mdReport"), color = pal.danger)
                        }
                    }
                }
            }

            if (sameSubject.isNotEmpty()) {
                item {
                    Text(
                        text = s("mdSameSubject"),
                        style = MaterialTheme.typography.titleSmall,
                        color = pal.accent2,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(sameSubject, key = { "rel_${it.favKey}" }) { rel ->
                    Surface(
                        onClick = { onOpenMaterial(rel.favKey) },
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(rel.name, modifier = Modifier.weight(1f), color = pal.textPrimary)
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = pal.textMuted)
                        }
                    }
                }
            }

            if (sameSemesterSubjects.isNotEmpty()) {
                item {
                    Text(
                        text = s("mdSameSemester"),
                        style = MaterialTheme.typography.titleSmall,
                        color = pal.accent2,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(sameSemesterSubjects, key = { "sem_${it.name}" }) { sub ->
                    Surface(
                        onClick = { onOpenSubject(sub.name) },
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(sub.name, modifier = Modifier.weight(1f), color = pal.textPrimary)
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = pal.textMuted)
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        var issueType by remember { mutableStateOf("reportTypeOutdated") }
        var msg by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(s("reportTitle")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s("reportTypeLabel"), style = MaterialTheme.typography.labelMedium, color = pal.textMuted)
                    listOf("reportTypeOutdated", "reportTypeWrong", "reportTypeBroken", "reportTypeOther").forEach { key ->
                        FilterChip(
                            selected = issueType == key,
                            onClick = { issueType = key },
                            label = { Text(s(key)) }
                        )
                    }
                    OutlinedTextField(
                        value = msg,
                        onValueChange = { msg = it },
                        label = { Text(s("reportMessageLabel")) },
                        placeholder = { Text(s("reportMessagePlaceholder")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val subjectLine = "[UNEC Hub] ${strings[issueType]}: ${item.file}"
                    val body = "Material: ${item.name}\nFile: ${item.file}\nURL: ${item.url}\n\n$msg"
                    val mailUri = "mailto:unecstudentshub@gmail.com?subject=${android.net.Uri.encode(subjectLine)}&body=${android.net.Uri.encode(body)}"
                    IntentHelpers.openUrl(context, mailUri)
                    Toast.makeText(context, strings["reportSentToast"], Toast.LENGTH_SHORT).show()
                    showReportDialog = false
                }) {
                    Text(s("btnSend"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text(s("btnCancel")) }
            }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val pal = LocalUnecPalette.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = pal.textPrimary, fontWeight = FontWeight.Medium)
    }
}
