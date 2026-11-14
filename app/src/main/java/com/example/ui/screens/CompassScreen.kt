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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CompassArticleJson
import com.example.data.repository.CatalogRepository
import com.example.ui.components.BadgePill
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

private val COMPASS_CATEGORIES = listOf(
    "admissions", "first-year", "academic", "finance", "campuses",
    "documents", "work", "campus", "exchange", "rights", "military"
)

@Composable
fun CompassScreen(
    articles: List<CompassArticleJson>,
    savedIds: Set<String>,
    onToggleSave: (String) -> Unit,
    onOpenArticle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current

    var audience by remember { mutableStateOf("all") } // all, student, applicant
    var category by remember { mutableStateOf("all") }
    var savedOnly by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val filtered = remember(articles, audience, category, savedOnly, savedIds, search) {
        val terms = CatalogRepository.normAz(search).split(" ").filter { it.isNotBlank() }
        articles.filter { art ->
            if (savedOnly && art.id !in savedIds) return@filter false
            if (audience != "all" && audience !in art.audience) return@filter false
            if (category != "all" && art.category != category) return@filter false
            if (terms.isNotEmpty()) {
                val blob = buildString {
                    append(art.title.az).append(' ').append(art.title.en).append(' ')
                    append(art.summary.az).append(' ').append(art.summary.en).append(' ')
                    art.sections.forEach { sec ->
                        append(sec.heading.az).append(' ').append(sec.heading.en).append(' ')
                        sec.paragraphs.forEach { p -> append(p.az).append(' ').append(p.en).append(' ') }
                    }
                }
                val normBlob = CatalogRepository.normAz(blob)
                terms.all { it in normBlob }
            } else true
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(s("compassIntro"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(s("compassSearchPlaceholder")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = s("btnClear"))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("compass_search_input")
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = audience == "all" && !savedOnly,
                        onClick = { audience = "all"; savedOnly = false },
                        label = { Text(s("compassAll")) }
                    )
                    FilterChip(
                        selected = audience == "student",
                        onClick = { audience = "student"; savedOnly = false },
                        label = { Text(s("compassStudents")) }
                    )
                    FilterChip(
                        selected = audience == "applicant",
                        onClick = { audience = "applicant"; savedOnly = false },
                        label = { Text(s("compassApplicants")) }
                    )
                    FilterChip(
                        selected = savedOnly,
                        onClick = { savedOnly = !savedOnly },
                        label = { Text("${s("compassSaved")} (${savedIds.size})") },
                        modifier = Modifier.testTag("compass_saved_chip")
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = category == "all",
                        onClick = { category = "all" },
                        label = { Text(s("compassAll")) }
                    )
                    COMPASS_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = if (category == cat) "all" else cat },
                            label = { Text(IntentHelpers.compassCategoryLabel(cat, strings)) }
                        )
                    }
                }

                Text(
                    text = s("compassCount", "n" to filtered.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = pal.textMuted
                )
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (savedOnly) s("compassEmptySaved") else s("matEmpty"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = pal.textMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { art ->
                        val isSaved = art.id in savedIds
                        Surface(
                            onClick = { onOpenArticle(art.id) },
                            shape = RoundedCornerShape(16.dp),
                            color = pal.surface,
                            border = BorderStroke(1.dp, pal.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compass_card_${art.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = art.title.get(strings.lang),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = pal.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = art.summary.get(strings.lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = pal.textMuted,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { onToggleSave(art.id) }) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                            contentDescription = s("compassSaved"),
                                            tint = if (isSaved) pal.warning else pal.textMuted
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BadgePill(
                                            text = IntentHelpers.compassCategoryLabel(art.category, strings),
                                            color = pal.accent2,
                                            bg = pal.surfaceInner
                                        )
                                        BadgePill(
                                            text = s("compassMinutes", "n" to art.readMinutes),
                                            color = pal.textMuted,
                                            bg = pal.surfaceInner
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = pal.textMuted,
                                        modifier = Modifier.size(20.dp)
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
