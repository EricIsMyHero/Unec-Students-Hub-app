package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPrefsState
import com.example.data.repository.NewsRepository
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import com.example.ui.theme.UnecThemes

private data class Contributor(val name: String, val role: String)

private val CONTRIBUTORS = listOf(
    Contributor("Nərimanov Elnur", "code"),
    Contributor("Şükürova Güləyar", "pdf"),
    Contributor("Hacıyev Tofiq", "pdf"),
    Contributor("Səfail Zeynalov", "pdf"),
    Contributor("İslamlı Həsən", "pdf"),
    Contributor("Həsənli Əsmər", "pdf")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MoreScreen(
    prefs: UserPrefsState,
    onSetLang: (String) -> Unit,
    onSetTheme: (String) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    var showSettings by remember { mutableStateOf(false) }
    var showContributors by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var policyDialog by remember { mutableStateOf<String?>(null) } // "privacy" or "terms"

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Star,
                    title = "${s("moreFavorites")} (${prefs.favoriteKeys.size})",
                    onClick = onOpenFavorites,
                    testTag = "more_favorites"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Palette,
                    title = s("moreSettings"),
                    onClick = { showSettings = true },
                    testTag = "more_settings"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Explore,
                    title = s("navCompass"),
                    onClick = onOpenCompass,
                    testTag = "more_compass"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Forum,
                    title = s("navRequests"),
                    onClick = onOpenRequests,
                    testTag = "more_requests"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Timer,
                    title = s("navTimer"),
                    onClick = onOpenTimer,
                    testTag = "more_timer"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Group,
                    title = s("moreContributors"),
                    onClick = { showContributors = true },
                    testTag = "more_contributors"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Info,
                    title = s("moreAbout"),
                    onClick = { showAbout = true },
                    testTag = "more_about"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Policy,
                    title = s("morePrivacy"),
                    onClick = { policyDialog = "privacy" },
                    testTag = "more_privacy"
                )
            }
            item {
                MoreRowItem(
                    icon = Icons.Rounded.Policy,
                    title = s("moreTerms"),
                    onClick = { policyDialog = "terms" },
                    testTag = "more_terms"
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = pal.surface,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s("moreContact"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SocialChip("Telegram") { IntentHelpers.openUrl(context, NewsRepository.TELEGRAM_CHANNEL_URL) }
                            SocialChip("WhatsApp") { IntentHelpers.openUrl(context, "https://whatsapp.com/channel/0029VbDlVlqJJhzeqDcnzV0t") }
                            SocialChip("Instagram") { IntentHelpers.openUrl(context, "https://www.instagram.com/unecstudentshub") }
                            SocialChip("TikTok") { IntentHelpers.openUrl(context, "https://www.tiktok.com/@unecstudentshub.com") }
                            SocialChip("Email", Icons.Rounded.Email) { IntentHelpers.openUrl(context, "mailto:unecstudentshub@gmail.com") }
                        }
                        Text(s("footerDisclaimer"), style = MaterialTheme.typography.labelSmall, color = pal.textMuted)
                    }
                }
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }, containerColor = pal.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(s("moreSettings"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(s("settingsLang"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = prefs.lang == "az",
                        onClick = { onSetLang("az") },
                        label = { Text("Azərbaycan (AZ)") },
                        modifier = Modifier.testTag("settings_lang_az")
                    )
                    FilterChip(
                        selected = prefs.lang == "en",
                        onClick = { onSetLang("en") },
                        label = { Text("English (EN)") },
                        modifier = Modifier.testTag("settings_lang_en")
                    )
                }

                Text(s("settingsTheme"), style = MaterialTheme.typography.labelLarge, color = pal.textMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnecThemes.forEach { theme ->
                        FilterChip(
                            selected = prefs.themeKey == theme.key,
                            onClick = { onSetTheme(theme.key) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(theme.accent)
                                )
                            },
                            label = { Text(if (strings.lang == "en") theme.labelEn else theme.labelAz) },
                            modifier = Modifier.testTag("theme_chip_${theme.key}")
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showContributors) {
        AlertDialog(
            onDismissRequest = { showContributors = false },
            title = { Text(s("moreContributors")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s("contribCode"), style = MaterialTheme.typography.labelLarge, color = pal.accent2)
                    CONTRIBUTORS.filter { it.role == "code" }.forEach { c ->
                        Text("• ${c.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(s("contribPdf"), style = MaterialTheme.typography.labelLarge, color = pal.accent2)
                    CONTRIBUTORS.filter { it.role == "pdf" }.forEach { c ->
                        Text("• ${c.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContributors = false }) { Text(s("btnClose")) }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(s("moreAbout")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s("aboutBody"))
                    Text(s("aboutMaintained"), color = pal.accent2, fontWeight = FontWeight.SemiBold)
                    Text(s("footerDisclaimer"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text(s("btnClose")) }
            }
        )
    }

    policyDialog?.let { kind ->
        val sections = remember(kind, strings.lang) { getPolicySections(kind, strings.lang) }
        AlertDialog(
            onDismissRequest = { policyDialog = null },
            title = { Text(if (kind == "privacy") s("morePrivacy") else s("moreTerms")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sections.forEach { (heading, body) ->
                        Text(heading, style = MaterialTheme.typography.titleSmall, color = pal.accent2, fontWeight = FontWeight.Bold)
                        Text(body, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { policyDialog = null }) { Text(s("btnClose")) }
            }
        )
    }
}

@Composable
private fun MoreRowItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    val pal = LocalUnecPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = pal.surface,
        border = BorderStroke(1.dp, pal.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = pal.accent2)
            Spacer(Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = pal.textMuted)
        }
    }
}

@Composable
private fun SocialChip(label: String, icon: ImageVector? = null, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) } },
        label = { Text(label) }
    )
}

private fun getPolicySections(kind: String, lang: String): List<Pair<String, String>> {
    return if (kind == "privacy") {
        if (lang == "en") listOf(
            "Local Storage" to "Your favorites, saved Compass articles, GPA subjects, exam countdowns, study notes, and preferences stay locally on your device.",
            "External Services" to "Materials are downloaded from Cloudflare R2 and Telegram updates are fetched over HTTPS.",
            "Contact" to "For data or copyright questions, contact unecstudentshub@gmail.com."
        ) else listOf(
            "Yerli yaddaş" to "Seçilmişlər, saxlanmış Kompas məqalələri, GPA qeydləri, imtahan tarixləri və ayarlar cihazınızda yerli olaraq saxlanılır.",
            "Xarici xidmətlər" to "Materiallar Cloudflare R2 üzərindən endirilir, son elanlar HTTPS ilə Telegram xəbər API-dən alınır.",
            "Əlaqə" to "Məlumat və müəllif hüquqları müraciətləri: unecstudentshub@gmail.com."
        )
    } else {
        if (lang == "en") listOf(
            "Platform" to "UNEC Students Hub is an independent student initiative and does not replace official academic records.",
            "Tools" to "Verify GPA, credits, and test answers against official UNEC syllabi and announcements.",
            "Contact" to "Questions and feedback: unecstudentshub@gmail.com."
        ) else listOf(
            "Platforma" to "UNEC Students Hub müstəqil tələbə təşəbbüsüdür və rəsmi akademik sənədləri əvəz etmir.",
            "Alətlər" to "GPA, kredit və test cavablarını rəsmi UNEC sillabus və elanları ilə yoxlayın.",
            "Əlaqə" to "Suallar və təkliflər: unecstudentshub@gmail.com."
        )
    }
}
