package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MaterialRequestEntity
import com.example.data.local.RequestReplyEntity
import com.example.ui.components.BadgePill
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import kotlinx.coroutines.flow.Flow

@Composable
fun RequestsScreen(
    requests: List<MaterialRequestEntity>,
    onAddRequest: (text: String, course: String, subject: String) -> Unit,
    onToggleUpvote: (MaterialRequestEntity) -> Unit,
    onToggleFulfilled: (MaterialRequestEntity) -> Unit,
    onDeleteRequest: (Long) -> Unit,
    onOpenThread: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    var filter by remember { mutableStateOf("all") } // all, open, fulfilled
    var showCreate by remember { mutableStateOf(false) }

    val filtered = remember(requests, filter) {
        when (filter) {
            "open" -> requests.filter { it.status == "open" }
            "fulfilled" -> requests.filter { it.status == "fulfilled" }
            else -> requests
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp),
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
                        Text(s("reqPageTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(s("reqHeroSub"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                        Button(
                            onClick = { showCreate = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("req_new_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(s("reqNewBtn"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "all" to s("reqFilterAll"),
                        "open" to s("reqFilterOpen"),
                        "fulfilled" to s("reqFilterFulfilled")
                    ).forEach { (k, label) ->
                        FilterChip(
                            selected = filter == k,
                            onClick = { filter = k },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = s("reqEmpty"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = pal.textMuted,
                        modifier = Modifier.padding(vertical = 18.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { req ->
                    val found = req.status == "fulfilled"
                    Surface(
                        onClick = { onOpenThread(req.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = pal.surface,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(req.authorName, style = MaterialTheme.typography.labelMedium, color = pal.accent2, fontWeight = FontWeight.Bold)
                                BadgePill(
                                    text = if (found) s("reqStatusFound") else s("reqStatusOpen"),
                                    color = if (found) pal.success else pal.warning,
                                    bg = (if (found) pal.success else pal.warning).copy(alpha = 0.14f)
                                )
                            }
                            Text(req.text, style = MaterialTheme.typography.bodyMedium, color = pal.textPrimary)
                            if (req.subject.isNotBlank() || req.course.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (req.course.isNotBlank()) BadgePill(req.course, pal.textMuted, pal.surfaceInner)
                                    if (req.subject.isNotBlank()) BadgePill(req.subject, pal.accent2, pal.surfaceInner)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleUpvote(req) },
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(Icons.Rounded.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${req.upvotes}")
                                }
                                OutlinedButton(
                                    onClick = { onToggleFulfilled(req) },
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(s("reqMarkFound"), maxLines = 1)
                                }
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { onDeleteRequest(req.id) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = s("btnClear"), tint = pal.textMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var text by remember { mutableStateOf("") }
        var course by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text(s("reqNewBtn")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text(s("reqTextPlaceholder")) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text(s("mdCourse")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(s("mdSubject")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddRequest(text, course, subject)
                        showCreate = false
                    },
                    enabled = text.isNotBlank()
                ) { Text(s("btnSend")) }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text(s("btnCancel")) }
            }
        )
    }
}

@Composable
fun RequestThreadScreen(
    request: MaterialRequestEntity?,
    repliesFlow: Flow<List<RequestReplyEntity>>,
    onAddReply: (String) -> Unit,
    onDeleteReply: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pal = LocalUnecPalette.current
    val replies by repliesFlow.collectAsState(initial = emptyList())
    var replyText by remember { mutableStateOf("") }

    if (request == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s("reqEmpty"), color = pal.textMuted)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = pal.surface,
                border = BorderStroke(1.dp, pal.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(request.authorName, style = MaterialTheme.typography.labelLarge, color = pal.accent2, fontWeight = FontWeight.Bold)
                    Text(request.text, style = MaterialTheme.typography.titleMedium, color = pal.textPrimary)
                }
            }

            Text(s("repliesTitle"), style = MaterialTheme.typography.titleSmall, color = pal.accent2, fontWeight = FontWeight.Bold)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(replies, key = { it.id }) { rep ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = pal.surfaceInner,
                        border = BorderStroke(1.dp, pal.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rep.authorName, style = MaterialTheme.typography.labelSmall, color = pal.accent2, fontWeight = FontWeight.Bold)
                                Text(rep.text, style = MaterialTheme.typography.bodySmall, color = pal.textPrimary)
                            }
                            IconButton(onClick = { onDeleteReply(rep.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = s("btnClear"), tint = pal.textMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text(s("reqReplyPlaceholder")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onAddReply(replyText)
                            replyText = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = s("btnSend"), tint = pal.accent2)
                }
            }
        }
    }
}
