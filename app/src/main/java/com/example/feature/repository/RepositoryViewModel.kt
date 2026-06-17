package com.example.feature.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.core.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Gemini Request / Response models for Moshi ---
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiResponse(val candidates: List<GeminiCandidate>)
data class GeminiCandidate(val content: GeminiContent)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): retrofit2.Response<GeminiResponse>
}

class RepositoryViewModel(
    private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase,
    private val getReadmeUseCase: GetReadmeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val repository: com.example.core.data.GitRepository
) : ViewModel() {

    private val _repoState = MutableStateFlow<UiState<GithubRepository>>(UiState.Loading)
    val repoState: StateFlow<UiState<GithubRepository>> = _repoState.asStateFlow()

    private val _readmeState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val readmeState: StateFlow<UiState<String>> = _readmeState.asStateFlow()

    private val _contributorsState = MutableStateFlow<UiState<List<GithubContributor>>>(UiState.Loading)
    val contributorsState: StateFlow<UiState<List<GithubContributor>>> = _contributorsState.asStateFlow()

    // Gemini API AI Summary state
    private val _aiSummaryState = MutableStateFlow<UiState<String>?>(null)
    val aiSummaryState: StateFlow<UiState<String>?> = _aiSummaryState.asStateFlow()

    fun loadRepositoryDetails(owner: String, name: String) {
        viewModelScope.launch {
            _repoState.value = UiState.Loading
            _readmeState.value = UiState.Loading
            _contributorsState.value = UiState.Loading
            _aiSummaryState.value = null

            // 1. Repo Details
            when (val repoResult = getRepositoryDetailsUseCase(owner, name)) {
                is Resource.Success -> {
                    _repoState.value = UiState.Success(repoResult.data)
                }
                is Resource.SuccessOffline -> {
                    _repoState.value = UiState.Success(repoResult.data, isOffline = true, lastUpdated = repoResult.lastUpdated)
                }
                is Resource.Error -> {
                    _repoState.value = UiState.Error(repoResult.message)
                }
            }

            // 2. Readme Content
            when (val readmeResult = getReadmeUseCase(owner, name)) {
                is Resource.Success -> {
                    _readmeState.value = UiState.Success(readmeResult.data)
                }
                is Resource.SuccessOffline -> {
                    _readmeState.value = UiState.Success(readmeResult.data, isOffline = true, lastUpdated = readmeResult.lastUpdated)
                }
                is Resource.Error -> {
                    _readmeState.value = UiState.Error(readmeResult.message)
                }
            }

            // 3. Contributors
            when (val contribResult = repository.getContributors(owner, name)) {
                is Resource.Success -> {
                    _contributorsState.value = UiState.Success(contribResult.data)
                }
                is Resource.Error -> {
                    _contributorsState.value = UiState.Error(contribResult.message)
                }
                else -> {}
            }
        }
    }

    fun toggleBookmark(repo: GithubRepository) {
        viewModelScope.launch {
            toggleBookmarkUseCase(repo)
            val currentState = _repoState.value
            if (currentState is UiState.Success) {
                _repoState.value = UiState.Success(
                    currentState.data.copy(isBookmarked = !currentState.data.isBookmarked),
                    isOffline = currentState.isOffline,
                    lastUpdated = currentState.lastUpdated
                )
            }
        }
    }

    /**
     * Integrates Gemini API (Option B Direct REST Interface) to explain and analyze the repository architecture.
     */
    fun explainCodebase() {
        val detail = _repoState.value
        val readme = _readmeState.value
        if (detail !is UiState.Success) return

        val repo = detail.data
        val readmeText = if (readme is UiState.Success) readme.data.take(1500) else "No README file."

        _aiSummaryState.value = UiState.Loading

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                _aiSummaryState.value = UiState.Error("Gemini Key is missing. Please set GEMINI_API_KEY in the Secrets panel.")
                return@launch
            }

            val prompt = """
                You are an expert production systems architect. Review the following repository metadata and README excerpt, and provide a masterly summary fit for professional developers.
                Include:
                - Codebase Core Purpose (1 sentence)
                - Suggested Clean Architecture mapping representing packages and core scopes
                - Quick code review or setup brief (2 bullets)
                Keep it brief, precise, professional, and directly actionable. Avoid promotional fluff or sales talk.
                
                Metadata:
                Name: ${repo.fullName}
                Description: ${repo.description ?: "N/A"}
                Language: ${repo.language ?: "Kotlin/Android"}
                Stars: ${repo.stargazersCount}
                
                README snippet:
                $readmeText
            """.trimIndent()

            try {
                val response = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build()

                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

                    val geminiService = Retrofit.Builder()
                        .baseUrl("https://generativelanguage.googleapis.com/")
                        .client(client)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                        .create(GeminiService::class.java)

                    val request = GeminiRequest(
                        contents = listOf(
                            GeminiContent(parts = listOf(GeminiPart(prompt)))
                        )
                    )

                    geminiService.generateContent(apiKey, request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val exp = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!exp.isNullOrBlank()) {
                        _aiSummaryState.value = UiState.Success(exp)
                    } else {
                        _aiSummaryState.value = UiState.Error("AI parsed content returned empty. Try again.")
                    }
                } else {
                    _aiSummaryState.value = UiState.Error("Gemini Server Error Code: ${response.code()}! Rate Limit or Key issue.")
                }
            } catch (e: Exception) {
                _aiSummaryState.value = UiState.Error("Could not invoke Gemini: ${e.localizedMessage}")
            }
        }
    }

    class Factory(
        private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase,
        private val getReadmeUseCase: GetReadmeUseCase,
        private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
        private val repository: com.example.core.data.GitRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RepositoryViewModel(
                getRepositoryDetailsUseCase,
                getReadmeUseCase,
                toggleBookmarkUseCase,
                repository
            ) as T
        }
    }
}
