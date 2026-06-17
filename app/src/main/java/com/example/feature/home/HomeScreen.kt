package com.example.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.common.CommonUtils
import com.example.core.common.UiState
import com.example.core.designsystem.*
import com.example.core.domain.GithubRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRepository: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val trendingState by viewModel.trendingState.collectAsState()
    val bookmarkedList by viewModel.bookmarkedState.collectAsState()
    val recentlyViewed by viewModel.recentlyViewedState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "GitExplorer",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, "Search repositories")
                    }
                    IconButton(onClick = onNavigateToBookmarks) {
                        BadgedBox(
                            badge = {
                                if (bookmarkedList.isNotEmpty()) {
                                    Badge { Text(bookmarkedList.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Bookmark, "Bookmarks")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Header Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_home_banner),
                        contentDescription = "App banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Discover Open Source",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Explore state-of-the-art archives on GitHub",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick Search Suggestion Bar
            item {
                Card(
                    onClick = onNavigateToSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(56.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search repositories...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Bookmarked Horizontal Pager Quick Access
            if (bookmarkedList.isNotEmpty()) {
                item {
                    Text(
                        text = "YOUR BOOKMARKS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(bookmarkedList) { repo ->
                            Card(
                                onClick = {
                                    viewModel.addRecentlyViewed(repo)
                                    onNavigateToRepository(repo.ownerLogin, repo.name)
                                },
                                modifier = Modifier.width(220.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = repo.ownerAvatarUrl,
                                            contentDescription = "Owner Avatar",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = repo.ownerLogin,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = repo.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = repo.description ?: "No description provided.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                "Stars",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                CommonUtils.formatNumber(repo.stargazersCount),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (repo.language != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    repo.language,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Horizontal Tabs Filters
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DISCOVER TRENDS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label = "Trending Today",
                        selected = selectedCategory == "trending_today",
                        onClick = { viewModel.setCategory("trending_today") }
                    )
                    CategoryChip(
                        label = "Kotlin Stars",
                        selected = selectedCategory == "kotlin_trending",
                        onClick = { viewModel.setCategory("kotlin_trending") }
                    )
                    CategoryChip(
                        label = "Android Repos",
                        selected = selectedCategory == "android_trending",
                        onClick = { viewModel.setCategory("android_trending") }
                    )
                }
            }

            // Main State Handling (Trending List)
            when (val state = trendingState) {
                is UiState.Loading -> {
                    items(5) {
                        ShimmerRepositoryItem()
                    }
                }
                is UiState.Success -> {
                    val repos = state.data
                    if (state.isOffline) {
                        item {
                            OfflineWarningBanner(lastSynced = state.lastUpdated)
                        }
                    }
                    if (repos.isEmpty()) {
                        item {
                            EmptyStateLayout(
                                title = "No results found",
                                message = "We couldn't locate any repositories in this list right now."
                            )
                        }
                    } else {
                        items(repos) { repo ->
                            RepositoryRowItem(
                                repo = repo,
                                onClick = {
                                    viewModel.addRecentlyViewed(repo)
                                    onNavigateToRepository(repo.ownerLogin, repo.name)
                                },
                                onToggleBookmark = { viewModel.toggleBookmark(repo) }
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorStateLayout(
                            errorMessage = state.message,
                            onRetry = { viewModel.loadTrending(forceRefresh = true) }
                        )
                    }
                }
            }

            // Recently Viewed List
            if (recentlyViewed.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENTLY VIEWED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                items(recentlyViewed) { repo ->
                    RepositoryRowItem(
                        repo = repo,
                        onClick = { onNavigateToRepository(repo.ownerLogin, repo.name) },
                        onToggleBookmark = { viewModel.toggleBookmark(repo) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun RepositoryRowItem(
    repo: GithubRepository,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("task_item_card"), // Consistent UI naming test tag
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = repo.ownerAvatarUrl,
                    contentDescription = "Owner avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.ownerLogin,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_button")
                ) {
                    Icon(
                        imageVector = if (repo.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Toggle Bookmark",
                        tint = if (repo.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars count",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = CommonUtils.formatNumber(repo.stargazersCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ForkRight,
                            contentDescription = "Forks count",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = CommonUtils.formatNumber(repo.forksCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (repo.language != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(getLanguageColor(repo.language))
                        )
                        Text(
                            text = repo.language,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Maps language to distinctive GitHub language dot colors
 */
fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFB07219)
        "javascript" -> Color(0xFFF1E05A)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3572A5)
        "swift" -> Color(0xFFF05138)
        "rust" -> Color(0xFFDEA584)
        "go" -> Color(0xFF00ADD8)
        "html" -> Color(0xFFE34C26)
        "css" -> Color(0xFF563D7C)
        else -> Color(0xFF8B8B8B)
    }
}
