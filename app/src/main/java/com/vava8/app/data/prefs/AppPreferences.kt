package com.vava8.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vava8.app.data.model.BrowseHistoryItem
import com.vava8.app.ui.theme.ReadingFontSize
import com.vava8.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.appPrefsStore by preferencesDataStore("vava8_app_prefs")

data class SavedLogin(
    val remember: Boolean,
    val username: String,
    val password: String
)

data class PersistedSession(
    val isLoggedIn: Boolean,
    val username: String
)

data class PostDraft(
    val title: String = "",
    val content: String = "",
    val channelId: Int = 0
) {
    val hasContent: Boolean = title.isNotBlank() || content.isNotBlank()
}

class AppPreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val noImageKey = booleanPreferencesKey("no_image_mode")
    private val fontKey = stringPreferencesKey("font_size")
    private val rememberLoginKey = booleanPreferencesKey("remember_login")
    private val savedUsernameKey = stringPreferencesKey("saved_username")
    private val savedPasswordKey = stringPreferencesKey("saved_password")

    // 会话标记用 SharedPreferences 同步落盘，避免杀进程丢登录态
    private val sessionPrefs =
        context.getSharedPreferences("vava8_session", Context.MODE_PRIVATE)
    // 发帖草稿同步落盘，退出 App 后仍可恢复
    private val draftPrefs =
        context.getSharedPreferences("vava8_post_draft", Context.MODE_PRIVATE)
    // UI 偏好（如返回顶部按钮位置）
    private val uiPrefs =
        context.getSharedPreferences("vava8_ui_prefs", Context.MODE_PRIVATE)
    // 本地浏览历史
    private val historyPrefs =
        context.getSharedPreferences("vava8_browse_history", Context.MODE_PRIVATE)
    private val historyJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val themeMode: Flow<ThemeMode> = context.appPrefsStore.data.map { prefs ->
        when (prefs[themeKey]) {
            ThemeMode.Light.name -> ThemeMode.Light
            ThemeMode.Dark.name -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    val noImageMode: Flow<Boolean> = context.appPrefsStore.data.map { prefs ->
        prefs[noImageKey] ?: false
    }

    val fontSize: Flow<ReadingFontSize> = context.appPrefsStore.data.map { prefs ->
        runCatching { ReadingFontSize.valueOf(prefs[fontKey] ?: ReadingFontSize.Medium.name) }
            .getOrDefault(ReadingFontSize.Medium)
    }

    val rememberLogin: Flow<Boolean> = context.appPrefsStore.data.map { prefs ->
        prefs[rememberLoginKey] ?: false
    }

    val savedLogin: Flow<SavedLogin> = context.appPrefsStore.data.map { prefs ->
        SavedLogin(
            remember = prefs[rememberLoginKey] ?: false,
            username = prefs[savedUsernameKey].orEmpty(),
            password = prefs[savedPasswordKey].orEmpty()
        )
    }

    fun loadPersistedSession(): PersistedSession {
        // 优先读同步 session prefs；兼容旧 DataStore 字段
        val spLoggedIn = sessionPrefs.getBoolean(KEY_SP_LOGGED_IN, false)
        val spUser = sessionPrefs.getString(KEY_SP_USERNAME, "").orEmpty()
        if (spLoggedIn || spUser.isNotBlank()) {
            return PersistedSession(isLoggedIn = spLoggedIn, username = spUser)
        }
        return PersistedSession(isLoggedIn = false, username = "")
    }

    fun savePersistedSession(username: String, isLoggedIn: Boolean) {
        sessionPrefs.edit()
            .putBoolean(KEY_SP_LOGGED_IN, isLoggedIn)
            .putString(KEY_SP_USERNAME, if (isLoggedIn) username else "")
            .commit()
    }

    fun clearPersistedSession() {
        sessionPrefs.edit()
            .putBoolean(KEY_SP_LOGGED_IN, false)
            .putString(KEY_SP_USERNAME, "")
            .commit()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appPrefsStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setNoImageMode(enabled: Boolean) {
        context.appPrefsStore.edit { it[noImageKey] = enabled }
    }

    suspend fun setFontSize(size: ReadingFontSize) {
        context.appPrefsStore.edit { it[fontKey] = size.name }
    }

    suspend fun setRememberLogin(enabled: Boolean) {
        context.appPrefsStore.edit { prefs ->
            prefs[rememberLoginKey] = enabled
            if (!enabled) {
                prefs.remove(savedUsernameKey)
                prefs.remove(savedPasswordKey)
            }
        }
    }

    suspend fun saveLoginCredentials(username: String, password: String) {
        context.appPrefsStore.edit { prefs ->
            prefs[rememberLoginKey] = true
            prefs[savedUsernameKey] = username
            prefs[savedPasswordKey] = password
        }
    }

    suspend fun clearLoginCredentials() {
        context.appPrefsStore.edit { prefs ->
            prefs[rememberLoginKey] = false
            prefs.remove(savedUsernameKey)
            prefs.remove(savedPasswordKey)
        }
    }

    suspend fun loadSavedLogin(): SavedLogin = savedLogin.first()

    fun loadPostDraft(): PostDraft = PostDraft(
        title = draftPrefs.getString(KEY_DRAFT_TITLE, "").orEmpty(),
        content = draftPrefs.getString(KEY_DRAFT_CONTENT, "").orEmpty(),
        channelId = draftPrefs.getInt(KEY_DRAFT_CHANNEL_ID, 0)
    )

    fun savePostDraft(draft: PostDraft) {
        draftPrefs.edit()
            .putString(KEY_DRAFT_TITLE, draft.title)
            .putString(KEY_DRAFT_CONTENT, draft.content)
            .putInt(KEY_DRAFT_CHANNEL_ID, draft.channelId)
            .commit()
    }

    fun clearPostDraft() {
        draftPrefs.edit()
            .remove(KEY_DRAFT_TITLE)
            .remove(KEY_DRAFT_CONTENT)
            .remove(KEY_DRAFT_CHANNEL_ID)
            .commit()
    }

    fun loadScrollToTopOffset(): Float =
        uiPrefs.getFloat(KEY_SCROLL_TOP_OFFSET, 0f)

    fun saveScrollToTopOffset(offsetY: Float) {
        uiPrefs.edit().putFloat(KEY_SCROLL_TOP_OFFSET, offsetY).commit()
    }

    fun loadBrowseHistory(): List<BrowseHistoryItem> {
        val raw = historyPrefs.getString(KEY_BROWSE_HISTORY, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            historyJson.decodeFromString<List<BrowseHistoryItem>>(raw)
        }.getOrDefault(emptyList())
    }

    fun recordBrowseHistory(item: BrowseHistoryItem) {
        val now = if (item.viewedAt > 0L) item.viewedAt else System.currentTimeMillis()
        val updated = buildList {
            add(item.copy(viewedAt = now))
            loadBrowseHistory().forEach { existing ->
                if (existing.id != item.id) add(existing)
            }
        }.take(MAX_BROWSE_HISTORY)
        historyPrefs.edit()
            .putString(KEY_BROWSE_HISTORY, historyJson.encodeToString(updated))
            .commit()
    }

    fun clearBrowseHistory() {
        historyPrefs.edit().remove(KEY_BROWSE_HISTORY).commit()
    }

    companion object {
        private const val KEY_SP_LOGGED_IN = "logged_in"
        private const val KEY_SP_USERNAME = "username"
        private const val KEY_DRAFT_TITLE = "title"
        private const val KEY_DRAFT_CONTENT = "content"
        private const val KEY_DRAFT_CHANNEL_ID = "channel_id"
        private const val KEY_SCROLL_TOP_OFFSET = "scroll_top_offset_y"
        private const val KEY_BROWSE_HISTORY = "items_json"
        private const val MAX_BROWSE_HISTORY = 100
    }
}
