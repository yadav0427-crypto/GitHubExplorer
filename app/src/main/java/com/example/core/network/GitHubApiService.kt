package com.example.core.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface GitHubApiService {

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") token: String? = null
    ): Response<GitHubSearchResponse>

    @GET("repos/{owner}/{name}")
    suspend fun getRepositoryDetails(
        @Path("owner") owner: String,
        @Path("name") name: String,
        @Header("Authorization") token: String? = null
    ): Response<GitHubRepositoryDto>

    @GET("repos/{owner}/{name}/contributors")
    suspend fun getContributors(
        @Path("owner") owner: String,
        @Path("name") name: String,
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") token: String? = null
    ): Response<List<GitHubContributorDto>>

    @GET("repos/{owner}/{name}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("name") name: String,
        @Query("state") state: String = "all", // open, closed, all
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") token: String? = null
    ): Response<List<GitHubIssueDto>>

    @GET("repos/{owner}/{name}/pulls")
    suspend fun getPullRequests(
        @Path("owner") owner: String,
        @Path("name") name: String,
        @Query("state") state: String = "all", // open, closed, all
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") token: String? = null
    ): Response<List<GitHubPullRequestDto>>

    @GET("repos/{owner}/{name}/readme")
    suspend fun getReadme(
        @Path("owner") owner: String,
        @Path("name") name: String,
        @Header("Authorization") token: String? = null
    ): Response<GitHubReadmeResponse>

    @GET
    suspend fun getRawReadme(
        @Url url: String
    ): Response<String>
}
