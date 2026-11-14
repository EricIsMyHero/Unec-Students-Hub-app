package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.SubjectSummary
import com.example.data.repository.CatalogRepository
import com.example.ui.components.BadgePill
import com.example.ui.components.MaterialCard
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MaterialsScreen(
    catalogRepo: CatalogRepository,
    favoriteKeys: Set<String>,
    selectedMajor: String,
    selectedYear: Int,
    initialFavOnly: Boolean = false,
    initialSubjectSearch: String = "",
    onToggleFavorite: (String) -> Unit,
    onSelectMajor: (String) -> Unit,
    onSelectYear: (Int) -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenMaterial: (String) -> Unit,
    onPdfOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current

    var mode by remember { mutableIntStateOf(if (initialFavOnly || initialSubjectSearch.isNotBlank()) 0 else 0) }
    var searchQuery by remember { mutableStateOf(initialSubjectSearch) }
    var favOnly by remember { mutableStateOf(initialFavOnly) }
    var formatFilter by remember { mutableStateOf("all") } // all, test, yazili, extra
    var typeFilter by remember { mutableStateOf("all") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val allMaterials = catalogRepo.allMaterials
    val pdfTypes = catalogRepo.pdfTypes
    val majorKeys = remember {
        listOf("all") + CatalogRepository.MAJOR_I18N_KEYS.keys.toList() + CatalogRepository.TUDIFAK_KEY
    }

    val filteredMaterials = remember(
        allMaterials, searchQuery, favOnly, favoriteKeys, formatFilter, typeFilter, selectedMajor
    ) {
        val terms = CatalogRepository.normAz(searchQuery).split(" ").filter { it.isNotBlank() }
        allMaterials.filter { m ->
            if (favOnly && m.favKey !in favoriteKeys) return@filter false
            if (formatFilter == "extra" && m.kind != "extra") return@filter false
            if (formatFilter in listOf("test", "yazili") && m.format != formatFilter) return@filter false
            if (typeFilter != "all" && m.pdfType != typeFilter) return@filter false
            if (selectedMajor != "all") {
                if (m.kind == "extra") return@filter false
                val subjName = m.subject.orEmpty()
                if (!catalogRepo.subjectMatchesMajor(subjName, m.code, m.majors, m.tudifak, selectedMajor)) {
                    return@filter false
                }
            }
            if (terms.isNotEmpty()) {
                terms.all { t -> m.searchNormalized.contains(t) }
            } else true
        }
    }

    val subjectsBySemester = remember(selectedYear, selectedMajor, searchQuery) {
        val fallSem = (selectedYear - 1) * 2 + 1
        val springSem = fallSem + 1
        val normSearch = CatalogRepository.normAz(searchQuery)
        val matching = catalogRepo.subjectCatalog.values.filter { sub ->
            if (!catalogRepo.subjectMatchesMajor(sub.name, sub.code, sub.majors, sub.tudifak, selectedMajor)) {
                return@filter false
            }
            if (normSearch.isNotBlank() && !CatalogRepository.normAz("${sub.name} ${sub.code}").contains(normSearch)) {
                return@filter false
            }
            true
        }

        val fall = mutableListOf<SubjectSummary>()
        val spring = mutableListOf<SubjectSummary>()
        val unknown = mutableListOf<SubjectSummary>()
        matching.forEach { sub ->
            val sems = catalogRepo.subjectSemesters(sub.name, sub.code, sub.entries, selectedMajor)
            val inFall = fallSem in sems
            val inSpring = springSem in sems
            if (inFall) fall.add(sub)
            if (inSpring) spring.add(sub)
            if (sems.isEmpty() && selectedYear == 1) unknown.add(sub)
        }
        Triple(
            fall.sortedBy { it.name },
            spring.sortedBy { it.name },
            unknown.sortedBy { it.name }
        )
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
        ) {
            // Mode Switch
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                SegmentedButton(
                    selected = mode == 0,
                    onClick = { mode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    modifier = Modifier.testTag("courses_mode_all")
                ) {
                    Text(s("courseModeAll"))
                }
                SegmentedButton(
                    selected = mode == 1,
                    onClick = { mode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    modifier = Modifier.testTag("courses_mode_subjects")
                ) {
                    Text(s("courseModeSubjects"))
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(s("matSearchPlaceholder"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = s("btnClear"))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("materials_search_input")
            )

            Spacer(Modifier.height(8.dp))

            if (mode == 0) {
                // Quick filter bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !favOnly,
                        onClick = { favOnly = false },
                        label = { Text(s("matFilterAll")) },
                        modifier = Modifier.testTag("chip_materials_all")
                    )
                    FilterChip(
                        selected = favOnly,
                        onClick = { favOnly = true },
                        label = { Text("${s("matFilterFav")} (${favoriteKeys.size})") },
                        modifier = Modifier.testTag("chip_materials_fav")
                    )
                    val activeExtra = (if (formatFilter != "all") 1 else 0) +
                        (if (typeFilter != "all") 1 else 0) +
                        (if (selectedMajor != "all") 1 else 0)
                    FilterChip(
                        selected = activeExtra > 0,
                        onClick = { showFilterSheet = true },
                        leadingIcon = { Icon(Icons.Rounded.FilterList, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = {
                            Text(if (activeExtra > 0) "${s("matFilterTitle")} ($activeExtra)" else s("matFilterTitle"))
                        },
                        modifier = Modifier.testTag("chip_materials_filter")
                    )
                }

                Text(
                    text = s("matCount", "n" to filteredMaterials.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = pal.textMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (filteredMaterials.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (favOnly) s("matEmptyFav") else s("matEmpty"),
                                style = MaterialTheme.typography.titleMedium,
                                color = pal.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (favOnly) s("matEmptyFavHint") else s("matEmptyHint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = pal.textMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredMaterials, key = { it.favKey }) { item ->
                            MaterialCard(
                                item = item,
                                isFavorite = item.favKey in favoriteKeys,
                                pdfTypes = pdfTypes,
                                onToggleFavorite = { onToggleFavorite(item.favKey) },
                                onOpenDetail = { onOpenMaterial(item.favKey) },
                                onPdfOpened = onPdfOpened
                            )
                        }
                    }
                }
            } else {
                // Year selector + Major selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..4).forEach { y ->
                        FilterChip(
                            selected = selectedYear == y,
                            onClick = { onSelectYear(y) },
                            label = { Text(s("yearShort", "y" to y)) },
                            modifier = Modifier.testTag("year_chip_$y")
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    majorKeys.forEach { mKey ->
                        val label = if (mKey == "all") s("majorAll") else catalogRepo.majorLabel(mKey, strings.lang)
                        FilterChip(
                            selected = selectedMajor == mKey,
                            onClick = { onSelectMajor(mKey) },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }

                val (fallList, springList, unknownList) = subjectsBySemester
                if (fallList.isEmpty() && springList.isEmpty() && unknownList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = s("noMajorSubjects"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = pal.textMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (fallList.isNotEmpty()) {
                            item {
                                Text(
                                    text = s("semesterFall"),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = pal.accent2,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(fallList, key = { "fall_${it.name}" }) { sub ->
                                SubjectRowCard(sub = sub, onClick = { onOpenSubject(sub.name) })
                            }
                        }
                        if (springList.isNotEmpty()) {
                            item {
                                Text(
                                    text = s("semesterSpring"),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = pal.accent2,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(springList, key = { "spring_${it.name}" }) { sub ->
                                SubjectRowCard(sub = sub, onClick = { onOpenSubject(sub.name) })
                            }
                        }
                        if (unknownList.isNotEmpty()) {
                            item {
                                Text(
                                    text = s("semesterUnknown"),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = pal.textMuted,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(unknownList, key = { "unk_${it.name}" }) { sub ->
                                SubjectRowCard(sub = sub, onClick = { onOpenSubject(sub.name) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = pal.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(s("matFilterTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text(s("matFilterFormat"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "all" to s("matFilterAll"),
                        "test" to s("formatTest"),
                        "yazili" to s("formatWritten"),
                        "extra" to s("extraMaterial")
                    ).forEach { (k, label) ->
                        FilterChip(
                            selected = formatFilter == k,
                            onClick = { formatFilter = k },
                            label = { Text(label) }
                        )
                    }
                }

                Text(s("matFilterType"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = typeFilter == "all",
                        onClick = { typeFilter = "all" },
                        label = { Text(s("matFilterAll")) }
                    )
                    pdfTypes.forEach { (k, meta) ->
                        FilterChip(
                            selected = typeFilter == k,
                            onClick = { typeFilter = k },
                            label = { Text(meta.label) }
                        )
                    }
                }

                Text(s("matFilterMajor"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    majorKeys.forEach { k ->
                        val label = if (k == "all") s("majorAll") else catalogRepo.majorLabel(k, strings.lang)
                        FilterChip(
                            selected = selectedMajor == k,
                            onClick = { onSelectMajor(k) },
                            label = { Text(label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            formatFilter = "all"
                            typeFilter = "all"
                            onSelectMajor("all")
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(s("matFilterReset"))
                    }
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(s("matFilterApply"))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SubjectRowCard(
    sub: SubjectSummary,
    onClick: () -> Unit
) {
    val pal = LocalUnecPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = pal.surface,
        border = BorderStroke(1.dp, pal.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${sub.name}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Book, contentDescription = null, tint = pal.accent2)
            }
            Spacer(Modifier.width(10.dp))
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
                    if (sub.type == "test") {
                        BadgePill(s("formatTest"), pal.fcTests, pal.fcTests.copy(alpha = 0.14f))
                    } else if (sub.type == "yazili") {
                        BadgePill(s("formatWritten"), pal.warning, pal.warning.copy(alpha = 0.14f))
                    }
                    if (sub.code.isNotBlank()) {
                        BadgePill(sub.code, pal.textMuted, pal.surfaceInner)
                    }
                    BadgePill(s("matCount", "n" to sub.entries.size), pal.accent2, pal.surfaceInner)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = pal.textMuted
            )
        }
    }
}
