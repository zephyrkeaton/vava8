package com.vava8.app.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vava8.app.data.model.PostItem
import com.vava8.app.data.repository.Vava8Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val hotOriginal: List<PostItem> = emptyList(),
    val hotPosts: List<PostItem> = emptyList(),
    val hotReply: List<PostItem> = emptyList(),
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null
)

/**
 * 绑定到发现页 NavBackStackEntry：进入详情再返回时 ViewModel 仍在，
 * 列表数据不会被重新拉空，配合 LazyListState 可回到原先标题位置。
 */
class DiscoverViewModel(
    private val repo: Vava8Repository
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState(loading = true))
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(force: Boolean = false) {
        if (_state.value.loaded && !force) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val original = repo.loadFeed(contentType = "original", limit = 8).data
                val hot = repo.loadFeed(sort = "hot", limit = 8).data
                val reply = repo.loadFeed(sort = "hotreply", limit = 8).data
                _state.update {
                    it.copy(
                        hotOriginal = original,
                        hotPosts = hot,
                        hotReply = reply,
                        loading = false,
                        loaded = true,
                        error = null
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(loading = false, error = e.message ?: "加载失败")
                }
            }
        }
    }

    companion object {
        fun factory(repo: Vava8Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiscoverViewModel(repo) as T
                }
            }
    }
}
