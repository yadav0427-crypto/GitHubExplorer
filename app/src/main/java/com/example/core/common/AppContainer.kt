package com.example.core.common

import android.content.Context
import com.example.core.data.GitRepository
import com.example.core.data.GitRepositoryImpl
import com.example.core.database.AppDatabase
import com.example.core.domain.*
import com.example.core.network.RetrofitClient

interface AppContainer {
    val gitRepository: GitRepository
    
    // Use Cases
    val getTrendingRepositoriesUseCase: GetTrendingRepositoriesUseCase
    val searchRepositoriesUseCase: SearchRepositoriesUseCase
    val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase
    val toggleBookmarkUseCase: ToggleBookmarkUseCase
    val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase
    val getReadmeUseCase: GetReadmeUseCase
    val getIssuesUseCase: GetIssuesUseCase
    val getPullRequestsUseCase: GetPullRequestsUseCase
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    private val gitHubApiService by lazy {
        RetrofitClient.githubApiService
    }

    override val gitRepository: GitRepository by lazy {
        GitRepositoryImpl(
            apiService = gitHubApiService,
            bookmarkDao = appDatabase.bookmarkDao(),
            cacheDao = appDatabase.cacheDao(),
            readmeDao = appDatabase.readmeDao(),
            searchHistoryDao = appDatabase.searchHistoryDao()
        )
    }

    override val getTrendingRepositoriesUseCase: GetTrendingRepositoriesUseCase by lazy {
        GetTrendingRepositoriesUseCase(gitRepository)
    }

    override val searchRepositoriesUseCase: SearchRepositoriesUseCase by lazy {
        SearchRepositoriesUseCase(gitRepository)
    }

    override val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase by lazy {
        GetRepositoryDetailsUseCase(gitRepository)
    }

    override val toggleBookmarkUseCase: ToggleBookmarkUseCase by lazy {
        ToggleBookmarkUseCase(gitRepository)
    }

    override val getBookmarkedRepositoriesUseCase: GetBookmarkedRepositoriesUseCase by lazy {
        GetBookmarkedRepositoriesUseCase(gitRepository)
    }

    override val getReadmeUseCase: GetReadmeUseCase by lazy {
        GetReadmeUseCase(gitRepository)
    }

    override val getIssuesUseCase: GetIssuesUseCase by lazy {
        GetIssuesUseCase(gitRepository)
    }

    override val getPullRequestsUseCase: GetPullRequestsUseCase by lazy {
        GetPullRequestsUseCase(gitRepository)
    }
}
