package com.vava8.app.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.vava8.app.Vava8App
import com.vava8.app.data.model.BrowseHistoryItem
import com.vava8.app.data.model.CommentItem
import com.vava8.app.data.model.PostDetail
import com.vava8.app.ui.components.SwipeBackContainer
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.ui.theme.LocalReaderPrefs
import com.vava8.app.ui.theme.ReadingFontSize
import com.vava8.app.ui.theme.ThemeMode
import com.vava8.app.util.TimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Long,
    onBack: () -> Unit,
    onLoginRequired: () -> Unit,
    onOpenChannel: (Int, String) -> Unit,
    onFontSizeChange: (ReadingFontSize) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val repo = Vava8App.instance.repository
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val readerPrefs = LocalReaderPrefs.current

    var loading by remember { mutableStateOf(true) }
    var detail by remember { mutableStateOf<PostDetail?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var favorited by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    fun reload() {
        scope.launch {
            loading = true
            runCatching {
                val view = repo.loadPost(articleId)
                if (view.ok != 1 || view.data == null) error(view.error ?: "加载失败")
                detail = view.data
                liked = view.data.isLiked
                likeCount = view.data.likes
                Vava8App.instance.preferences.recordBrowseHistory(
                    BrowseHistoryItem(
                        id = view.data.id,
                        title = view.data.title,
                        image = view.data.image,
                        author = view.data.author,
                        channelName = view.data.channelName,
                        viewedAt = System.currentTimeMillis()
                    )
                )
                val c = repo.loadComments(articleId)
                comments = buildCommentTree(c.data)
            }.onFailure {
                snackbar.showSnackbar(it.message ?: "加载失败")
            }
            loading = false
        }
    }

    LaunchedEffect(articleId) { reload() }

    SwipeBackContainer(enabled = !showSettings, onBack = onBack) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.channelName ?: "正文", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    // 键盘弹出时抬起留言栏，避免被虚拟键盘遮挡
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("写下你的看法...") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (!repo.user.value.isLoggedIn) {
                            onLoginRequired(); return@TextButton
                        }
                        if (commentText.isBlank()) return@TextButton
                        scope.launch {
                            val res = repo.comment(articleId, commentText.trim())
                            if (res.ok == 1) {
                                commentText = ""
                                snackbar.showSnackbar("评论成功")
                                reload()
                            } else {
                                snackbar.showSnackbar(res.error ?: "评论失败")
                            }
                        }
                    }) { Text("发送") }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionBtn(
                        icon = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        label = likeCount.toString(),
                        tinted = liked
                    ) {
                        if (!repo.user.value.isLoggedIn) {
                            onLoginRequired(); return@ActionBtn
                        }
                        val next = !liked
                        liked = next
                        likeCount += if (next) 1 else -1
                        scope.launch {
                            val res = repo.like("article", articleId, next)
                            if (res.ok != 1) {
                                liked = !next
                                likeCount += if (next) -1 else 1
                                snackbar.showSnackbar(res.error ?: "操作失败")
                            } else {
                                likeCount = res.likesCount ?: likeCount
                            }
                        }
                    }
                    ActionBtn(Icons.Outlined.ChatBubbleOutline, "${detail?.comments ?: 0}") {}
                    ActionBtn(
                        if (favorited) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        if (favorited) "已藏" else "收藏",
                        tinted = favorited
                    ) {
                        if (!repo.user.value.isLoggedIn) {
                            onLoginRequired(); return@ActionBtn
                        }
                        scope.launch {
                            val res = repo.favorite(articleId)
                            if (res.ok == 1) {
                                favorited = (res.isFavorited ?: if (favorited) 0 else 1) == 1
                            } else snackbar.showSnackbar(res.error ?: "收藏失败")
                        }
                    }
                    ActionBtn(Icons.Filled.Share, "分享") {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${detail?.title.orEmpty()}\nhttps://www.vava8.com/index.php?app=index&act=view&id=$articleId"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "分享到"))
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (loading && detail == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BrandBlue) }
        } else {
            val d = detail
            if (d == null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("内容不存在") }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = d.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = d.channelName,
                                    color = BrandBlue,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrandBlue.copy(0.1f))
                                        .clickableChannel(d.channelId, d.channelName, onOpenChannel)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${d.author} · ${TimeUtils.relative(d.publishedTs)} · ${d.views}阅读",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (d.uid > 0) {
                                    TextButton(onClick = {
                                        if (!repo.user.value.isLoggedIn) {
                                            onLoginRequired(); return@TextButton
                                        }
                                        scope.launch {
                                            val res = repo.followUser(d.uid, true)
                                            snackbar.showSnackbar(
                                                if (res.ok == 1) "已关注" else (res.error ?: "失败")
                                            )
                                        }
                                    }) {
                                        Icon(Icons.Outlined.PersonAdd, null, Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("关注")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        ArticleWebBody(
                            html = d.body,
                            dark = MaterialTheme.colorScheme.background.luminance() < 0.5f,
                            fontScale = when (readerPrefs.fontSize) {
                                ReadingFontSize.Small -> 15
                                ReadingFontSize.Medium -> 17
                                ReadingFontSize.Large -> 19
                                ReadingFontSize.ExtraLarge -> 21
                            },
                            noImage = readerPrefs.noImageMode
                        )
                    }
                    item {
                        Text(
                            text = "评论 ${d.comments}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "暂无评论，来抢沙发吧",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(comments, key = { it.id }) { c ->
                            CommentBlock(c)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("阅读设置", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text("字体大小", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReadingFontSize.entries.forEach { size ->
                        FilterChip(
                            selected = readerPrefs.fontSize == size,
                            onClick = { onFontSizeChange(size) },
                            label = {
                                Text(
                                    when (size) {
                                        ReadingFontSize.Small -> "小"
                                        ReadingFontSize.Medium -> "中"
                                        ReadingFontSize.Large -> "大"
                                        ReadingFontSize.ExtraLarge -> "特大"
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("主题外观", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = readerPrefs.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label()) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    } // SwipeBackContainer
}

private fun Modifier.clickableChannel(
    id: Int,
    name: String,
    onOpen: (Int, String) -> Unit
): Modifier = this.clickable { onOpen(id, name) }

@Composable
private fun ActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tinted: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(
            icon,
            null,
            tint = if (tinted) BrandBlue else MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun CommentBlock(comment: CommentItem, depth: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 + depth * 16).dp, end = 16.dp, top = 10.dp, bottom = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val noImage = LocalReaderPrefs.current.noImageMode
            if (!noImage) {
                AsyncImage(
                    model = comment.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(comment.author, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(TimeUtils.relative(comment.publishedTs), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.body.replace(Regex("<[^>]+>"), "").trim(),
            style = MaterialTheme.typography.bodyMedium
        )
        comment.children.forEach { child ->
            CommentBlock(child, depth + 1)
        }
    }
}

private fun buildCommentTree(flat: List<CommentItem>): List<CommentItem> {
    val map = linkedMapOf<Long, CommentItem>()
    flat.forEach { map[it.id] = it.copy(children = emptyList()) }
    val roots = mutableListOf<CommentItem>()
    flat.forEach { raw ->
        val parentId = raw.parentCommentId
        val node = map[raw.id] ?: return@forEach
        if (parentId != null && parentId > 0 && map.containsKey(parentId)) {
            val parent = map[parentId]!!
            map[parentId] = parent.copy(children = parent.children + node)
        } else {
            roots += node
        }
    }
    // Rebuild from map so nested children are latest
    fun resolve(id: Long): CommentItem {
        val n = map[id]!!
        return n.copy(children = n.children.map { resolve(it.id) })
    }
    return roots.map { resolve(it.id) }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ArticleWebBody(html: String, dark: Boolean, fontScale: Int, noImage: Boolean) {
    val bg = if (dark) "#1A222C" else "#FFFFFF"
    val fg = if (dark) "#E8EEF5" else "#1A1F2C"
    var webHeightDp by remember { mutableIntStateOf(480) }
    val imageCss = if (noImage) "img{display:none !important;}" else "img{max-width:100%;height:auto;border-radius:4px;margin:8px 0;}"
    val wrapped = """
        <!DOCTYPE html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
        <style>
          body{margin:0;padding:16px;background:$bg;color:$fg;font-size:${fontScale}px;line-height:1.75;
          font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Noto Sans SC",sans-serif;}
          $imageCss
          a{color:#1E88E5;text-decoration:none;}
          p,span,div{word-break:break-word;}
        </style></head><body>$html</body></html>
    """.trimIndent()
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript(
                            "(function(){return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);})();"
                        ) { value ->
                            val h = value?.replace("\"", "")?.toFloatOrNull() ?: 480f
                            webHeightDp = h.toInt().coerceIn(200, 12000)
                        }
                    }
                }
                settings.javaScriptEnabled = true
                settings.loadsImagesAutomatically = !noImage
                settings.blockNetworkImage = noImage
                // 正文由外层 LazyColumn 纵向滚动；减少横向抢手势，配合左缘右滑返回
                isHorizontalScrollBarEnabled = false
                overScrollMode = android.view.View.OVER_SCROLL_NEVER
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { wv ->
            wv.settings.loadsImagesAutomatically = !noImage
            wv.settings.blockNetworkImage = noImage
            wv.loadDataWithBaseURL("https://www.vava8.com/", wrapped, "text/html", "UTF-8", null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(webHeightDp.dp)
    )
}
