package com.example.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.domain.GetBookmarkedRepositoriesUseCase
import com.example.core.domain.GithubRepository
import com.example.core.domain.ToggleBookmarkUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter observed bookmarks list on user input
    val bookmarkedList: StateFlow<List<GithubRepository>> = combine(
        getBookmarkedRepositoriesUseCase(),
        _searchQuery
    ) { repos, query ->
        if (query.isBlank()) {
            repos
        } else {
            repos.filter {
                it.name.contains(query, ignoreCase = true) || 
                (it.description?.contains(query, ignoreCase = true) ?: false) ||
                it.ownerLogin.contains(query, ignoreCase = true)
            }
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(newQ: String) {
        _searchQuery.value = newQ
    }

    fun removeBookmark(repo: GithubRepository) {
        viewModelScope.launch {
            toggleBookmarkUseCase(repo)
        }
    }

    class Factory(
        private val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase,
        private val toggleBookmarkUseCase: ToggleBookmarkUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BookmarksViewModel(getBookmarkedRepositoriesUseCase, toggleBookmarkUseCase) as T
        }
    }
}
