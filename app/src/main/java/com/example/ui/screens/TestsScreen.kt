package com.example.ui.screens

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import com.example.ui.viewmodel.QuizPhase
import com.example.ui.viewmodel.TestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    viewModel: TestsViewModel,
    catalogRepo: CatalogRepository,
    initialSubject: String = "",
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val subjects = remember { catalogRepo.testBankSubjects.keys.sorted() }

    LaunchedEffect(initialSubject) {
        if (initialSubject.isNotBlank() && initialSubject in subjects) {
            viewModel.selectSubject(initialSubject)
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var displayName = "Local PDF"
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) displayName = c.getString(idx) ?: displayName
            }
            viewModel.loadLocalPdf(uri, displayName)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (state.phase) {
            QuizPhase.SETUP -> {
                var expanded by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 820.dp),
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
                                Text(s("testHeroTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(s("testHeroDesc"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = pal.surface,
                            border = BorderStroke(1.dp, pal.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(s("testSelectorLabel"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = state.selectedSubject,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .testTag("tests_subject_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        subjects.forEach { subj ->
                                            DropdownMenuItem(
                                                text = { Text(subj) },
                                                onClick = {
                                                    viewModel.selectSubject(subj)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { viewModel.loadSubjectBank() },
                                        enabled = !state.isLoading && state.selectedSubject.isNotBlank(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("tests_load_pdf_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(s("testLoadPdfBtn"), maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                        enabled = !state.isLoading,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("tests_import_local_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Rounded.UploadFile, contentDescription = s("testImportPdf"), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("PDF")
                                    }
                                }

                                if (state.isLoading) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                        Text(s("testLoadingSub"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                                    }
                                }

                                state.errorKey?.let { key ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = pal.danger.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, pal.danger.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = s(key),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = pal.danger,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (state.bankQuestions.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = pal.success.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, pal.success.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = pal.success, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = s("testAvailable", "n" to state.bankQuestions.size),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = pal.success,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            state.parseStats?.let { st ->
                                                Text(
                                                    text = s(
                                                        "testParseStats",
                                                        "detected" to st.detected,
                                                        "rejected" to st.rejected,
                                                        "unmarked" to st.unmarked,
                                                        "malformed" to st.malformed
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = pal.textMuted
                                                )
                                            }
                                        }
                                    }

                                    Text(s("testCountLabel"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                                    val maxQ = state.bankQuestions.size
                                    val choices = (listOf(10, 20, 30, 50).filter { it < maxQ } + maxQ).distinct()
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        choices.forEach { cnt ->
                                            FilterChip(
                                                selected = state.selectedCount == cnt,
                                                onClick = { viewModel.setQuestionCount(cnt) },
                                                label = { Text(if (cnt == maxQ) "${s("matFilterAll")} ($cnt)" else "$cnt") },
                                                modifier = Modifier.testTag("test_count_$cnt")
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.startQuiz() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("tests_start_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = pal.fcTests, contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text(s("testStartBtn"), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            QuizPhase.ACTIVE -> {
                var showFinishDialog by remember { mutableStateOf(false) }
                val q = state.quizQuestions.getOrNull(state.currentIndex)
                val totalQ = state.quizQuestions.size
                val answeredCount = state.answers.size
                val mins = state.remainingSeconds / 60
                val secs = state.remainingSeconds % 60
                val timeText = String.format("%02d:%02d", mins, secs)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 820.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s("testQuestionWord")} ${state.currentIndex + 1} / $totalQ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Timer, contentDescription = s("quizTimeLeft"), tint = pal.warning, modifier = Modifier.size(18.dp))
                            Text(timeText, style = MaterialTheme.typography.titleSmall, color = pal.warning, fontWeight = FontWeight.Bold)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { if (totalQ == 0) 0f else (state.currentIndex + 1f) / totalQ },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = s("testAnswered", "a" to answeredCount, "n" to totalQ),
                        style = MaterialTheme.typography.labelSmall,
                        color = pal.textMuted
                    )

                    if (q != null) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = pal.surface,
                                    border = BorderStroke(1.dp, pal.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = q.question,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = pal.textPrimary,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            itemsIndexed(q.options) { idx, opt ->
                                val selected = state.answers[state.currentIndex] == idx
                                val letter = ('A' + idx).toString()
                                Surface(
                                    onClick = { viewModel.selectAnswer(idx) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) pal.accent.copy(alpha = 0.18f) else pal.surface,
                                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) pal.accent else pal.border),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quiz_option_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$letter)",
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) pal.accent2 else pal.textMuted,
                                            modifier = Modifier.width(28.dp)
                                        )
                                        Text(opt, style = MaterialTheme.typography.bodyMedium, color = pal.textPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.goToQuestion(state.currentIndex - 1) },
                            enabled = state.currentIndex > 0,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("quiz_prev_btn")
                        ) {
                            Text(s("testPrevBtn"))
                        }
                        if (state.currentIndex < totalQ - 1) {
                            Button(
                                onClick = { viewModel.goToQuestion(state.currentIndex + 1) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("quiz_next_btn")
                            ) {
                                Text(s("testNextBtn"))
                            }
                        } else {
                            Button(
                                onClick = { showFinishDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("quiz_finish_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = pal.success, contentColor = Color.White)
                            ) {
                                Text(s("testFinishBtn"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (state.currentIndex < totalQ - 1) {
                        TextButton(
                            onClick = { showFinishDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(s("testFinishBtn"), color = pal.accent2)
                        }
                    }
                }

                if (showFinishDialog) {
                    AlertDialog(
                        onDismissRequest = { showFinishDialog = false },
                        title = { Text(s("testFinishBtn")) },
                        text = { Text(s("quizFinishConfirm")) },
                        confirmButton = {
                            Button(onClick = {
                                showFinishDialog = false
                                viewModel.finishQuiz()
                            }) {
                                Text(s("testFinishBtn"))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showFinishDialog = false }) { Text(s("btnCancel")) }
                        }
                    )
                }
            }

            QuizPhase.RESULTS -> {
                val total = state.quizQuestions.size
                var correct = 0
                var wrong = 0
                var skipped = 0
                state.quizQuestions.forEachIndexed { idx, q ->
                    val pick = state.answers[idx]
                    when {
                        pick == null -> skipped++
                        pick == q.answerIndex -> correct++
                        else -> wrong++
                    }
                }
                val pct = if (total == 0) 0 else (correct * 100) / total
                val verdictKey = when {
                    pct >= 85 -> "testResExcellent"
                    pct >= 65 -> "testResGood"
                    pct >= 45 -> "testResAverage"
                    else -> "testResPractice"
                }

                val filteredItems = state.quizQuestions.mapIndexed { idx, q -> idx to q }.filter { (idx, q) ->
                    val pick = state.answers[idx]
                    when (state.reviewFilter) {
                        "wrong" -> pick != null && pick != q.answerIndex
                        "skipped" -> pick == null
                        else -> true
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 820.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = pal.surface,
                            border = BorderStroke(1.dp, pal.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(s("testResultTitle"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = if (pct >= 51) pal.success else pal.danger,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(s(verdictKey), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ResultMiniStat("$correct", s("testStatCorrect"), pal.success)
                                    ResultMiniStat("$wrong", s("testStatWrong"), pal.danger)
                                    ResultMiniStat("$skipped", s("testStatSkipped"), pal.warning)
                                }
                                Spacer(Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.backToSetup() },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quiz_restart_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(s("testRestartBtn"), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "all" to s("testFilterAll"),
                                "wrong" to s("testFilterWrong"),
                                "skipped" to s("testFilterSkipped")
                            ).forEach { (k, label) ->
                                FilterChip(
                                    selected = state.reviewFilter == k,
                                    onClick = { viewModel.setReviewFilter(k) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    itemsIndexed(filteredItems, key = { _, pair -> pair.first }) { _, (idx, q) ->
                        val pick = state.answers[idx]
                        val isCorrect = pick == q.answerIndex
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = pal.surface,
                            border = BorderStroke(
                                1.dp,
                                when {
                                    pick == null -> pal.warning.copy(alpha = 0.5f)
                                    isCorrect -> pal.success.copy(alpha = 0.5f)
                                    else -> pal.danger.copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "${idx + 1}. ${q.question}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${s("quizCorrectMark")}: ${q.options.getOrElse(q.answerIndex) { "" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = pal.success,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (pick != null && pick != q.answerIndex) {
                                    Text(
                                        text = "${s("quizYourMark")}: ${q.options.getOrElse(pick) { "" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = pal.danger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMiniStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
