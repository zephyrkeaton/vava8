package com.vava8.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vava8.app.data.model.CursorNext
import com.vava8.app.data.model.FeedTab
import com.vava8.app.data.model.FeedTabs
import com.vava8.app.data.model.PostItem
import com.vava8.app.data.repository.Vava8Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val tabs: List<FeedTab> = FeedTabs.home,
    val selectedTab: String = "home",
    val posts: List<PostItem> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val channelId: Int = 0,
    val channelName: String = ""
)

class HomeViewModel(
    private val repo: Vava8Repository,
    initialChannelId: Int = 0,
    initialChannelName: String = ""
) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(channelId = initialChannelId, channelName = initialChannelName)
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var cursor: CursorNext? = null

    init {
        refresh()
    }

    fun selectTab(key: String) {
        if (_state.value.selectedTab == key) return
        _state.update { it.copy(selectedTab = key, channelId = 0, channelName = "") }
        refresh()
    }

    fun setChannel(id: Int, name: String) {
        _state.update { it.copy(channelId = id, channelName = name, selectedTab = "home") }
        refresh()
    }

    fun clearChannel() {
        _state.update { it.copy(channelId = 0, channelName = "") }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, error = null, loading = it.posts.isEmpty()) }
            cursor = null
            runCatching { fetch(first = true) }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            refreshing = false,
                            loading = false,
                            error = e.message ?: "加载失败"
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.loadingMore || s.refreshing || !s.hasMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            runCatching { fetch(first = false) }
                .onFailure {
                    _state.update { st -> st.copy(loadingMore = false) }
                }
        }
    }

    private suspend fun fetch(first: Boolean) {
        val s = _state.value
        val tab = s.tabs.find { it.key == s.selectedTab } ?: s.tabs.first()
        val res = repo.loadFeed(
            sort = tab.sort,
            contentType = tab.contentType,
            channelId = s.channelId,
            cursorTime = if (first) 0 else cursor?.cursorTime ?: 0,
            cursorId = if (first) 0 else cursor?.cursorId ?: 0,
            cursorSort = if (first) 0 else cursor?.cursorSort ?: 0,
            cursorViews = if (first) 0 else cursor?.cursorViews ?: 0,
            cursorComments = if (first) 0 else cursor?.cursorComments ?: 0,
            isIndex = s.channelId == 0 && tab.key == "home"
        )
        if (res.ok != 1) error(res.error ?: "接口错误")
        cursor = res.next
        val merged = if (first) res.data else {
            val exist = s.posts.map { it.id }.toSet()
            s.posts + res.data.filter { it.id !in exist }
        }
        _state.update {
            it.copy(
                posts = merged,
                loading = false,
                refreshing = false,
                loadingMore = false,
                hasMore = res.next?.hasMore == 1,
                error = null
            )
        }
    }

    companion object {
        fun factory(
            repo: Vava8Repository,
            channelId: Int = 0,
            channelName: String = ""
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repo, channelId, channelName) as T
                }
            }
    }
}
