package com.vava8.app.ui.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vava8.app.Vava8App
import com.vava8.app.data.model.SiteChannels
import com.vava8.app.data.prefs.PostDraft
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    onLoginRequired: () -> Unit,
    onCreated: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val repo = Vava8App.instance.repository
    val prefs = Vava8App.instance.preferences
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var channelIndex by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }
    var draftLoaded by remember { mutableStateOf(false) }
    val channel = SiteChannels.all[channelIndex]

    LaunchedEffect(Unit) {
        val draft = prefs.loadPostDraft()
        if (draft.hasContent) {
            title = draft.title
            content = draft.content
            val idx = SiteChannels.all.indexOfFirst { it.id == draft.channelId }
            if (idx >= 0) channelIndex = idx
        }
        draftLoaded = true
    }

    LaunchedEffect(title, content, channelIndex, draftLoaded) {
        if (!draftLoaded) return@LaunchedEffect
        val ch = SiteChannels.all.getOrNull(channelIndex) ?: SiteChannels.all.first()
        prefs.savePostDraft(
            PostDraft(title = title, content = content, channelId = ch.id)
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("发布帖子") }) },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "分享你的见闻与原创内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                    modifier = Modifier.weight(1f)
                )
                if (title.isNotBlank() || content.isNotBlank()) {
                    TextButton(
                        onClick = {
                            title = ""
                            content = ""
                            channelIndex = 0
                            prefs.clearPostDraft()
                            scope.launch { snackbar.showSnackbar("草稿已清空") }
                        }
                    ) { Text("清空草稿") }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 100) title = it },
                label = { Text("标题") },
                placeholder = { Text("不超过 50 个汉字") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = channel.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("频道") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SiteChannels.all.forEachIndexed { index, ch ->
                        DropdownMenuItem(
                            text = { Text(ch.name) },
                            onClick = {
                                channelIndex = index
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("正文") },
                placeholder = { Text("写下你想说的话...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (!repo.user.value.isLoggedIn) {
                        onLoginRequired(); return@Button
                    }
                    if (title.isBlank() || content.isBlank()) {
                        scope.launch { snackbar.showSnackbar("请填写标题和正文") }
                        return@Button
                    }
                    submitting = true
                    scope.launch {
                        val res = repo.createPost(title.trim(), content.trim(), channel.id)
                        submitting = false
                        if (res.ok == 1) {
                            title = ""
                            content = ""
                            channelIndex = 0
                            prefs.clearPostDraft()
                            snackbar.showSnackbar("发布成功")
                            onCreated()
                        } else {
                            snackbar.showSnackbar(res.error ?: "发布失败，请确认已登录")
                        }
                    }
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (submitting) "发布中..." else "发布")
            }
        }
    }
}
