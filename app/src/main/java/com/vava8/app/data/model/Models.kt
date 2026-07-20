package com.vava8.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiListResponse(
    val ok: Int = 0,
    val limit: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    val sort: String = "latest",
    val data: List<PostItem> = emptyList(),
    val next: CursorNext? = null,
    val error: String? = null
)

@Serializable
data class CursorNext(
    @SerialName("cursor_time") val cursorTime: Long = 0,
    @SerialName("cursor_ts") val cursorTs: Long = 0,
    @SerialName("cursor_id") val cursorId: Long = 0,
    @SerialName("cursor_sort") val cursorSort: Long = 0,
    @SerialName("cursor_views") val cursorViews: Long = 0,
    @SerialName("cursor_comments") val cursorComments: Long = 0,
    @SerialName("has_more") val hasMore: Int = 0
)

@Serializable
data class PostItem(
    val id: Long,
    @SerialName("source_id") val sourceId: Long = 0,
    val uid: Long = 0,
    val title: String = "",
    val image: String? = null,
    @SerialName("index_sort") val indexSort: Long = 0,
    val author: String = "",
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("published_ts") val publishedTs: Long = 0,
    val likes: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    val comments: Int = 0,
    val shares: Int = 0,
    val views: String = "",
    val snippet: String = ""
)

@Serializable
data class ApiViewResponse(
    val ok: Int = 0,
    val data: PostDetail? = null,
    val error: String? = null
)

@Serializable
data class PostDetail(
    val id: Long,
    val title: String = "",
    val image: String? = null,
    val author: String = "",
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("published_ts") val publishedTs: Long = 0,
    val body: String = "",
    @SerialName("source_url") val sourceUrl: String = "",
    val likes: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    val comments: Int = 0,
    val shares: Int = 0,
    val views: String = "",
    val uid: Long = 0
)

@Serializable
data class ApiCommentsResponse(
    val ok: Int = 0,
    val mode: String = "tree",
    @SerialName("content_type") val contentType: String = "post",
    val data: List<CommentItem> = emptyList(),
    val error: String? = null
)

@Serializable
data class CommentItem(
    val id: Long,
    @SerialName("parent_comment_id") val parentCommentId: Long? = null,
    @SerialName("author_uid") val authorUid: Long = 0,
    val author: String = "",
    val avatar: String? = null,
    val body: String = "",
    @SerialName("published_ts") val publishedTs: Long = 0,
    val likes: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    val comments: Int = 0,
    val children: List<CommentItem> = emptyList()
)

@Serializable
data class SimpleOkResponse(
    val ok: Int = 0,
    val error: String? = null,
    @SerialName("likes_count") val likesCount: Int? = null,
    @SerialName("is_followed") val isFollowed: Int? = null,
    @SerialName("is_favorited") val isFavorited: Int? = null
)

data class Channel(
    val id: Int,
    val name: String
)

data class FeedTab(
    val key: String,
    val title: String,
    val sort: String = "latest",
    val contentType: String = "",
    val act: String = ""
)

data class SearchResult(
    val id: Long,
    val title: String,
    val channelName: String,
    val snippet: String,
    val author: String = "",
    val time: String = ""
)

data class SessionUser(
    val username: String,
    val uid: Long = 0,
    val isLoggedIn: Boolean = false
)

@Serializable
data class BrowseHistoryItem(
    val id: Long,
    val title: String = "",
    val image: String? = null,
    val author: String = "",
    @SerialName("channel_name") val channelName: String = "",
    /** 最近浏览时间（毫秒） */
    @SerialName("viewed_at") val viewedAt: Long = 0L
)

object SiteChannels {
    val all = listOf(
        Channel(45, "奇珍异宝"),
        Channel(50, "快乐嗨吧"),
        Channel(6, "体育"),
        Channel(11, "户外与旅游"),
        Channel(16, "搞笑"),
        Channel(18, "宠物情缘"),
        Channel(20, "文化艺术"),
        Channel(21, "摄影与自拍"),
        Channel(29, "快乐美食"),
        Channel(32, "发吧网事"),
        Channel(34, "精彩人生"),
        Channel(35, "网络文坛"),
        Channel(36, "音艺摄苑"),
        Channel(37, "哲学世界"),
        Channel(41, "闲聊三国"),
        Channel(42, "江南同乡"),
        Channel(43, "生活百态"),
        Channel(44, "卡通漫画"),
        Channel(46, "自由文学"),
        Channel(47, "八闽大地"),
        Channel(48, "自卫家防"),
        Channel(55, "婚姻家庭"),
        Channel(56, "新❀华漫"),
        Channel(57, "现代科学"),
        Channel(58, "汉学沙龙"),
        Channel(59, "网友荐歌"),
        Channel(63, "香茗一席"),
        Channel(65, "老友坊"),
        Channel(66, "我与美国"),
        Channel(70, "谈股论金"),
        Channel(72, "江湖儿女"),
        Channel(73, "我的中国"),
        Channel(74, "留园往事"),
        Channel(1, "商业与金融"),
        Channel(2, "娱乐"),
        Channel(3, "汽车与交通"),
        Channel(76, "在线视频"),
        Channel(75, "情感世界"),
        Channel(69, "半百谈"),
        Channel(25, "欧洲同城"),
        Channel(23, "美国同城"),
        Channel(27, "亚洲同城")
    )
}

object FeedTabs {
    val home = listOf(
        FeedTab("home", "推荐"),
        FeedTab("news", "资讯", contentType = "news"),
        FeedTab("hot", "最热", sort = "hot"),
        FeedTab("hotreply", "热评", sort = "hotreply"),
        FeedTab("original", "原创", contentType = "original"),
        FeedTab("following", "关注", act = "following")
    )
}
