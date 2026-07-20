package com.vava8.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vava8.app.Vava8App
import com.vava8.app.data.model.BrowseHistoryItem
import com.vava8.app.ui.components.EmptyState
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.ui.theme.InkSecondary
import com.vava8.app.ui.theme.LocalReaderPrefs
import com.vava8.app.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseHistoryScreen(
    onBack: () -> Unit,
    onOpenPost: (Long) -> Unit
) {
    val prefs = Vava8App.instance.preferences
    val readerPrefs = LocalReaderPrefs.current
    var items by remember { mutableStateOf(prefs.loadBrowseHistory()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("浏览历史") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                prefs.clearBrowseHistory()
                                items = emptyList()
                            }
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState("暂无浏览记录")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryRow(
                        item = item,
                        showImage = !readerPrefs.noImageMode,
                        onClick = { onOpenPost(item.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: BrowseHistoryItem,
    showImage: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.ifBlank { "无标题" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.channelName.isNotBlank()) {
                    Text(
                        text = item.channelName,
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandBlue
                    )
                }
                if (item.author.isNotBlank()) {
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = TimeUtils.relative(item.viewedAt / 1000),
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSecondary
                )
            }
        }
        if (showImage && !item.image.isNullOrBlank()) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
