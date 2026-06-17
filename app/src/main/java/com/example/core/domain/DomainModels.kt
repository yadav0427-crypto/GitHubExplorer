package com.example.core.domain

import com.example.core.database.BookmarkedRepoEntity
import com.example.core.database.CachedRepoEntity
import com.example.core.network.*

data class GithubRepository(
    val id: Long,
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
    val watchersCount: Int = 0,
    val licenseName: String? = null,
    val isBookmarked: Boolean = false
)

data class GithubIssue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String?,
    val state: String,
    val author: String,
    val authorAvatarUrl: String,
    val createdAt: String,
    val labels: List<GithubLabel> = emptyList(),
    val assignee: String? = null
)

data class GithubLabel(
    val id: Long?,
    val name: String,
    val color: String
)

data class GithubPullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val author: String,
    val authorAvatarUrl: String,
    val state: String,
    val createdAt: String,
    val draft: Boolean = false,
    val closedAt: String? = null,
    val mergedAt: String? = null
)

data class GithubContributor(
    val login: String,
    val avatarUrl: String,
    val contributions: Int
)

// --- Mapper Extensions ---

fun GitHubRepositoryDto.toDomain(isBookmarked: Boolean = false): GithubRepository {
    return GithubRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        openIssuesCount = openIssuesCount,
        language = language,
        updatedAt = updatedAt,
        watchersCount = watchersCount ?: stargazersCount,
        licenseName = license?.name ?: license?.spdxId,
        isBookmarked = isBookmarked
    )
}

fun BookmarkedRepoEntity.toDomain(): GithubRepository {
    return GithubRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = ownerLogin,
        ownerAvatarUrl = ownerAvatarUrl,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        openIssuesCount = openIssuesCount,
        language = language,
        updatedAt = updatedAt,
        watchersCount = watchCount,
        licenseName = licenseName,
        isBookmarked = true
    )
}

fun CachedRepoEntity.toDomain(isBookmarked: Boolean = false): GithubRepository {
    return GithubRepository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = ownerLogin,
        ownerAvatarUrl = ownerAvatarUrl,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        openIssuesCount = openIssuesCount,
        language = language,
        updatedAt = updatedAt,
        watchersCount = stargazersCount,
        licenseName = null,
        isBookmarked = isBookmarked
    )
}

fun GithubRepository.toBookmarkEntity(): BookmarkedRepoEntity {
    return BookmarkedRepoEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = ownerLogin,
        ownerAvatarUrl = ownerAvatarUrl,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        openIssuesCount = openIssuesCount,
        language = language,
        updatedAt = updatedAt,
        watchCount = watchersCount,
        licenseName = licenseName
    )
}

fun GitHubRepositoryDto.toCacheEntity(category: String, query: String? = null): CachedRepoEntity {
    return CachedRepoEntity(
        id = id,
        category = category,
        name = name,
        fullName = fullName,
        description = description,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        openIssuesCount = openIssuesCount,
        language = language,
        updatedAt = updatedAt,
        query = query
    )
}

fun GitHubIssueDto.toDomain(): GithubIssue {
    return GithubIssue(
        id = id,
        number = number,
        title = title,
        body = body,
        state = state,
        author = user.login,
        authorAvatarUrl = user.avatarUrl,
        createdAt = createdAt,
        labels = labels?.map { GithubLabel(it.id, it.name, it.color) } ?: emptyList(),
        assignee = assignee?.login
    )
}

fun GitHubPullRequestDto.toDomain(): GithubPullRequest {
    return GithubPullRequest(
        id = id,
        number = number,
        title = title,
        author = user.login,
        authorAvatarUrl = user.avatarUrl,
        state = state,
        createdAt = createdAt,
        draft = draft ?: false,
        closedAt = closedAt,
        mergedAt = mergedAt
    )
}

fun GitHubContributorDto.toDomain(): GithubContributor {
    return GithubContributor(
        login = login,
        avatarUrl = avatarUrl,
        contributions = contributions
    )
}
