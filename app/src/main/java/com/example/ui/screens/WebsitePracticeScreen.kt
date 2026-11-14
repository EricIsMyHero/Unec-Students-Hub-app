package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.IntentHelpers
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette

@Composable
fun WebsitePracticeScreen() {
    val context = LocalContext.current
    val pal = LocalUnecPalette.current
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
        Surface(shape = RoundedCornerShape(18.dp), color = pal.surface,
            border = BorderStroke(1.dp, pal.border), modifier = Modifier.fillMaxWidth().widthIn(max = 820.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s("practiceWebTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(s("practiceWebDescription"), style = MaterialTheme.typography.bodyMedium, color = pal.textMuted)
                Button(onClick = { IntentHelpers.openUrl(context, "https://unecstudentshub.com/#/tests") },
                    modifier = Modifier.fillMaxWidth()) { Text(s("practiceOpenWebsite")) }
            }
        }
    }
}
