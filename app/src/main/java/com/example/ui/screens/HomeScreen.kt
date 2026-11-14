package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.NewsRepository
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import com.example.ui.viewmodel.NewsUiState

private data class FeatureTile(
    val tag: String,
    val titleKey: String,
    val descKey: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    totalMaterials: Int,
    totalSubjects: Int,
    totalMajors: Int,
    newsState: NewsUiState,
    onRefreshNews: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenTests: () -> Unit,
    onOpenGpa: () -> Unit,
    onOpenCurriculum: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenCourses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    val features = listOf(
        FeatureTile("tile_tests", "navTests", "featTestsDesc", Icons.Rounded.Quiz, pal.fcTests, onOpenTests),
        FeatureTile("tile_gpa", "navGpa", "featGpaDesc", Icons.Rounded.Calculate, pal.fcGpa, onOpenGpa),
        FeatureTile("tile_plan", "navPlan", "featPlanDesc", Icons.Rounded.AccountTree, pal.fcPlan, onOpenCurriculum),
        FeatureTile("tile_requests", "navRequests", "featRequestsDesc", Icons.Rounded.Forum, pal.fcRequests, onOpenRequests),
        FeatureTile("tile_timer", "navTimer", "featTimerDesc", Icons.Rounded.Timer, pal.fcTimer, onOpenTimer),
        FeatureTile("tile_dashboard", "navDashboard", "featDashDesc", Icons.Rounded.AutoGraph, pal.fcDash, onOpenDashboard)
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(172.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_campus),
                                contentDescription = stringResource(id = R.string.hero_banner_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.25f),
                                                pal.bg.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = s("homeWhyTitle"),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = s("homeWhySub"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.86f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroStatBox(
                                value = "$totalMaterials+",
                                label = s("homeStatMaterials"),
                                color = pal.accent2,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onOpenCourses)
                            )
                            HeroStatBox(
                                value = "$totalSubjects+",
                                label = s("homeStatSubjects"),
                                color = pal.success,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onOpenCourses)
                            )
                            HeroStatBox(
                                value = "$totalMajors",
                                label = s("homeStatMajors"),
                                color = pal.fcPlan,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onOpenCurriculum)
                            )
                        }
                    }
                }
            }

            // Student Compass featured card
            item {
                Surface(
                    onClick = onOpenCompass,
                    shape = RoundedCornerShape(18.dp),
                    color = pal.accent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, pal.accent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_compass_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pal.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s("navCompass"),
                                style = MaterialTheme.typography.titleMedium,
                                color = pal.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = s("featCompassDesc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = pal.textMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = s("navCompass"),
                            tint = pal.accent2
                        )
                    }
                }
            }

            // 6-card Feature Grid
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cols = if (maxWidth >= 600.dp) 3 else 2
                    val rows = features.chunked(cols)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { tile ->
                                    Surface(
                                        onClick = tile.onClick,
                                        shape = RoundedCornerShape(18.dp),
                                        color = pal.surface,
                                        border = BorderStroke(1.dp, pal.border),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(tile.tag)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(tile.color.copy(alpha = 0.16f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = tile.icon,
                                                    contentDescription = null,
                                                    tint = tile.color,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Text(
                                                text = s(tile.titleKey),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = pal.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = s(tile.descKey),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = pal.textMuted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size < cols) {
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Latest updates from Telegram
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = s("homeNewsTitle"),
                                style = MaterialTheme.typography.titleMedium,
                                color = pal.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { IntentHelpers.openUrl(context, NewsRepository.TELEGRAM_CHANNEL_URL) },
                                modifier = Modifier.testTag("home_news_see_all")
                            ) {
                                Text(s("homeNewsAll"), color = pal.accent2)
                            }
                        }

                        when {
                            newsState.isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = pal.accent, modifier = Modifier.size(28.dp))
                                }
                            }

                            newsState.error != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = s("notifEmpty"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = pal.textMuted
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onRefreshNews,
                                        modifier = Modifier.testTag("home_news_retry")
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(s("btnRetry"))
                                    }
                                }
                            }

                            newsState.posts.isEmpty() -> {
                                Text(
                                    text = s("notifEmpty"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = pal.textMuted,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    newsState.posts.forEach { post ->
                                        Surface(
                                            onClick = { IntentHelpers.openUrl(context, post.url) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = pal.surfaceInner,
                                            border = BorderStroke(1.dp, pal.border),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = s("newsFromTelegramShort"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = pal.accent2,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (post.date.isNotBlank()) {
                                                        Text(
                                                            text = IntentHelpers.formatDateShort(post.date, strings.lang),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = pal.textMuted
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = post.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = pal.textPrimary,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { IntentHelpers.openUrl(context, NewsRepository.TELEGRAM_CHANNEL_URL) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("home_telegram_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = pal.accent, contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s("newsTelegram"), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatBox(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = pal.surfaceInner,
        border = BorderStroke(1.dp, pal.border)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = pal.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
