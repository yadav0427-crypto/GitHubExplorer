package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_repositories")
data class BookmarkedRepoEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val stargazersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val language: String?,
    val updatedAt: String,
    val watchCount: Int,
    val licenseName: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_repositories")
data class CachedRepoEntity(
    @PrimaryKey val id: Long,
    val category: String, // e.g. "trending", "kotlin", "android", "search_result"
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val stargazersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val language: String?,
    val updatedAt: String,
    val query: String? = null, // if category is "search_result"
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_readmes")
data class CachedReadmeEntity(
    @PrimaryKey val repoFullName: String,
    val markdownContent: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)
