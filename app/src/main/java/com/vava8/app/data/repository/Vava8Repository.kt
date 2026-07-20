package com.vava8.app.data.repository

import com.vava8.app.data.api.PersistentCookieJar
import com.vava8.app.data.api.Vava8Api
import com.vava8.app.data.model.ApiCommentsResponse
import com.vava8.app.data.model.ApiListResponse
import com.vava8.app.data.model.ApiViewResponse
import com.vava8.app.data.model.SearchResult
import com.vava8.app.data.model.SessionUser
import com.vava8.app.data.model.SimpleOkResponse
import com.vava8.app.data.prefs.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Vava8Repository(
    private val api: Vava8Api,
    private val cookieJar: PersistentCookieJar,
    private val preferences: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _user = MutableStateFlow(restoreSessionUser())
    val user: StateFlow<SessionUser> = _user.asStateFlow()

    init {
        // 冷启动：有保存的账号密码则静默续期，避免 Cookie/服务端会话失效后要手动再登
        scope.launch { refreshSessionIfNeeded() }
    }

    private fun restoreSessionUser(): SessionUser {
        val persisted = preferences.loadPersistedSession()
        val hasCookies = cookieJar.hasAuthCookies()
        return when {
            persisted.isLoggedIn && persisted.username.isNotBlank() ->
                SessionUser(username = persisted.username, isLoggedIn = true)
            hasCookies ->
                SessionUser(username = persisted.username, isLoggedIn = true)
            else ->
                SessionUser(username = "", isLoggedIn = false)
        }
    }

    private suspend fun refreshSessionIfNeeded() {
        val saved = preferences.loadSavedLogin()
        val persisted = preferences.loadPersistedSession()
        val shouldStayLoggedIn = persisted.isLoggedIn || _user.value.isLoggedIn

        // 已登录且勾选了保存密码：冷启动静默登录，刷新服务端会话 / 补回丢失的 Cookie
        if (shouldStayLoggedIn &&
            saved.remember &&
            saved.username.isNotBlank() &&
            saved.password.isNotBlank()
        ) {
            val res = runCatching {
                api.login(saved.username, saved.password)
            }.getOrNull()
            if (res?.ok == 1) {
                markLoggedIn(saved.username)
                return
            }
        }

        // 未保存密码且本地已无会话 Cookie：视为登录失效
        if (shouldStayLoggedIn && !cookieJar.hasAuthCookies() && !saved.remember) {
            markLoggedOutLocal()
        }
    }

    suspend fun loadFeed(
        sort: String = "latest",
        contentType: String = "",
        channelId: Int = 0,
        userId: Long = 0,
        cursorTime: Long = 0,
        cursorId: Long = 0,
        cursorSort: Long = 0,
        cursorViews: Long = 0,
        cursorComments: Long = 0,
        limit: Int = 30,
        isIndex: Boolean = channelId == 0 && contentType.isBlank() && sort == "latest"
    ): ApiListResponse = api.fetchList(
        limit = limit,
        sort = sort,
        contentType = contentType,
        channelId = channelId,
        userId = userId,
        cursorTime = cursorTime,
        cursorId = cursorId,
        cursorSort = cursorSort,
        cursorViews = cursorViews,
        cursorComments = cursorComments,
        isIndex = isIndex
    )

    suspend fun loadPost(id: Long): ApiViewResponse = api.fetchView(id)

    suspend fun loadComments(id: Long): ApiCommentsResponse = api.fetchComments(id)

    suspend fun like(type: String, id: Long, liked: Boolean): SimpleOkResponse =
        api.toggleLike(type, id, liked)

    suspend fun favorite(id: Long): SimpleOkResponse = api.toggleFavorite(id)

    suspend fun followUser(uid: Long, follow: Boolean): SimpleOkResponse =
        api.followUser(uid, follow)

    suspend fun followChannel(channelId: Int, follow: Boolean): SimpleOkResponse =
        api.followChannel(channelId, follow)

    suspend fun search(q: String): List<SearchResult> = api.search(q)

    suspend fun login(username: String, password: String): SimpleOkResponse {
        val res = api.login(username, password)
        if (res.ok == 1) {
            markLoggedIn(username)
        }
        return res
    }

    suspend fun logout(): SimpleOkResponse {
        val res = api.logout()
        cookieJar.clear()
        markLoggedOutLocal()
        return res
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        password2: String
    ): SimpleOkResponse = api.register(username, email, password, password2)

    suspend fun comment(articleId: Long, content: String, parentId: Long = 0): SimpleOkResponse =
        api.submitComment(articleId, content, parentId)

    suspend fun createPost(title: String, content: String, channelId: Int): SimpleOkResponse =
        api.createPost(title, content, channelId)

    fun markLoggedIn(username: String) {
        _user.value = SessionUser(username = username, isLoggedIn = true)
        preferences.savePersistedSession(username = username, isLoggedIn = true)
    }

    private fun markLoggedOutLocal() {
        _user.value = SessionUser(username = "", isLoggedIn = false)
        preferences.clearPersistedSession()
    }
}
