package com.example.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.domain.GithubRepository
import com.example.core.domain.SearchRepositoriesUseCase
import com.example.core.domain.ToggleBookmarkUseCase
import com.example.core.data.GitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepositoriesUseCase: SearchRepositoriesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val gitRepository: GitRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<GithubRepository>>?>(null)
    val searchState: StateFlow<UiState<List<GithubRepository>>?> = _searchState.asStateFlow()

    // Persistent Search history flow from DB
    val searchHistory: StateFlow<List<String>> = gitRepository.getSearchHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Advanced search filter states
    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _selectedTopic = MutableStateFlow<String?>(null)
    val selectedTopic: StateFlow<String?> = _selectedTopic.asStateFlow()

    private val _selectedOrg = MutableStateFlow<String?>(null)
    val selectedOrg: StateFlow<String?> = _selectedOrg.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun setLanguage(lang: String?) {
        _selectedLanguage.value = lang
    }

    fun setTopic(topic: String?) {
        _selectedTopic.value = topic
    }

    fun setOrg(org: String?) {
        _selectedOrg.value = org
    }

    fun performSearch() {
        val q = _query.value.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _searchState.value = UiState.Loading
            
            val result = searchRepositoriesUseCase(
                query = q,
                language = _selectedLanguage.value,
                topic = _selectedTopic.value,
                org = _selectedOrg.value
            )

            when (result) {
                is Resource.Success -> {
                    _searchState.value = UiState.Success(result.data)
                }
                is Resource.SuccessOffline -> {
                    _searchState.value = UiState.Success(result.data, isOffline = true)
                }
                is Resource.Error -> {
                    _searchState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun deleteQuery(query: String) {
        viewModelScope.launch {
            gitRepository.deleteSearchQuery(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            gitRepository.clearSearchHistory()
        }
    }

    fun selectHistoryQuery(selectedQ: String) {
        _query.value = selectedQ
        performSearch()
    }

    fun toggleBookmark(repo: GithubRepository) {
        viewModelScope.launch {
            toggleBookmarkUseCase(repo)
            // Trigger ui state update if search succeeds to sync UI
            val state = _searchState.value
            if (state is UiState.Success) {
                val updated = state.data.map {
                    if (it.id == repo.id) it.copy(isBookmarked = !it.isBookmarked) else it
                }
                _searchState.value = UiState.Success(updated, isOffline = state.isOffline, lastUpdated = state.lastUpdated)
            }
        }
    }

    class Factory(
        private val searchRepositoriesUseCase: SearchRepositoriesUseCase,
        private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
        private val gitRepository: GitRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(
                searchRepositoriesUseCase,
                toggleBookmarkUseCase,
                gitRepository
            ) as T
        }
    }
}
