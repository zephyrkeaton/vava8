package com.vava8.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vava8.app.Vava8App
import com.vava8.app.ui.components.EmptyState
import com.vava8.app.ui.components.PostListItem
import com.vava8.app.ui.components.ScrollToTopFab
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.ui.theme.LocalReaderPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenPost: (Long) -> Unit,
    onSearch: () -> Unit,
    onOpenFavorites: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val readerPrefs = LocalReaderPrefs.current
    val showImages = !readerPrefs.noImageMode
    val scope = rememberCoroutineScope()
    val compactHeader by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 80
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, state.posts.size, state.hasMore, state.loadingMore) {
        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (last >= state.posts.size - 3 && state.hasMore && !state.loadingMore && state.posts.isNotEmpty()) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HomeTopBar(
                compact = compactHeader,
                channelName = state.channelName,
                showImages = showImages,
                onSearch = onSearch,
                onOpenFavorites = onOpenFavorites,
                onToggleImages = {
                    scope.launch {
                        Vava8App.instance.preferences.setNoImageMode(showImages)
                    }
                },
                onBack = onBack ?: if (state.channelId > 0) viewModel::clearChannel else null
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 4.dp)
            ) {
                items(state.tabs, key = { it.key }) { tab ->
                    val selected = state.selectedTab == tab.key && state.channelId == 0
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectTab(tab.key) },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBlue.copy(alpha = 0.12f),
                            selectedLabelColor = BrandBlue
                        ),
                        border = null,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            val pullState = rememberPullRefreshState(
                refreshing = state.refreshing,
                onRefresh = viewModel::refresh
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullState)
            ) {
                when {
                    state.loading && state.posts.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    }
                    state.error != null && state.posts.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EmptyState(state.error ?: "加载失败")
                            TextButton(onClick = viewModel::refresh) { Text("重试") }
                        }
                    }
                    state.posts.isEmpty() -> EmptyState("暂无内容")
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.posts, key = { it.id }) { post ->
                                PostListItem(
                                    post = post,
                                    showImage = showImages,
                                    onClick = { onOpenPost(post.id) }
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.loadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else if (!state.hasMore) {
                                        Text(
                                            "已经到底了",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                        ScrollToTopFab(listState = listState)
                    }
                }
                PullRefreshIndicator(
                    refreshing = state.refreshing,
                    state = pullState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    compact: Boolean,
    channelName: String,
    showImages: Boolean,
    onSearch: () -> Unit,
    onOpenFavorites: () -> Unit,
    onToggleImages: () -> Unit,
    onBack: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        AnimatedVisibility(
            visible = !compact,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = BrandBlue
                        )
                    }
                }
                Text(
                    text = "Vava8",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (channelName.isNotBlank()) channelName else "发吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenFavorites) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "我的收藏",
                        tint = BrandBlue
                    )
                }
                IconButton(onClick = onToggleImages) {
                    Icon(
                        imageVector = if (showImages) Icons.Outlined.Image else Icons.Outlined.ImageNotSupported,
                        contentDescription = if (showImages) "关闭无图模式" else "开启无图模式",
                        tint = BrandBlue
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSearch)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索资讯、帖子、频道...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
