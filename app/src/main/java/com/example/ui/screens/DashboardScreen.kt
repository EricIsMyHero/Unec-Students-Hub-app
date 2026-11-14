package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuizResultEntity
import com.example.data.local.UserPrefsState
import com.example.data.repository.StudyRepository
import com.example.ui.components.BadgePill
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import java.time.LocalDate

@Composable
fun DashboardScreen(
    prefs: UserPrefsState,
    recentResults: List<QuizResultEntity>,
    onSaveProfile: (name: String, faculty: String, major: String, year: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    var name by remember(prefs.profileName) { mutableStateOf(prefs.profileName) }
    var faculty by remember(prefs.profileFaculty) { mutableStateOf(prefs.profileFaculty) }
    var major by remember(prefs.profileMajor) { mutableStateOf(prefs.profileMajor) }
    var year by remember(prefs.profileYear) { mutableIntStateOf(prefs.profileYear) }
    var showStreakInfo by remember { mutableStateOf(false) }

    val rankInfo = remember(prefs.xp) { StudyRepository.getRankProgress(prefs.xp) }
    val activeToday = prefs.lastActiveDate == LocalDate.now().toString()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(s("dashboardLocalNotice"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
            }
            // Rank & XP + Streak
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(s("dashLevel"), style = MaterialTheme.typography.labelMedium, color = pal.textMuted)
                                Text(
                                    text = rankInfo.current.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = pal.accent2,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            BadgePill("${prefs.xp} ${s("dashXp")}", pal.fcDash, pal.fcDash.copy(alpha = 0.14f))
                        }

                        LinearProgressIndicator(
                            progress = { rankInfo.progressFraction },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = rankInfo.next?.let { s("dashRankNext", "xp" to rankInfo.xpToNext, "rank" to it.name) }
                                ?: s("dashRankMax"),
                            style = MaterialTheme.typography.labelSmall,
                            color = pal.textMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = if (activeToday) pal.warning else pal.textMuted
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${prefs.streak} ${s("dashDayUnit")} ${s("dashStreak")}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (activeToday) s("dashStreakOk") else s("dashStreakWarn"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (activeToday) pal.success else pal.warning
                                    )
                                }
                            }
                            IconButton(onClick = { showStreakInfo = true }) {
                                Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = s("dashStreakHow"))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DashMiniStat("${prefs.solvedTests}", s("dashSolved"))
                            DashMiniStat("${prefs.pdfsOpened}", "PDF")
                            DashMiniStat("${prefs.pomodoroTotalCompleted}", "Pomodoro")
                        }
                    }
                }
            }

            // Student Profile
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s("dashProfile"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(s("dashNameLabel")) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                        )
                        OutlinedTextField(
                            value = faculty,
                            onValueChange = { faculty = it },
                            label = { Text(s("dashFacultyLabel")) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = major,
                            onValueChange = { major = it },
                            label = { Text(s("dashMajorLabel")) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(s("dashYearLabel"), style = MaterialTheme.typography.labelMedium, color = pal.textMuted)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..4).forEach { y ->
                                FilterChip(
                                    selected = year == y,
                                    onClick = { year = if (year == y) 0 else y },
                                    label = { Text(s("yearShort", "y" to y)) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onSaveProfile(name, faculty, major, year)
                                Toast.makeText(context, strings["btnSave"], Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_save_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s("btnSave"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Badges
            item {
                Text(s("dashBadges"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(StudyRepository.BADGES.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { badge ->
                        val unlocked = badge.id in prefs.unlockedBadges
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (unlocked) pal.accent.copy(alpha = 0.14f) else pal.surface,
                            border = BorderStroke(1.dp, if (unlocked) pal.accent else pal.border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (unlocked) pal.warning else pal.textMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (strings.lang == "en") badge.nameEn else badge.nameAz,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (unlocked) pal.textPrimary else pal.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (strings.lang == "en") badge.descEn else badge.descAz,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = pal.textMuted
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            // Recent Results
            item {
                Text(s("dashRecentResults"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (recentResults.isEmpty()) {
                item {
                    Text(s("dashNoTests"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                }
            } else {
                items(recentResults, key = { it.id }) { res ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.subjectName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${res.correctCount}/${res.total} • ${res.durationSec / 60}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pal.textMuted
                                )
                            }
                            BadgePill(
                                text = "${res.percentage}%",
                                color = if (res.percentage >= 51) pal.success else pal.danger,
                                bg = (if (res.percentage >= 51) pal.success else pal.danger).copy(alpha = 0.14f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStreakInfo) {
        AlertDialog(
            onDismissRequest = { showStreakInfo = false },
            title = { Text(s("dashStreakHow")) },
            text = { Text(s("streakHowBody")) },
            confirmButton = {
                TextButton(onClick = { showStreakInfo = false }) { Text(s("btnClose")) }
            }
        )
    }
}

@Composable
private fun DashMiniStat(value: String, label: String) {
    val pal = LocalUnecPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = pal.textPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
    }
}
