package com.vava8.app.data.api

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

/**
 * 持久化 Cookie，保证杀进程 / 覆盖安装后仍能带上站点会话。
 * 使用 SharedPreferences#commit 同步落盘，避免 DataStore 异步写入未完成就退出。
 */
class PersistentCookieJar(private val context: Context) : CookieJar {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memory = mutableMapOf<String, MutableList<Cookie>>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        cookies.forEach { cookie ->
            val key = storageKey(cookie)
            val list = memory.getOrPut(key) { mutableListOf() }
            list.removeAll { it.name == cookie.name && it.path == cookie.path }
            // 会话 Cookie（无 Expires）在 OkHttp 里 expiresAt 为远未来；一并持久化以便下次启动仍登录
            if (cookie.expiresAt > now) {
                list.add(cookie)
            }
        }
        persistLocked()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val matched = mutableListOf<Cookie>()
        val emptyKeys = mutableListOf<String>()
        memory.forEach { (key, list) ->
            list.removeAll { it.expiresAt < now }
            if (list.isEmpty()) emptyKeys += key
            list.forEach { cookie ->
                if (cookie.matches(url)) matched += cookie
            }
        }
        emptyKeys.forEach { memory.remove(it) }
        return matched
    }

    @Synchronized
    fun clear() {
        memory.clear()
        persistLocked()
    }

    /** 是否持有除匿名探测 Cookie 外的站点 Cookie（用于判断可能仍登录）。 */
    @Synchronized
    fun hasAuthCookies(): Boolean {
        val now = System.currentTimeMillis()
        return memory.values.flatten().any { cookie ->
            cookie.expiresAt > now && cookie.name !in ANONYMOUS_COOKIES
        }
    }

    @Synchronized
    fun snapshot(): List<Cookie> = memory.values.flatten().toList()

    private fun loadFromDisk() {
        val raw = prefs.getString(KEY_COOKIES, "").orEmpty()
        if (raw.isBlank()) return
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                try {
                    val o = arr.getJSONObject(i)
                    val domain = o.getString("domain")
                    val hostOnly = o.optBoolean("hostOnly", false)
                    val expiresAt = o.optLong("expiresAt", -1L)
                    if (expiresAt > 0L && expiresAt < System.currentTimeMillis()) continue

                    val builder = Cookie.Builder()
                        .name(o.getString("name"))
                        .value(o.getString("value"))
                        .path(o.optString("path", "/"))
                    if (hostOnly) {
                        builder.hostOnlyDomain(domain)
                    } else {
                        builder.domain(domain)
                    }
                    if (o.optBoolean("secure")) builder.secure()
                    if (o.optBoolean("httpOnly")) builder.httpOnly()
                    if (expiresAt > 0L) builder.expiresAt(expiresAt)

                    val cookie = builder.build()
                    memory.getOrPut(storageKey(cookie)) { mutableListOf() }.add(cookie)
                } catch (_: Exception) {
                    // 单条损坏不影响其余 Cookie 恢复
                }
            }
        }
    }

    private fun persistLocked() {
        val arr = JSONArray()
        memory.values.flatten().forEach { c ->
            arr.put(
                JSONObject()
                    .put("name", c.name)
                    .put("value", c.value)
                    .put("domain", c.domain)
                    .put("path", c.path)
                    .put("secure", c.secure)
                    .put("httpOnly", c.httpOnly)
                    .put("hostOnly", c.hostOnly)
                    .put("expiresAt", c.expiresAt)
            )
        }
        // commit：同步写入，避免进程被杀时 apply 尚未落盘
        prefs.edit().putString(KEY_COOKIES, arr.toString()).commit()
    }

    private fun storageKey(cookie: Cookie): String =
        "${if (cookie.hostOnly) "h:" else "d:"}${cookie.domain}"

    companion object {
        private const val PREFS_NAME = "vava8_cookies"
        private const val KEY_COOKIES = "cookies_json"
        private val ANONYMOUS_COOKIES = setOf("country")
    }
}
