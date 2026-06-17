package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.domain.GetBookmarkedRepositoriesUseCase
import com.example.core.domain.GetTrendingRepositoriesUseCase
import com.example.core.domain.GithubRepository
import com.example.core.domain.ToggleBookmarkUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTrendingRepositoriesUseCase: GetTrendingRepositoriesUseCase,
    private val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("trending_today")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _trendingState = MutableStateFlow<UiState<List<GithubRepository>>>(UiState.Loading)
    val trendingState: StateFlow<UiState<List<GithubRepository>>> = _trendingState.asStateFlow()

    // Observe bookmarked list reactively
    val bookmarkedState: StateFlow<List<GithubRepository>> = getBookmarkedRepositoriesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentlyViewedState = MutableStateFlow<List<GithubRepository>>(emptyList())

    init {
        loadTrending()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        loadTrending()
    }

    fun loadTrending(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _trendingState.value = UiState.Loading
            when (val res = getTrendingRepositoriesUseCase(_selectedCategory.value, forceRefresh)) {
                is Resource.Success -> {
                    _trendingState.value = UiState.Success(res.data, isOffline = false, lastUpdated = res.lastUpdated)
                }
                is Resource.SuccessOffline -> {
                    _trendingState.value = UiState.Success(res.data, isOffline = true, lastUpdated = res.lastUpdated)
                }
                is Resource.Error -> {
                    _trendingState.value = UiState.Error(res.message, res.code, res.throwable)
                }
            }
        }
    }

    fun toggleBookmark(repo: GithubRepository) {
        viewModelScope.launch {
            toggleBookmarkUseCase(repo)
        }
    }

    fun addRecentlyViewed(repo: GithubRepository) {
        val current = recentlyViewedState.value.toMutableList()
        current.removeAll { it.id == repo.id }
        current.add(0, repo)
        recentlyViewedState.value = current.take(6) // Only keep top 6
    }

    class Factory(
        private val getTrendingRepositoriesUseCase: GetTrendingRepositoriesUseCase,
        private val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase,
        private val toggleBookmarkUseCase: ToggleBookmarkUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                getTrendingRepositoriesUseCase,
                getBookmarkedRepositoriesUseCase,
                toggleBookmarkUseCase
            ) as T
        }
    }
}
