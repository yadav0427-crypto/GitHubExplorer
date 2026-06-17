package com.example.feature.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.domain.GetIssuesUseCase
import com.example.core.domain.GetPullRequestsUseCase
import com.example.core.domain.GithubIssue
import com.example.core.domain.GithubPullRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IssuesViewModel(
    private val getIssuesUseCase: GetIssuesUseCase,
    private val getPullRequestsUseCase: GetPullRequestsUseCase
) : ViewModel() {

    private val _issuesState = MutableStateFlow<UiState<List<GithubIssue>>>(UiState.Loading)
    val issuesState: StateFlow<UiState<List<GithubIssue>>> = _issuesState.asStateFlow()

    private val _pullsState = MutableStateFlow<UiState<List<GithubPullRequest>>>(UiState.Loading)
    val pullsState: StateFlow<UiState<List<GithubPullRequest>>> = _pullsState.asStateFlow()

    private val _selectedStateFilter = MutableStateFlow("all") // open, closed, all
    val selectedStateFilter: StateFlow<String> = _selectedStateFilter.asStateFlow()

    private val _searchFilterQuery = MutableStateFlow("")
    val searchFilterQuery: StateFlow<String> = _searchFilterQuery.asStateFlow()

    fun loadIssuesAndPulls(owner: String, name: String) {
        viewModelScope.launch {
            _issuesState.value = UiState.Loading
            _pullsState.value = UiState.Loading

            // Load issues
            when (val issuesRes = getIssuesUseCase(owner, name, _selectedStateFilter.value)) {
                is Resource.Success -> {
                    _issuesState.value = UiState.Success(issuesRes.data)
                }
                is Resource.SuccessOffline -> {
                    _issuesState.value = UiState.Success(issuesRes.data, isOffline = true, lastUpdated = issuesRes.lastUpdated)
                }
                is Resource.Error -> {
                    _issuesState.value = UiState.Error(issuesRes.message)
                }
            }

            // Load pulls
            when (val pullsRes = getPullRequestsUseCase(owner, name, _selectedStateFilter.value)) {
                is Resource.Success -> {
                    _pullsState.value = UiState.Success(pullsRes.data)
                }
                is Resource.SuccessOffline -> {
                    _pullsState.value = UiState.Success(pullsRes.data, isOffline = true, lastUpdated = pullsRes.lastUpdated)
                }
                is Resource.Error -> {
                    _pullsState.value = UiState.Error(pullsRes.message)
                }
            }
        }
    }

    fun setStateFilter(filter: String, owner: String, name: String) {
        _selectedStateFilter.value = filter
        loadIssuesAndPulls(owner, name)
    }

    fun updateSearchFilter(query: String) {
        _searchFilterQuery.value = query
    }

    class Factory(
        private val getIssuesUseCase: GetIssuesUseCase,
        private val getPullRequestsUseCase: GetPullRequestsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IssuesViewModel(getIssuesUseCase, getPullRequestsUseCase) as T
        }
    }
}
