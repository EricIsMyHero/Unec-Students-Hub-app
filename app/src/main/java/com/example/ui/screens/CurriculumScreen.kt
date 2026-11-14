package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurriculumSubjectJson
import com.example.data.repository.CatalogRepository
import com.example.ui.components.BadgePill
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import kotlinx.coroutines.flow.Flow

@Composable
fun CurriculumScreen(
    catalogRepo: CatalogRepository,
    selectedMajor: String,
    profileYear: Int,
    observeOverrides: (String) -> Flow<Map<String, Boolean>>,
    onSelectMajor: (String) -> Unit,
    onToggleOverride: (majorKey: String, subjectKey: String, completed: Boolean) -> Unit,
    onOpenSubject: (String) -> Unit,
    onSearchMaterials: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val majors = remember { catalogRepo.curriculumMajors.keys.toList() }
    val activeMajor = selectedMajor.takeIf { it in majors } ?: majors.firstOrNull().orEmpty()
    val majorData = catalogRepo.curriculumMajors[activeMajor]
    val overrides by observeOverrides(activeMajor).collectAsState(initial = emptyMap())

    var selectedYear by remember { mutableIntStateOf(0) } // 0 = all semesters
    var search by remember { mutableStateOf("") }
    var showCodes by remember { mutableStateOf(true) }

    fun isSubjectCompleted(sem: Int, sub: CurriculumSubjectJson): Boolean {
        val key = "$sem::${sub.name}"
        overrides[key]?.let { return it }
        if (profileYear <= 0) return false
        val semYear = (sem + 1) / 2
        return semYear < profileYear
    }

    val semesterEntries = remember(majorData, selectedYear, search) {
        if (majorData == null) emptyList()
        else {
            val sems = when (selectedYear) {
                in 1..4 -> listOf((selectedYear - 1) * 2 + 1, selectedYear * 2)
                else -> (1..8).toList()
            }
            val q = CatalogRepository.normAz(search)
            sems.mapNotNull { sem ->
                val list = majorData.semesterList(sem).filter { sub ->
                    if (q.isBlank()) true
                    else {
                        val codes = (listOfNotNull(sub.code) + sub.codes).joinToString(" ")
                        CatalogRepository.normAz("${sub.name} $codes").contains(q)
                    }
                }
                if (list.isEmpty()) null else sem to list
            }
        }
    }

    val (completedCredits, totalCredits) = remember(majorData, overrides, profileYear) {
        if (majorData == null) 0 to 0
        else {
            var done = 0
            var tot = 0
            for (sem in 1..8) {
                majorData.semesterList(sem).forEach { sub ->
                    tot += sub.credit
                    if (isSubjectCompleted(sem, sub)) done += sub.credit
                }
            }
            done to tot
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s("navPlan"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(s("currHeaderSub"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            majors.forEach { mKey ->
                                FilterChip(
                                    selected = mKey == activeMajor,
                                    onClick = { onSelectMajor(mKey) },
                                    label = { Text(catalogRepo.majorLabel(mKey, strings.lang)) },
                                    modifier = Modifier.testTag("curr_major_$mKey")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(s("currCompletedCredits"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                            Text(
                                text = "$completedCredits / $totalCredits",
                                style = MaterialTheme.typography.titleSmall,
                                color = pal.accent2,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (totalCredits == 0) 0f else (completedCredits.toFloat() / totalCredits).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(s("currSearchPlaceholder")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("curr_search_input")
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedYear == 0,
                            onClick = { selectedYear = 0 },
                            label = { Text(s("currAllSemesters")) }
                        )
                        (1..4).forEach { y ->
                            FilterChip(
                                selected = selectedYear == y,
                                onClick = { selectedYear = y },
                                label = { Text(s("yearShort", "y" to y)) }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s("currShowCodes"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                    Switch(checked = showCodes, onCheckedChange = { showCodes = it })
                }
            }

            semesterEntries.forEach { (sem, list) ->
                item(key = "sem_hdr_$sem") {
                    Text(
                        text = s("semesterNum", "s" to sem),
                        style = MaterialTheme.typography.titleMedium,
                        color = pal.accent2,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(list, key = { "sub_${sem}_${it.name}" }) { sub ->
                    val checked = isSubjectCompleted(sem, sub)
                    val codesList = (listOfNotNull(sub.code) + sub.codes).filter { it.isNotBlank() }
                    val matches = remember(sub.name, codesList) {
                        catalogRepo.findMaterialsForCurriculumSubject(sub.name, codesList)
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val exactSub = matches.firstOrNull()?.subject
                                if (!exactSub.isNullOrBlank()) onOpenSubject(exactSub)
                                else if (matches.isNotEmpty()) onSearchMaterials(sub.name)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { next ->
                                    onToggleOverride(activeMajor, "$sem::${sub.name}", next)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = pal.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BadgePill("${sub.credit} ${s("currTableCredit")}", pal.accent2, pal.surfaceInner)
                                    BadgePill("${sub.hours}h", pal.textMuted, pal.surfaceInner)
                                    BadgePill("${s("currTableAbsence")}: ${sub.absenceLimit}", pal.warning, pal.warning.copy(alpha = 0.14f))
                                    if (showCodes && codesList.isNotEmpty()) {
                                        BadgePill(codesList.first(), pal.cyan, pal.cyan.copy(alpha = 0.14f))
                                    }
                                }
                            }
                            if (matches.isNotEmpty()) {
                                BadgePill(
                                    text = "${s("currMaterials")} (${matches.size})",
                                    color = pal.success,
                                    bg = pal.success.copy(alpha = 0.14f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = s("currTip"),
                    style = MaterialTheme.typography.bodySmall,
                    color = pal.textMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
