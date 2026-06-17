package com.example.core.domain

import com.example.core.common.Resource
import com.example.core.data.GitRepository
import kotlinx.coroutines.flow.Flow

class GetTrendingRepositoriesUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(category: String, forceRefresh: Boolean = false): Resource<List<GithubRepository>> {
        return repository.getTrendingRepositories(category, forceRefresh)
    }
}

class SearchRepositoriesUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(
        query: String,
        language: String? = null,
        topic: String? = null,
        org: String? = null,
        page: Int = 1
    ): Resource<List<GithubRepository>> {
        return repository.searchRepositories(query, language, topic, org, page)
    }
}

class GetRepositoryDetailsUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(owner: String, name: String): Resource<GithubRepository> {
        val detailResult = repository.getRepositoryDetails(owner, name)
        return detailResult
    }
}

class ToggleBookmarkUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(repo: GithubRepository) {
        repository.toggleBookmark(repo)
    }
}

class GetBookmarkedRepositoriesUseCase(private val repository: GitRepository) {
    operator fun invoke(): Flow<List<GithubRepository>> {
        return repository.getBookmarkedRepositories()
    }
}

class GetReadmeUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(owner: String, name: String, forceRefresh: Boolean = false): Resource<String> {
        return repository.getReadme(owner, name, forceRefresh)
    }
}

class GetIssuesUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(owner: String, name: String, state: String, page: Int = 1): Resource<List<GithubIssue>> {
        return repository.getIssues(owner, name, state, page)
    }
}

class GetPullRequestsUseCase(private val repository: GitRepository) {
    suspend operator fun invoke(owner: String, name: String, state: String, page: Int = 1): Resource<List<GithubPullRequest>> {
        return repository.getPullRequests(owner, name, state, page)
    }
}
