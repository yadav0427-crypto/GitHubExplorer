package com.example.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.UiState
import com.example.core.designsystem.*
import com.example.feature.home.RepositoryRowItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToRepository: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val selectedOrg by viewModel.selectedOrg.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search repositories") },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Toggle advanced filters",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Input Text Field
            TextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                placeholder = { Text("Search repositories...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("username_input"), // Naming Tag rule
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ) 
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateQuery("") }) {
                            Icon(Icons.Default.Close, "Clear query")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = CircleShape
            )

            // Dynamic Advanced Filters Box
            AnimatedVisibility(visible = showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    var langText by remember { mutableStateOf(selectedLanguage ?: "") }
                    var topicText by remember { mutableStateOf(selectedTopic ?: "") }
                    var orgText by remember { mutableStateOf(selectedOrg ?: "") }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Advanced Filtering",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Language Filter Text Inputs
                            OutlinedTextField(
                                value = langText,
                                onValueChange = {
                                    langText = it
                                    viewModel.setLanguage(it.ifBlank { null })
                                },
                                label = { Text("Language", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Topic
                            OutlinedTextField(
                                value = topicText,
                                onValueChange = {
                                    topicText = it
                                    viewModel.setTopic(it.ifBlank { null })
                                },
                                label = { Text("Topic", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = orgText,
                                onValueChange = {
                                    orgText = it
                                    viewModel.setOrg(it.ifBlank { null })
                                },
                                label = { Text("Organization Name", fontSize = 11.sp) },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    langText = ""
                                    topicText = ""
                                    orgText = ""
                                    viewModel.setLanguage(null)
                                    viewModel.setTopic(null)
                                    viewModel.setOrg(null)
                                },
                                colors = ButtonDefaults.textButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Search Trigger Button
            Button(
                onClick = { viewModel.performSearch() },
                enabled = query.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("submit_button"), // Naming Tag
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Search GitHub Archive")
            }

            // History & Suggestions OR Seached Results list
            if (searchState == null) {
                // Pre filled beautiful recommendations
                Text(
                    "Suggested Tech Topics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchSuggestionChip(
                        label = "Jetpack Compose",
                        onClick = { viewModel.selectHistoryQuery("compose") }
                    )
                    SearchSuggestionChip(
                        label = "Coroutines",
                        onClick = { viewModel.selectHistoryQuery("coroutines") }
                    )
                    SearchSuggestionChip(
                        label = "Hilt",
                        onClick = { viewModel.selectHistoryQuery("hilt") }
                    )
                }

                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear All")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(searchHistory) { hist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectHistoryQuery(hist) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History icon",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(hist, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { viewModel.deleteQuery(hist) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    EmptyStateLayout(
                        title = "Begin Exploring",
                        message = "Type repository keywords, organizations, or select a suggested topic to search general archives."
                    )
                }
            } else {
                // List of searched items
                when (val state = searchState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val repos = state.data
                        if (repos.isEmpty()) {
                            EmptyStateLayout(
                                title = "No direct repositories found",
                                message = "We searched deep but could not locate matching entities. Change filters and try again."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                if (state.isOffline) {
                                    item {
                                        OfflineWarningBanner(lastSynced = state.lastUpdated)
                                    }
                                }
                                items(repos) { repo ->
                                    RepositoryRowItem(
                                        repo = repo,
                                        onClick = { onNavigateToRepository(repo.ownerLogin, repo.name) },
                                        onToggleBookmark = { viewModel.toggleBookmark(repo) }
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        ErrorStateLayout(
                            errorMessage = state.message,
                            onRetry = { viewModel.performSearch() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun SearchSuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
