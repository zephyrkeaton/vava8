package com.vava8.app.ui.discover

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vava8.app.data.model.Channel
import com.vava8.app.data.model.PostItem
import com.vava8.app.data.model.SiteChannels
import com.vava8.app.ui.components.ScrollToTopFab
import com.vava8.app.ui.components.SectionHeader
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.util.TimeUtils

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onOpenChannel: (Int, String) -> Unit,
    onOpenPost: (Long) -> Unit,
    onAllChannels: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    // 保存到 NavBackStackEntry：从详情右滑/返回键回来时恢复到原先标题位置
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    Scaffold(bottomBar = bottomBar) { padding ->
        when {
            ui.loading && !ui.loaded -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(key = "header") {
                            Text(
                                text = "发现",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        item(key = "channels-header") {
                            SectionHeader("热门频道", "全部") { onAllChannels() }
                        }
                        item(key = "channels-grid") {
                            ChannelGrid(
                                channels = SiteChannels.all.take(9),
                                onOpenChannel = onOpenChannel
                            )
                        }
                        item(key = "original-header") { SectionHeader("热门原创") }
                        items(ui.hotOriginal, key = { "o-${it.id}" }) { post ->
                            DiscoverRow(post) { onOpenPost(post.id) }
                        }
                        item(key = "hot-header") { SectionHeader("最热资讯") }
                        items(ui.hotPosts, key = { "h-${it.id}" }) { post ->
                            DiscoverRow(post) { onOpenPost(post.id) }
                        }
                        item(key = "reply-header") { SectionHeader("热评精选") }
                        items(ui.hotReply, key = { "r-${it.id}" }) { post ->
                            DiscoverRow(post) { onOpenPost(post.id) }
                        }
                        item(key = "footer") { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                    ScrollToTopFab(listState = listState)
                }
            }
        }
    }
}

@Composable
private fun ChannelGrid(
    channels: List<Channel>,
    onOpenChannel: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        channels.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { ch ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onOpenChannel(ch.id, ch.name) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ch.name.take(1),
                            color = BrandBlue,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ch.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DiscoverRow(post: PostItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${post.channelName} · ${TimeUtils.relative(post.publishedTs)} · ${post.comments} 评",
            style = MaterialTheme.typography.labelMedium
        )
    }
}
