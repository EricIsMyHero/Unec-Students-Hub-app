package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaterialItem
import com.example.data.model.PdfTypeMeta
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnecTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    hasUnreadUpdates: Boolean = false,
    onOpenUpdates: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onToggleLang: () -> Unit = {},
    onOpenDashboard: () -> Unit = {}
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = pal.bgAlt,
            titleContentColor = pal.textPrimary,
            navigationIconContentColor = pal.textPrimary
        ),
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = s("btnBack")
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pal.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(
                onClick = onOpenUpdates,
                modifier = Modifier.testTag("top_updates_button")
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = s("notifTitle"),
                        tint = pal.textPrimary
                    )
                    if (hasUnreadUpdates) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(pal.danger)
                        )
                    }
                }
            }
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("top_theme_toggle")
            ) {
                Icon(
                    imageVector = if (pal.isLight) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = s("settingsTheme"),
                    tint = pal.textPrimary
                )
            }
            Surface(
                onClick = onToggleLang,
                shape = RoundedCornerShape(8.dp),
                color = pal.surfaceInner,
                border = BorderStroke(1.dp, pal.border),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_lang_toggle")
            ) {
                Text(
                    text = strings.lang.uppercase(),
                    color = pal.accent2,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
            IconButton(
                onClick = onOpenDashboard,
                modifier = Modifier.testTag("top_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = s("navDashboard"),
                    tint = pal.accent2
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialCard(
    item: MaterialItem,
    isFavorite: Boolean,
    pdfTypes: Map<String, PdfTypeMeta>,
    onToggleFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
    onPdfOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("material_card_${item.file}")
            .clickable(onClick = onOpenDetail),
        shape = RoundedCornerShape(16.dp),
        color = pal.surface,
        border = BorderStroke(1.dp, pal.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pal.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = pal.accent2,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = pal.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.subject.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.subject,
                            style = MaterialTheme.typography.bodySmall,
                            color = pal.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (item.desc.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = pal.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("fav_toggle_${item.file}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (isFavorite) s("favRemoved") else s("favAdded"),
                        tint = if (isFavorite) pal.warning else pal.textMuted
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.course.isNotBlank()) {
                    BadgePill(text = item.course, color = pal.accent2, bg = pal.surfaceInner)
                }
                if (item.format == "test") {
                    BadgePill(text = s("formatTest"), color = pal.fcTests, bg = pal.fcTests.copy(alpha = 0.14f))
                } else if (item.format == "yazili") {
                    BadgePill(text = s("formatWritten"), color = pal.warning, bg = pal.warning.copy(alpha = 0.14f))
                } else if (item.kind == "extra") {
                    BadgePill(text = s("extraMaterial"), color = pal.cyan, bg = pal.cyan.copy(alpha = 0.14f))
                }
                val pMeta = pdfTypes[item.pdfType]
                if (pMeta != null) {
                    val c = IntentHelpers.parseColorOr(pMeta.color, pal.accent2)
                    BadgePill(text = pMeta.label, color = c, bg = c.copy(alpha = 0.14f))
                }
                if (item.code.isNotBlank()) {
                    BadgePill(text = item.code, color = pal.textMuted, bg = pal.surfaceInner)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onPdfOpened()
                        IntentHelpers.openUrl(context, item.url)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_pdf_${item.file}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pal.accent, contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s("appOpen"), fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        onPdfOpened()
                        IntentHelpers.downloadPdf(context, item.url, item.file, strings)
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("download_pdf_${item.file}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, pal.border),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = s("appDownload"), tint = pal.textPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s("appDownload"), color = pal.textPrimary, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun BadgePill(
    text: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}
