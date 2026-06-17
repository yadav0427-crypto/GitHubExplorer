package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubSearchResponse(
    @Json(name = "items") val items: List<GitHubRepositoryDto>
)

@JsonClass(generateAdapter = true)
data class GitHubRepositoryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "owner") val owner: GitHubOwnerDto,
    @Json(name = "stargazers_count") val stargazersCount: Int,
    @Json(name = "forks_count") val forksCount: Int,
    @Json(name = "open_issues_count") val openIssuesCount: Int,
    @Json(name = "language") val language: String?,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "subscribers_count") val watchersCount: Int? = 0,
    @Json(name = "license") val license: GitHubLicenseDto? = null
)

@JsonClass(generateAdapter = true)
data class GitHubOwnerDto(
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String
)

@JsonClass(generateAdapter = true)
data class GitHubLicenseDto(
    @Json(name = "key") val key: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "spdx_id") val spdxId: String?
)

@JsonClass(generateAdapter = true)
data class GitHubContributorDto(
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "contributions") val contributions: Int
)

@JsonClass(generateAdapter = true)
data class GitHubIssueDto(
    @Json(name = "id") val id: Long,
    @Json(name = "number") val number: Int,
    @Json(name = "title") val title: String,
    @Json(name = "body") val body: String?,
    @Json(name = "state") val state: String,
    @Json(name = "user") val user: GitHubOwnerDto,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "labels") val labels: List<GitHubLabelDto>? = emptyList(),
    @Json(name = "assignee") val assignee: GitHubOwnerDto? = null
)

@JsonClass(generateAdapter = true)
data class GitHubLabelDto(
    @Json(name = "id") val id: Long?,
    @Json(name = "name") val name: String,
    @Json(name = "color") val color: String
)

@JsonClass(generateAdapter = true)
data class GitHubPullRequestDto(
    @Json(name = "id") val id: Long,
    @Json(name = "number") val number: Int,
    @Json(name = "title") val title: String,
    @Json(name = "user") val user: GitHubOwnerDto,
    @Json(name = "state") val state: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "draft") val draft: Boolean? = false,
    @Json(name = "closed_at") val closedAt: String? = null,
    @Json(name = "merged_at") val mergedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubReadmeResponse(
    @Json(name = "content") val content: String?,
    @Json(name = "encoding") val encoding: String?,
    @Json(name = "download_url") val downloadUrl: String?
)
