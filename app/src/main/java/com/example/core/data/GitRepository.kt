package com.example.core.data

import com.example.core.common.Resource
import com.example.core.database.*
import com.example.core.domain.*
import com.example.core.network.GitHubApiService
import com.example.core.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

interface GitRepository {
    fun getBookmarkedRepositories(): Flow<List<GithubRepository>>
    fun isBookmarkedFlow(id: Long): Flow<Boolean>
    suspend fun toggleBookmark(repo: GithubRepository)
    
    suspend fun getTrendingRepositories(
        category: String, // "trending_today", "kotlin_trending", "android_trending"
        forceRefresh: Boolean = false
    ): Resource<List<GithubRepository>>

    suspend fun searchRepositories(
        query: String,
        language: String? = null,
        topic: String? = null,
        org: String? = null,
        page: Int = 1
    ): Resource<List<GithubRepository>>

    suspend fun getRepositoryDetails(
        owner: String,
        name: String
    ): Resource<GithubRepository>

    suspend fun getContributors(
        owner: String,
        name: String
    ): Resource<List<GithubContributor>>

    suspend fun getIssues(
        owner: String,
        name: String,
        state: String, // "open", "closed", "all"
        page: Int = 1
    ): Resource<List<GithubIssue>>

    suspend fun getPullRequests(
        owner: String,
        name: String,
        state: String, // "open", "closed", "all"
        page: Int = 1
    ): Resource<List<GithubPullRequest>>

    suspend fun getReadme(
        owner: String,
        name: String,
        forceRefresh: Boolean = false
    ): Resource<String>

    fun getSearchHistory(): Flow<List<String>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()
}

class GitRepositoryImpl(
    private val apiService: GitHubApiService,
    private val bookmarkDao: BookmarkDao,
    private val cacheDao: CacheDao,
    private val readmeDao: ReadmeDao,
    private val searchHistoryDao: SearchHistoryDao
) : GitRepository {

    override fun getBookmarkedRepositories(): Flow<List<GithubRepository>> {
        return bookmarkDao.getBookmarkedRepositories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun isBookmarkedFlow(id: Long): Flow<Boolean> {
        return bookmarkDao.isBookmarkedFlow(id)
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return searchHistoryDao.getSearchHistory().map { list ->
            list.map { it.query }
        }
    }

    override suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertQuery(SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteQuery(query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    override suspend fun toggleBookmark(repo: GithubRepository) {
        val isCurrentBookmarked = bookmarkDao.isBookmarked(repo.id)
        if (isCurrentBookmarked) {
            bookmarkDao.deleteBookmark(repo.id)
        } else {
            bookmarkDao.insertBookmark(repo.toBookmarkEntity())
        }
    }

    override suspend fun getTrendingRepositories(
        category: String,
        forceRefresh: Boolean
    ): Resource<List<GithubRepository>> {
        // Build specific search queries representing our trending sections
        val apiQuery = when (category) {
            "trending_today" -> "stars:>10000+language:kotlin"
            "kotlin_trending" -> "stars:>5000+language:kotlin"
            "android_trending" -> "stars:>4000+topic:android"
            else -> "stars:>10000"
        }

        val cached = cacheDao.getCachedRepositories(category)
        
        if (!forceRefresh && cached.isNotEmpty()) {
            // Return cached data as successful offline resource
            val isBookmarkedList = getBookmarkedIds()
            val mapped = cached.map { it.toDomain(isBookmarked = isBookmarkedList.contains(it.id)) }
            return Resource.SuccessOffline(mapped, cached.first().cachedAt)
        }

        return try {
            val response = apiService.searchRepositories(
                query = apiQuery,
                sort = "stars",
                order = "desc",
                perPage = 20,
                page = 1
            )
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!.items
                
                // Clear old cash and insert new cash
                cacheDao.clearCategory(category)
                val cacheEntities = dtoList.map { it.toCacheEntity(category) }
                cacheDao.insertAll(cacheEntities)
                
                val isBookmarkedList = getBookmarkedIds()
                val domains = dtoList.map { dto ->
                    dto.toDomain(isBookmarked = isBookmarkedList.contains(dto.id))
                }
                Resource.Success(domains, System.currentTimeMillis())
            } else {
                handleNetworkError(response.code(), response.message(), cached, "trending")
            }
        } catch (e: Exception) {
            handleException(e, cached, "trending")
        }
    }

    override suspend fun searchRepositories(
        query: String,
        language: String?,
        topic: String?,
        org: String?,
        page: Int
    ): Resource<List<GithubRepository>> {
        var searchQuery = query
        if (!language.isNullOrBlank()) searchQuery += "+language:$language"
        if (!topic.isNullOrBlank()) searchQuery += "+topic:$topic"
        if (!org.isNullOrBlank()) searchQuery += "+org:$org"

        // Search repositories API limits public usages so we handle cases gracefully
        return try {
            val response = apiService.searchRepositories(
                query = searchQuery,
                sort = "stars",
                order = "desc",
                perPage = 20,
                page = page
            )
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!.items
                val isBookmarkedList = getBookmarkedIds()
                val domains = dtoList.map { dto ->
                    dto.toDomain(isBookmarked = isBookmarkedList.contains(dto.id))
                }
                
                // Persistence of search query in search history
                if (page == 1) {
                    addSearchQuery(query)
                }
                
                Resource.Success(domains)
            } else {
                handleNetworkError(response.code(), response.message(), emptyList<CachedRepoEntity>(), "search")
            }
        } catch (e: Exception) {
            handleException(e, emptyList<CachedRepoEntity>(), "search")
        }
    }

    override suspend fun getRepositoryDetails(
        owner: String,
        name: String
    ): Resource<GithubRepository> {
        val fullName = "$owner/$name"
        return try {
            val response = apiService.getRepositoryDetails(owner, name)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val isBookmarked = bookmarkDao.isBookmarked(dto.id)
                Resource.Success(dto.toDomain(isBookmarked))
            } else {
                Resource.Error("GitHub API return failure code: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unexpected connection error", throwable = e)
        }
    }

    override suspend fun getContributors(
        owner: String,
        name: String
    ): Resource<List<GithubContributor>> {
        return try {
            val response = apiService.getContributors(owner, name, perPage = 12)
            if (response.isSuccessful && response.body() != null) {
                val domains = response.body()!!.map { it.toDomain() }
                Resource.Success(domains)
            } else {
                Resource.Error("Failed to fetch contributors: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "API Connection failure", throwable = e)
        }
    }

    override suspend fun getIssues(
        owner: String,
        name: String,
        state: String,
        page: Int
    ): Resource<List<GithubIssue>> {
        return try {
            val response = apiService.getIssues(owner, name, state = state, perPage = 20, page = page)
            if (response.isSuccessful && response.body() != null) {
                val domains = response.body()!!.map { it.toDomain() }
                // Pull requests can sometimes appear in issues. GitHub matches them.
                // We'll exclude pull request items from this list if the body indicates it or if handled.
                Resource.Success(domains)
            } else {
                handleNetworkError(response.code(), response.message(), emptyList<CachedRepoEntity>(), "issues")
            }
        } catch (e: Exception) {
            handleException(e, emptyList<CachedRepoEntity>(), "issues")
        }
    }

    override suspend fun getPullRequests(
        owner: String,
        name: String,
        state: String,
        page: Int
    ): Resource<List<GithubPullRequest>> {
        return try {
            val response = apiService.getPullRequests(owner, name, state = state, perPage = 20, page = page)
            if (response.isSuccessful && response.body() != null) {
                val domains = response.body()!!.map { it.toDomain() }
                Resource.Success(domains)
            } else {
                handleNetworkError(response.code(), response.message(), emptyList<CachedRepoEntity>(), "pulls")
            }
        } catch (e: Exception) {
            handleException(e, emptyList<CachedRepoEntity>(), "pulls")
        }
    }

    override suspend fun getReadme(
        owner: String,
        name: String,
        forceRefresh: Boolean
    ): Resource<String> {
        val fullName = "$owner/$name"
        val cached = readmeDao.getReadme(fullName)
        
        if (!forceRefresh && cached != null) {
            return Resource.SuccessOffline(cached.markdownContent, cached.cachedAt)
        }

        // We attempt to fetch the Readme via two strategies:
        // Strategy A: Call GitHub raw contents (fastest, supports larger files, bypassed rate limit issues in many networks)
        // Strategy B: Call API get readme
        return try {
            val rawRes = apiService.getRawReadme("https://raw.githubusercontent.com/$owner/$name/main/README.md")
            val finalRes = if (rawRes.isSuccessful && rawRes.body() != null) {
                rawRes
            } else {
                apiService.getRawReadme("https://raw.githubusercontent.com/$owner/$name/master/README.md")
            }

            if (finalRes.isSuccessful && finalRes.body() != null) {
                val content = finalRes.body()!!
                readmeDao.insertReadme(CachedReadmeEntity(fullName, content))
                Resource.Success(content, System.currentTimeMillis())
            } else {
                // Try Strategy B
                val apiRes = apiService.getReadme(owner, name)
                if (apiRes.isSuccessful && apiRes.body() != null) {
                    val rawBase64 = apiRes.body()!!.content ?: ""
                    val cleaned = rawBase64.replace("\n", "").replace("\r", "")
                    val decoded = android.util.Base64.decode(cleaned, android.util.Base64.DEFAULT).decodeToString()
                    readmeDao.insertReadme(CachedReadmeEntity(fullName, decoded))
                    Resource.Success(decoded, System.currentTimeMillis())
                } else {
                    if (cached != null) {
                        Resource.SuccessOffline(cached.markdownContent, cached.cachedAt)
                    } else {
                        Resource.Error("Could not locate README.md file in repository.")
                    }
                }
            }
        } catch (e: Exception) {
            if (cached != null) {
                Resource.SuccessOffline(cached.markdownContent, cached.cachedAt)
            } else {
                Resource.Error("Could not retrieve README: ${e.localizedMessage}", throwable = e)
            }
        }
    }

    // Helpers
    private suspend fun getBookmarkedIds(): Set<Long> {
        return bookmarkDao.getBookmarkedRepositories().first().map { it.id }.toSet()
    }

    private fun handleNetworkError(
        code: Int,
        msg: String,
        cachedEntities: List<CachedRepoEntity>,
        section: String
    ): Resource<Nothing> {
        val errorMessage = when (code) {
            401 -> "Unauthorized access. Check credentials."
            403 -> "GitHub API rate limit exceeded. Please try again in a while."
            404 -> "Resource not found on GitHub."
            429 -> "Too many requests. Please slow down."
            in 500..599 -> "GitHub server error. Try again later."
            else -> "HTTP $code Error: $msg"
        }
        return Resource.Error(errorMessage, code)
    }

    private fun handleException(
        e: Exception,
        cachedEntities: List<CachedRepoEntity>,
        section: String
    ): Resource<Nothing> {
        val message = if (e is IOException) {
            "No internet connection. Please verify your connection."
        } else {
            e.localizedMessage ?: "An unexpected error occurred"
        }
        return Resource.Error(message, throwable = e)
    }
}
