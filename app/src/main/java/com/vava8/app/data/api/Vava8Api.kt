package com.vava8.app.data.api

import com.vava8.app.data.model.ApiCommentsResponse
import com.vava8.app.data.model.ApiListResponse
import com.vava8.app.data.model.ApiViewResponse
import com.vava8.app.data.model.SearchResult
import com.vava8.app.data.model.SimpleOkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class Vava8Api(
    private val client: OkHttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
) {
    companion object {
        const val BASE = "https://www.vava8.com/"
        const val SITE_NAME = "Vava8 发吧"
    }

    private fun apiUrl(act: String, params: Map<String, String> = emptyMap()): String {
        val builder = BASE.toHttpUrl().newBuilder()
            .addQueryParameter("app", "index")
            .addQueryParameter("act", act)
        params.forEach { (k, v) -> builder.addQueryParameter(k, v) }
        return builder.build().toString()
    }

    private fun usersUrl(act: String): String =
        "${BASE}index.php?app=users&act=$act"

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).get()
            .header("Accept", "application/json, text/html;q=0.9,*/*;q=0.8")
            .header("User-Agent", "Vava8Android/1.0")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
    }

    private suspend fun postForm(url: String, fields: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder().apply {
                fields.forEach { (k, v) -> add(k, v) }
            }.build()
            val req = Request.Builder().url(url).post(body)
                .header("Accept", "application/json")
                .header("User-Agent", "Vava8Android/1.0")
                .header("X-Requested-With", "XMLHttpRequest")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                resp.body?.string().orEmpty()
            }
        }

    suspend fun fetchList(
        limit: Int = 30,
        sort: String = "latest",
        contentType: String = "",
        channelId: Int = 0,
        userId: Long = 0,
        sourceId: Long = 0,
        tagId: Int = 0,
        listSort: Int = -1,
        cursorTime: Long = 0,
        cursorId: Long = 0,
        cursorSort: Long = 0,
        cursorViews: Long = 0,
        cursorComments: Long = 0,
        isIndex: Boolean = false
    ): ApiListResponse {
        val params = mutableMapOf(
            "limit" to limit.toString(),
            "sort" to sort
        )
        if (isIndex) params["isIndex"] = "1"
        if (contentType.isNotBlank()) params["content_type"] = contentType
        if (channelId > 0) {
            params["channel_id"] = channelId.toString()
            if (listSort == 0 || listSort == 1) params["list_sort"] = listSort.toString()
        }
        if (userId > 0) params["user_id"] = userId.toString()
        if (sourceId > 0) params["source_id"] = sourceId.toString()
        if (tagId > 0) params["tag_id"] = tagId.toString()
        if (cursorTime > 0 && cursorId > 0) {
            params["cursor_time"] = cursorTime.toString()
            params["cursor_id"] = cursorId.toString()
            when (sort) {
                "hot" -> params["cursor_views"] = cursorViews.toString()
                "hotreply" -> params["cursor_comments"] = cursorComments.toString()
                else -> params["cursor_sort"] = cursorSort.toString()
            }
        }
        val body = get(apiUrl("api_list", params))
        return json.decodeFromString(body)
    }

    suspend fun fetchView(id: Long): ApiViewResponse {
        val body = get(apiUrl("api_view", mapOf("id" to id.toString())))
        return json.decodeFromString(body)
    }

    suspend fun fetchComments(articleId: Long, limit: Int = 200): ApiCommentsResponse {
        val body = get(
            apiUrl(
                "api_comments",
                mapOf(
                    "article_id" to articleId.toString(),
                    "limit" to limit.toString(),
                    "scene" to "article"
                )
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun toggleLike(type: String, id: Long, liked: Boolean): SimpleOkResponse {
        val body = postForm(
            apiUrl("api_toggle_like"),
            mapOf(
                "type" to type,
                "id" to id.toString(),
                "liked" to if (liked) "1" else "0"
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun followUser(targetUid: Long, follow: Boolean): SimpleOkResponse {
        val body = postForm(
            apiUrl("api_follow_user"),
            mapOf(
                "target_uid" to targetUid.toString(),
                "action" to if (follow) "follow" else "unfollow"
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun followChannel(channelId: Int, follow: Boolean): SimpleOkResponse {
        val body = postForm(
            apiUrl("api_follow_channel"),
            mapOf(
                "channel_id" to channelId.toString(),
                "action" to if (follow) "follow" else "unfollow"
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun toggleFavorite(articleId: Long): SimpleOkResponse {
        val url = "${BASE}index.php?app=home&act=toggle_favorite"
        // 站点前端提交 content_type + content_id；传 id 会返回「无效的内容编号」
        val body = postForm(
            url,
            mapOf(
                "content_type" to "article",
                "content_id" to articleId.toString()
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun login(username: String, password: String): SimpleOkResponse {
        val body = postForm(
            usersUrl("do_login"),
            mapOf("username" to username, "password" to password)
        )
        return json.decodeFromString(body)
    }

    suspend fun logout(): SimpleOkResponse {
        val body = postForm(usersUrl("logout"), emptyMap())
        return runCatching { json.decodeFromString<SimpleOkResponse>(body) }
            .getOrElse { SimpleOkResponse(ok = 1) }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        password2: String
    ): SimpleOkResponse {
        val body = postForm(
            usersUrl("do_register"),
            mapOf(
                "username" to username,
                "email" to email,
                "password" to password,
                "password2" to password2
            )
        )
        return json.decodeFromString(body)
    }

    suspend fun submitComment(
        articleId: Long,
        content: String,
        parentCommentId: Long = 0
    ): SimpleOkResponse {
        // 站点 comment.js 提交字段名为 body（非 content），否则服务端判定「评论内容不能为空」
        val fields = mutableMapOf(
            "article_id" to articleId.toString(),
            "body" to content
        )
        if (parentCommentId > 0) fields["parent_comment_id"] = parentCommentId.toString()
        val body = postForm("${BASE}index.php?app=comment&act=do_submit", fields)
        return json.decodeFromString(body)
    }

    suspend fun createPost(
        title: String,
        content: String,
        channelId: Int,
        articleType: String = "1"
    ): SimpleOkResponse {
        // Site create form posts HTML body to act=create
        val htmlBody = content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .lines()
            .joinToString("<br>")
        val body = postForm(
            apiUrl("create"),
            mapOf(
                "title" to title,
                "body" to htmlBody,
                "channel_id" to channelId.toString(),
                "article_type" to articleType
            )
        )
        return runCatching { json.decodeFromString<SimpleOkResponse>(body) }
            .getOrElse {
                if (body.contains("\"ok\":1") || body.contains("成功")) SimpleOkResponse(ok = 1)
                else SimpleOkResponse(ok = 0, error = "发帖失败，请登录后重试")
            }
    }

    suspend fun search(query: String, type: String = "all"): List<SearchResult> {
        val url = BASE.toHttpUrl().newBuilder()
            .addQueryParameter("app", "index")
            .addQueryParameter("act", "search")
            .addQueryParameter("q", query)
            .addQueryParameter("t", type)
            .build()
            .toString()
        val html = get(url)
        return parseSearchHtml(html)
    }

    private fun parseSearchHtml(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val blockPattern = Pattern.compile(
            """class="post-title"[\s\S]*?channel_id=\d+">([^<]+)</a>]\s*<a href="[^"]*act=view(?:&amp;|&)id=(\d+)"[^>]*>([\s\S]*?)</a>[\s\S]*?(?:class="search-snippet"[^>]*>[\s\S]*?<a[^>]*>([\s\S]*?)</a>)?""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = blockPattern.matcher(html)
        while (matcher.find()) {
            val channel = matcher.group(1)?.trim().orEmpty()
            val id = matcher.group(2)?.toLongOrNull() ?: continue
            val title = stripHtml(matcher.group(3).orEmpty())
            val snippet = stripHtml(matcher.group(4).orEmpty())
            if (title.isNotBlank()) {
                results += SearchResult(
                    id = id,
                    title = title,
                    channelName = channel,
                    snippet = snippet
                )
            }
        }
        if (results.isEmpty()) {
            // Fallback: extract view ids + nearby title text
            val idPattern = Pattern.compile("""act=view(?:&amp;|&)id=(\d+)""")
            val idMatcher = idPattern.matcher(html)
            val seen = mutableSetOf<Long>()
            while (idMatcher.find()) {
                val id = idMatcher.group(1)?.toLongOrNull() ?: continue
                if (id == 19907L || !seen.add(id)) continue
                results += SearchResult(id = id, title = "帖子 #$id", channelName = "", snippet = "")
                if (results.size >= 40) break
            }
        }
        return results.distinctBy { it.id }
    }

    private fun stripHtml(raw: String): String =
        raw.replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
}

object ApiFactory {
    fun create(cookieJar: PersistentCookieJar): Vava8Api {
        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        return Vava8Api(client)
    }
}
