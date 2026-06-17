package com.example.feature.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.EmptyStateLayout
import com.example.feature.home.RepositoryRowItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onNavigateToRepository: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val bookmarkedByQuery by viewModel.bookmarkedList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Bookmarks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Filter query within bookmarks
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Filter bookmarked archives...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
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

            if (bookmarkedByQuery.isEmpty()) {
                val title = if (searchQuery.isNotBlank()) "No matching bookmarks" else "Your chest is empty"
                val body = if (searchQuery.isNotBlank()) {
                    "No bookmarked repositories match '$searchQuery'."
                } else {
                    "Tap the bookmark icon on any repository screen to index it for seamless offline access."
                }
                EmptyStateLayout(
                    title = title,
                    message = body,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(bookmarkedByQuery) { repo ->
                        RepositoryRowItem(
                            repo = repo,
                            onClick = { onNavigateToRepository(repo.ownerLogin, repo.name) },
                            onToggleBookmark = { viewModel.removeBookmark(repo) }
                        )
                    }
                }
            }
        }
    }
}
