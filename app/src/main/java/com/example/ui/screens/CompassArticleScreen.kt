package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CompassArticleJson
import com.example.ui.components.BadgePill
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

@Composable
fun CompassArticleScreen(
    articleId: String,
    articles: List<CompassArticleJson>,
    savedIds: Set<String>,
    onToggleSave: (String) -> Unit,
    onOpenArticle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val article = remember(articleId, articles) { articles.firstOrNull { it.id == articleId } }

    if (article == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s("matEmpty"), color = pal.textMuted)
        }
        return
    }

    val isSaved = article.id in savedIds
    var tocExpanded by remember { mutableStateOf(false) }
    val related = remember(article, articles) {
        articles.filter { it.id != article.id && it.category == article.category }.take(3)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BadgePill(
                                IntentHelpers.compassCategoryLabel(article.category, strings),
                                pal.accent2,
                                pal.surfaceInner
                            )
                            BadgePill(s("compassMinutes", "n" to article.readMinutes), pal.textMuted, pal.surfaceInner)
                        }
                        Text(
                            text = article.title.get(strings.lang),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = article.summary.get(strings.lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = pal.textMuted
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onToggleSave(article.id) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("compass_article_save")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (isSaved) pal.warning else pal.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s("compassSaved"))
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareBody = "${article.title.get(strings.lang)}\n\n${article.summary.get(strings.lang)}"
                                    IntentHelpers.shareText(context, article.title.get(strings.lang), shareBody)
                                },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("compass_article_share")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s("btnShare"))
                            }
                        }
                    }
                }
            }

            if (article.sections.isNotEmpty()) {
                item {
                    Surface(
                        onClick = { tocExpanded = !tocExpanded },
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surfaceInner,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(s("compassToc"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (tocExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            if (tocExpanded) {
                                Spacer(Modifier.height(8.dp))
                                article.sections.forEachIndexed { idx, sec ->
                                    Text(
                                        text = "${idx + 1}. ${sec.heading.get(strings.lang)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = pal.accent2,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(article.sections) { sec ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = sec.heading.get(strings.lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = pal.accent2,
                            fontWeight = FontWeight.Bold
                        )
                        sec.paragraphs.forEach { p ->
                            Text(p.get(strings.lang), style = MaterialTheme.typography.bodyMedium, color = pal.textPrimary)
                        }
                        sec.bullets.forEach { b ->
                            Text("• ${b.get(strings.lang)}", style = MaterialTheme.typography.bodySmall, color = pal.textPrimary)
                        }
                        sec.table?.let { tbl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(pal.surfaceInner, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (tbl.headers.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            tbl.headers.forEach { h ->
                                                Text(
                                                    text = h.get(strings.lang),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = pal.accent2,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.width(140.dp)
                                                )
                                            }
                                        }
                                    }
                                    tbl.rows.forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            row.forEach { cell ->
                                                Text(
                                                    text = cell.get(strings.lang),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = pal.textPrimary,
                                                    modifier = Modifier.width(140.dp)
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

            if (article.steps.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(s("compassSteps"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            article.steps.forEach { st ->
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, tint = pal.success, modifier = Modifier.size(18.dp))
                                    Text(st.get(strings.lang), style = MaterialTheme.typography.bodySmall, color = pal.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            if (article.sources.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(s("compassSources"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            article.sources.forEach { src ->
                                OutlinedButton(
                                    onClick = { IntentHelpers.openUrl(context, src.url) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(src.title.get(strings.lang), modifier = Modifier.weight(1f), maxLines = 1)
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(s("compassNote"), style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
                        }
                    }
                }
            }

            if (related.isNotEmpty()) {
                item {
                    Text(s("compassRelated"), style = MaterialTheme.typography.titleSmall, color = pal.accent2, fontWeight = FontWeight.Bold)
                }
                items(related, key = { "rel_${it.id}" }) { rel ->
                    Surface(
                        onClick = { onOpenArticle(rel.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(rel.title.get(strings.lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(rel.summary.get(strings.lang), style = MaterialTheme.typography.bodySmall, color = pal.textMuted, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}
