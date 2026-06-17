package com.example.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_repositories ORDER BY bookmarkedAt DESC")
    fun getBookmarkedRepositories(): Flow<List<BookmarkedRepoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_repositories WHERE id = :id)")
    fun isBookmarkedFlow(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_repositories WHERE id = :id)")
    suspend fun isBookmarked(id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(repo: BookmarkedRepoEntity)

    @Query("DELETE FROM bookmarked_repositories WHERE id = :id")
    suspend fun deleteBookmark(id: Long)
}

@Dao
interface CacheDao {
    @Query("SELECT * FROM cached_repositories WHERE category = :category ORDER BY stargazersCount DESC")
    suspend fun getCachedRepositories(category: String): List<CachedRepoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repositories: List<CachedRepoEntity>)

    @Query("DELETE FROM cached_repositories WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Query("DELETE FROM cached_repositories")
    suspend fun clearAll()
}

@Dao
interface ReadmeDao {
    @Query("SELECT * FROM cached_readmes WHERE repoFullName = :fullName LIMIT 1")
    suspend fun getReadme(fullName: String): CachedReadmeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadme(readme: CachedReadmeEntity)

    @Query("DELETE FROM cached_readmes WHERE repoFullName = :fullName")
    suspend fun deleteReadme(fullName: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 15")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :query")
    suspend fun deleteQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
