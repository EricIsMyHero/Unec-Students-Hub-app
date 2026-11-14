package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SubjectNoteEntity
import com.example.data.repository.CatalogRepository
import com.example.ui.components.BadgePill
import com.example.ui.components.MaterialCard
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubjectDetailScreen(
    subjectName: String,
    catalogRepo: CatalogRepository,
    favoriteKeys: Set<String>,
    notesFlow: Flow<List<SubjectNoteEntity>>,
    onToggleFavorite: (String) -> Unit,
    onOpenMaterial: (String) -> Unit,
    onPracticeTest: (String) -> Unit,
    onAddNote: (tagKey: String?, text: String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onPdfOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val subject = catalogRepo.subjectCatalog[subjectName]
    val notes by notesFlow.collectAsState(initial = emptyList())
    val hasTestBank = catalogRepo.testBankSubjects.containsKey(subjectName)

    var noteText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>("noteTagNote") }

    val examNotes = remember(subject?.type, strings.lang) {
        when (subject?.type) {
            "test" -> if (strings.lang == "en") listOf(
                "This subject uses the Electronic Test format.",
                "Question order may vary. Learn answers by meaning rather than position.",
                "Answers in older files may change—always verify against your lecturer's syllabus."
            ) else listOf(
                "Bu fənn Elektron-Test formatındadır.",
                "Sualların sırası fərqli ola bilər. Cavabları sıraya görə deyil, mənaya görə öyrənin.",
                "Materiallardakı cavablar köhnə ola bilər, müəllimin dediyi mövzularla müqayisə edin."
            )
            "yazili" -> if (strings.lang == "en") listOf(
                "This subject uses the Electronic Written format.",
                "Write complete, well-reasoned answers in your own words.",
                "Questions may evolve across semesters—cross-check with your current syllabus."
            ) else listOf(
                "Bu fənn Elektron-Yazılı formatındadır.",
                "Cavablarınızı tam, əsaslandırılmış və fərqli şəkildə yazmağa diqqət göstərin.",
                "Materiallardakı suallar köhnə ola bilər, sillabusunuzla mütləq uyğunlaşdırın."
            )
            else -> emptyList()
        }
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = subject?.name ?: subjectName,
                            style = MaterialTheme.typography.titleLarge,
                            color = pal.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (subject?.type == "test") {
                                BadgePill(s("formatTest"), pal.fcTests, pal.fcTests.copy(alpha = 0.14f))
                            } else if (subject?.type == "yazili") {
                                BadgePill(s("formatWritten"), pal.warning, pal.warning.copy(alpha = 0.14f))
                            }
                            if (!subject?.code.isNullOrBlank()) {
                                BadgePill(subject!!.code, pal.accent2, pal.surfaceInner)
                            }
                            if (subject != null && subject.semesters.isNotEmpty()) {
                                BadgePill(
                                    subject.semesters.joinToString(", ") { "S$it" },
                                    pal.textMuted,
                                    pal.surfaceInner
                                )
                            }
                        }

                        if (hasTestBank) {
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { onPracticeTest(subjectName) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("subject_practice_test"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = pal.fcTests, contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(s("navTests"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (examNotes.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pal.accent.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, pal.accent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = pal.accent2, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s("subjectInfo"), style = MaterialTheme.typography.titleSmall, color = pal.accent2, fontWeight = FontWeight.Bold)
                            }
                            examNotes.forEach { line ->
                                Text("• $line", style = MaterialTheme.typography.bodySmall, color = pal.textPrimary)
                            }
                        }
                    }
                }
            }

            if (subject != null) {
                items(subject.entries, key = { it.favKey }) { item ->
                    MaterialCard(
                        item = item,
                        isFavorite = item.favKey in favoriteKeys,
                        pdfTypes = catalogRepo.pdfTypes,
                        onToggleFavorite = { onToggleFavorite(item.favKey) },
                        onOpenDetail = { onOpenMaterial(item.favKey) },
                        onPdfOpened = onPdfOpened
                    )
                }
            }

            // Subject Discussion
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = s("subjectDiscussion"),
                            style = MaterialTheme.typography.titleMedium,
                            color = pal.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = s("subjectDiscussionSub"),
                            style = MaterialTheme.typography.bodySmall,
                            color = pal.textMuted
                        )

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("noteTagNote", "noteTagTip", "noteTagQuestion", "noteTagResource").forEach { tag ->
                                FilterChip(
                                    selected = selectedTag == tag,
                                    onClick = { selectedTag = if (selectedTag == tag) null else tag },
                                    label = { Text(s(tag)) }
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text(s("chatPlaceholder")) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("subject_note_input"),
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (noteText.isNotBlank()) {
                                        onAddNote(selectedTag, noteText)
                                        noteText = ""
                                    }
                                },
                                modifier = Modifier.testTag("subject_note_send")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = s("btnSend"), tint = pal.accent2)
                            }
                        }

                        if (notes.isEmpty()) {
                            Text(
                                text = s("chatEmpty"),
                                style = MaterialTheme.typography.bodySmall,
                                color = pal.textMuted,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        } else {
                            notes.forEach { n ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = pal.surfaceInner,
                                    border = BorderStroke(1.dp, pal.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = n.authorName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = pal.accent2,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (!n.tagKey.isNullOrBlank()) {
                                                    BadgePill(s(n.tagKey), pal.cyan, pal.cyan.copy(alpha = 0.14f))
                                                }
                                                Text(
                                                    text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(n.createdAt)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = pal.textMuted
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(n.text, style = MaterialTheme.typography.bodySmall, color = pal.textPrimary)
                                        }
                                        IconButton(onClick = { onDeleteNote(n.id) }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = s("btnClear"), tint = pal.textMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
