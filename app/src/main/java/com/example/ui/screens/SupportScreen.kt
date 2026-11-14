package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

private const val BIRBANK_URL = "https://app.birbank.az/qp?t=2&id=3F12353F2088F167E0634C0214ACD220"
private const val M10_URL = "https://m10.onelink.me/g54T/cw5bg1gx"

@Composable
fun SupportScreen(modifier: Modifier = Modifier) {
    val pal = LocalUnecPalette.current
    val context = LocalContext.current

    var qrModalRes by remember { mutableStateOf<Pair<Int, String>?>(null) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
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
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = pal.fcSupport)
                            Spacer(Modifier.width(8.dp))
                            Text(s("supportPageTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Text(s("supportPageDesc"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                    }
                }
            }

            item {
                SupportChannelCard(
                    title = s("supportBirbankLabel"),
                    url = BIRBANK_URL,
                    icon = Icons.Rounded.CreditCard,
                    onOpenLink = { IntentHelpers.openUrl(context, BIRBANK_URL) },
                    onShowQr = { qrModalRes = R.drawable.birbank_qr to context.getString(R.string.qr_birbank_desc) },
                    testTagPrefix = "support_birbank"
                )
            }

            item {
                SupportChannelCard(
                    title = s("supportM10Label"),
                    url = M10_URL,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    onOpenLink = { IntentHelpers.openUrl(context, M10_URL) },
                    onShowQr = { qrModalRes = R.drawable.m10_qr to context.getString(R.string.qr_m10_desc) },
                    testTagPrefix = "support_m10"
                )
            }

            // Explicit card gateway placeholder notice (no dead-end checkout button)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = pal.surfaceInner,
                    border = BorderStroke(1.dp, pal.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s("supportCardTitle"), style = MaterialTheme.typography.titleSmall, color = pal.accent2, fontWeight = FontWeight.Bold)
                        Text(s("supportTodoGateway"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                    }
                }
            }
        }
    }

    qrModalRes?.let { (resId, desc) ->
        AlertDialog(
            onDismissRequest = { qrModalRes = null },
            title = { Text(s("supportQrPay")) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = desc,
                        modifier = Modifier.size(240.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { qrModalRes = null }) { Text(s("btnClose")) }
            }
        )
    }
}

@Composable
private fun SupportChannelCard(
    title: String,
    url: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onOpenLink: () -> Unit,
    onShowQr: () -> Unit,
    testTagPrefix: String
) {
    val pal = LocalUnecPalette.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = pal.surface,
        border = BorderStroke(1.dp, pal.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = pal.accent2)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenLink,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("${testTagPrefix}_open"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s("supportOpenApp"), maxLines = 1)
                }
                OutlinedButton(
                    onClick = onShowQr,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("${testTagPrefix}_qr"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s("supportQrPay"), maxLines = 1)
                }
            }
        }
    }
}
