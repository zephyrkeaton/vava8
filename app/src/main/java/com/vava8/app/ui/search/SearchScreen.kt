package com.vava8.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vava8.app.Vava8App
import com.vava8.app.data.model.SearchResult
import com.vava8.app.ui.theme.BrandBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenPost: (Long) -> Unit
) {
    val repo = Vava8App.instance.repository
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun doSearch() {
        if (query.isBlank()) return
        loading = true
        error = null
        scope.launch {
            runCatching { repo.search(query.trim()) }
                .onSuccess { results = it; if (it.isEmpty()) error = "没有找到相关内容" }
                .onFailure { error = it.message ?: "搜索失败" }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入关键词") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { doSearch() })
            )
            Spacer(modifier = Modifier.height(12.dp))
            when {
                loading -> {
                    CircularProgressIndicator(
                        color = BrandBlue,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(24.dp)
                    )
                }
                error != null && results.isEmpty() -> {
                    Text(error ?: "", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
                else -> {
                    LazyColumn {
                        items(results, key = { it.id }) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenPost(item.id) }
                                    .padding(vertical = 12.dp)
                            ) {
                                if (item.channelName.isNotBlank()) {
                                    Text(item.channelName, color = BrandBlue, style = MaterialTheme.typography.labelMedium)
                                }
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.snippet.isNotBlank()) {
                                    Text(
                                        item.snippet,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.65f)
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
