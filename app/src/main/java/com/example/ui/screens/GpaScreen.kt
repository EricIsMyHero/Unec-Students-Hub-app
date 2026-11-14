package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.GpaSnapshotEntity
import com.example.data.local.GpaSubjectEntity
import com.example.domain.GpaCalculator
import com.example.domain.PredictionResult
import com.example.ui.components.BadgePill
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import java.util.Locale

@Composable
fun GpaScreen(
    subjects: List<GpaSubjectEntity>,
    history: List<GpaSnapshotEntity>,
    onAddSubject: () -> Unit,
    onUpdateSubject: (GpaSubjectEntity) -> Unit,
    onDeleteSubject: (Long) -> Unit,
    onSaveSnapshot: (gpa: Double, totalCredits: Int, earnedCredits: Int, count: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    var tab by remember { mutableIntStateOf(0) }
    val summary = remember(subjects) { GpaCalculator.calculate(subjects) }
    val sweepFraction by animateFloatAsState(
        targetValue = (summary.gpa.toFloat() / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "gpaRing"
    )

    var targetText by remember { mutableStateOf("85") }
    var remainingCreditText by remember { mutableStateOf("30") }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        modifier = Modifier.testTag("gpa_tab_calc")
                    ) { Text(s("gpaModeCalc")) }
                    SegmentedButton(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        modifier = Modifier.testTag("gpa_tab_pred")
                    ) { Text(s("gpaModePred")) }
                }
            }

            // Ring Hero Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(156.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 14.dp.toPx()
                                val inset = stroke / 2f
                                val arcSize = Size(size.width - stroke, size.height - stroke)
                                val topLeft = Offset(inset, inset)
                                drawArc(
                                    color = pal.surfaceInner,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                                if (sweepFraction > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(listOf(summary.ringStart, summary.ringEnd, summary.ringStart)),
                                        startAngle = -90f,
                                        sweepAngle = 360f * sweepFraction,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.2f", summary.gpa),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = summary.color,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (summary.gradeKey.isNotEmpty()) {
                                    Text(
                                        text = s(summary.gradeKey),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = summary.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = s(summary.messageKey),
                            style = MaterialTheme.typography.bodySmall,
                            color = pal.textMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GpaMetric("${summary.totalCredits}", s("gpaTotalCredit"))
                            GpaMetric("${summary.earnedCredits}", s("gpaEarnedCredit"))
                            GpaMetric(s("gpaSubjectCount", "n" to subjects.size), s("gpaOverallGpa"))
                        }

                        if (history.isNotEmpty()) {
                            val latest = history.first()
                            val prev = history.getOrNull(1)
                            val delta = if (prev != null) latest.gpa - prev.gpa else 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${s("gpaPrevious")}: ${String.format(Locale.US, "%.2f", latest.gpa)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pal.textMuted
                                )
                                if (prev != null) {
                                    val sign = if (delta >= 0) "+" else ""
                                    Text(
                                        text = "${s("gpaChange")}: $sign${String.format(Locale.US, "%.2f", delta)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (delta >= 0) pal.success else pal.danger,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSaveSnapshot(summary.gpa, summary.totalCredits, summary.earnedCredits, subjects.size)
                                    Toast.makeText(context, strings["gpaSnapshotSaved"], Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("gpa_save_snapshot"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s("gpaSnapshotBtn"), maxLines = 1)
                            }
                            Button(
                                onClick = {
                                    val msg = "UNEC GPA: ${String.format(Locale.US, "%.2f", summary.gpa)} (${summary.earnedCredits}/${summary.totalCredits} cr)"
                                    IntentHelpers.shareText(context, strings["navGpa"], msg)
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("gpa_share_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s("gpaShareBtn"), maxLines = 1)
                            }
                        }
                    }
                }
            }

            if (tab == 0) {
                items(subjects, key = { it.id }) { sub ->
                    GpaSubjectRow(
                        subject = sub,
                        canDelete = subjects.size > 1,
                        onUpdate = onUpdateSubject,
                        onDelete = {
                            if (subjects.size > 1) onDeleteSubject(sub.id)
                            else Toast.makeText(context, strings["gpaMinSubjects"], Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Button(
                        onClick = onAddSubject,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gpa_add_subject"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s("gpaAddSubjectBtn"), fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text(
                        text = s("gpaFormulaNote"),
                        style = MaterialTheme.typography.bodySmall,
                        color = pal.textMuted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(s("gpaPredictorTitle"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(s("gpaPredictorSub"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = targetText,
                                    onValueChange = { targetText = it },
                                    label = { Text(s("gpaTargetLabel")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("gpa_target_input")
                                )
                                OutlinedTextField(
                                    value = remainingCreditText,
                                    onValueChange = { remainingCreditText = it },
                                    label = { Text(s("gpaRemainingCreditLabel")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("gpa_remaining_input")
                                )
                            }

                            val prediction = GpaCalculator.predict(
                                summary = summary,
                                target = targetText.replace(',', '.').toDoubleOrNull(),
                                remainingCredits = remainingCreditText.toIntOrNull()
                            )
                            val predText = when (prediction) {
                                PredictionResult.Invalid -> s("gpaPredictInvalid")
                                PredictionResult.Impossible -> s("gpaPredictImpossible")
                                PredictionResult.AlreadyReached -> s("gpaPredictReached")
                                is PredictionResult.LowPass -> s(
                                    "gpaPredictLow",
                                    "target" to String.format(Locale.US, "%.1f", prediction.target),
                                    "credits" to prediction.credits
                                )
                                is PredictionResult.Required -> s(
                                    "gpaPredictRequired",
                                    "target" to String.format(Locale.US, "%.1f", prediction.target),
                                    "credits" to prediction.credits,
                                    "score" to String.format(Locale.US, "%.2f", prediction.requiredScore)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = pal.surfaceInner,
                                border = BorderStroke(1.dp, pal.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = predText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = pal.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GpaMetric(value: String, label: String) {
    val pal = LocalUnecPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = pal.textPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
    }
}

@Composable
private fun GpaSubjectRow(
    subject: GpaSubjectEntity,
    canDelete: Boolean,
    onUpdate: (GpaSubjectEntity) -> Unit,
    onDelete: () -> Unit
) {
    val pal = LocalUnecPalette.current
    var name by remember(subject.id) { mutableStateOf(subject.name) }
    var scoreText by remember(subject.id) {
        mutableStateOf(if (subject.score == 0.0) "" else subject.score.toInt().toString())
    }
    val passed = GpaCalculator.isPassed(subject.score)
    val scoreColor = GpaCalculator.scoreColor(subject.score)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = pal.surface,
        border = BorderStroke(1.dp, pal.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onUpdate(subject.copy(name = it))
                    },
                    placeholder = { Text(s("gpaNamePlaceholder")) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("gpa_delete_${subject.id}")) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = s("btnClear"), tint = pal.danger)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(s("gpaCreditShort"), style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
                    IconButton(
                        onClick = {
                            val next = (subject.credit - 1).coerceAtLeast(1)
                            onUpdate(subject.copy(credit = next))
                        }
                    ) { Icon(Icons.Rounded.Remove, contentDescription = "-") }
                    Text("${subject.credit}", fontWeight = FontWeight.Bold, color = pal.textPrimary)
                    IconButton(
                        onClick = {
                            val next = (subject.credit + 1).coerceAtMost(60)
                            onUpdate(subject.copy(credit = next))
                        }
                    ) { Icon(Icons.Rounded.Add, contentDescription = "+") }
                }

                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { raw ->
                        val cleaned = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                        scoreText = cleaned
                        val parsed = cleaned.replace(',', '.').toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
                        onUpdate(subject.copy(score = parsed))
                    },
                    label = { Text(s("gpaScoreShort")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                BadgePill(
                    text = if (passed) s("gpaPassed") else s("gpaFailed"),
                    color = scoreColor,
                    bg = scoreColor.copy(alpha = 0.14f)
                )
            }
        }
    }
}
