package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.UserPrefsState
import com.example.ui.components.BadgePill
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import com.example.ui.viewmodel.PomodoroMode
import com.example.ui.viewmodel.TimerViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    prefs: UserPrefsState,
    exams: List<ExamCountdownEntity>,
    onAddExam: (subjectName: String, dateIso: String) -> Unit,
    onDeleteExam: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val timerState by viewModel.state.collectAsState()

    var hasNotifPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasNotifPermission = granted
    }

    var examName by remember { mutableStateOf("") }
    var selectedDateIso by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val today = LocalDate.now().toString()
    val todaySessions = if (prefs.pomodoroTodayDate == today) prefs.pomodoroTodaySessions else 0
    val todayMinutes = if (prefs.pomodoroTodayDate == today) prefs.pomodoroTodayMinutes else 0

    val totalSec = (timerState.remainingMillis / 1000L).toInt().coerceAtLeast(0)
    val clockText = String.format("%02d:%02d", totalSec / 60, totalSec % 60)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pal.accent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, pal.accent.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = pal.accent2)
                            Text(
                                text = s("timerNotifBanner"),
                                style = MaterialTheme.typography.bodySmall,
                                color = pal.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                modifier = Modifier.testTag("timer_allow_notif_btn")
                            ) {
                                Text(s("timerNotifBtn"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Pomodoro Card
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(s("timerPomodoroTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                PomodoroMode.FOCUS to s("timerFocus"),
                                PomodoroMode.SHORT_BREAK to s("timerShortBreak"),
                                PomodoroMode.LONG_BREAK to s("timerLongBreak")
                            ).forEach { (m, label) ->
                                FilterChip(
                                    selected = timerState.mode == m,
                                    onClick = { viewModel.setMode(m) },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("pomo_mode_${m.name}")
                                )
                            }
                        }

                        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 12.dp.toPx()
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
                                drawArc(
                                    color = if (timerState.mode == PomodoroMode.FOCUS) pal.fcTimer else pal.success,
                                    startAngle = -90f,
                                    sweepAngle = 360f * timerState.progress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = clockText,
                                style = MaterialTheme.typography.displaySmall,
                                color = pal.textPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.toggleStartPause() },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("pomo_start_pause"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (timerState.isRunning) s("timerPause") else s("timerStart"), fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.height(48.dp).testTag("pomo_reset")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = s("timerReset"))
                            }
                            OutlinedButton(
                                onClick = { viewModel.skipToNext() },
                                modifier = Modifier.height(48.dp).testTag("pomo_skip")
                            ) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = s("timerSkip"))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$todaySessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(s("timerSession"), style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$todayMinutes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(s("timerMinsToday"), style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
                            }
                        }
                    }
                }
            }

            // Exam Countdowns
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s("timerExamCountdown"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            placeholder = { Text(s("examNamePlaceholder")) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("exam_name_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("exam_date_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(IntentHelpers.formatDateShort(selectedDateIso, strings.lang))
                            }
                            Button(
                                onClick = {
                                    if (examName.isNotBlank()) {
                                        onAddExam(examName, selectedDateIso)
                                        examName = ""
                                    }
                                },
                                enabled = examName.isNotBlank(),
                                modifier = Modifier.height(48.dp).testTag("exam_add_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(s("examAddBtn"))
                            }
                        }
                    }
                }
            }

            if (exams.isEmpty()) {
                item {
                    Text(
                        text = s("examEmptyText"),
                        style = MaterialTheme.typography.bodySmall,
                        color = pal.textMuted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(exams, key = { it.id }) { exam ->
                    val diffDays = try {
                        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(exam.examDateIso)).toInt()
                    } catch (_: Exception) {
                        0
                    }
                    val (label, badgeColor) = when {
                        diffDays < 0 -> s("examPast", "n" to -diffDays) to pal.textMuted
                        diffDays == 0 -> s("examToday") to pal.danger
                        diffDays <= 3 -> s("examDaysLeft", "n" to diffDays) to pal.danger
                        diffDays <= 7 -> s("examDaysLeft", "n" to diffDays) to pal.warning
                        else -> s("examDaysLeft", "n" to diffDays) to pal.success
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exam.subjectName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = IntentHelpers.formatDateShort(exam.examDateIso, strings.lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pal.textMuted
                                )
                            }
                            BadgePill(label, badgeColor, badgeColor.copy(alpha = 0.14f))
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { onDeleteExam(exam.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = s("btnClear"), tint = pal.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = try {
            LocalDate.parse(selectedDateIso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
        val dpState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        selectedDateIso = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text(s("btnSave")) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(s("btnCancel")) }
            }
        ) {
            DatePicker(state = dpState)
        }
    }
}
