package com.example.feature.issues

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.common.CommonUtils
import com.example.core.common.UiState
import com.example.core.designsystem.*
import com.example.core.domain.GithubIssue
import com.example.core.domain.GithubPullRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesScreen(
    viewModel: IssuesViewModel,
    owner: String,
    name: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val issuesState by viewModel.issuesState.collectAsState()
    val pullsState by viewModel.pullsState.collectAsState()
    val selectedFilter by viewModel.selectedStateFilter.collectAsState()
    val searchQuery by viewModel.searchFilterQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Issues, 1 = Pull Requests

    LaunchedEffect(owner, name) {
        viewModel.loadIssuesAndPulls(owner, name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$name - Live Audits") },
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
            // Screen Section Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Issues", style = MaterialTheme.typography.titleSmall) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pull Requests", style = MaterialTheme.typography.titleSmall) }
                )
            }

            // Keyword Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchFilter(it) },
                    placeholder = { Text("Keyword filter...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Filter Buttons (Open, Closed, All)
                FilterIconButton(
                    label = "All",
                    selected = selectedFilter == "all",
                    onClick = { viewModel.setStateFilter("all", owner, name) }
                )
                FilterIconButton(
                    label = "Open",
                    selected = selectedFilter == "open",
                    onClick = { viewModel.setStateFilter("open", owner, name) }
                )
                FilterIconButton(
                    label = "Closed",
                    selected = selectedFilter == "closed",
                    onClick = { viewModel.setStateFilter("closed", owner, name) }
                )
            }

            // Results List
            if (selectedTab == 0) {
                // RENDER ISSUES
                when (val state = issuesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val filteredList = state.data.filter {
                            it.title.contains(searchQuery, ignoreCase = true) || 
                            (it.body?.contains(searchQuery, ignoreCase = true) ?: false)
                        }

                        if (state.isOffline) {
                            OfflineWarningBanner(lastSynced = state.lastUpdated)
                        }

                        if (filteredList.isEmpty()) {
                            EmptyStateLayout(
                                title = "No issues found",
                                message = "There are no issues matching the specified state and keywords."
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredList) { issue ->
                                    IssueRowCard(issue = issue)
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        ErrorStateLayout(errorMessage = state.message) {
                            viewModel.loadIssuesAndPulls(owner, name)
                        }
                    }
                }
            } else {
                // RENDER PRs
                when (val state = pullsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val filteredList = state.data.filter {
                            it.title.contains(searchQuery, ignoreCase = true)
                        }

                        if (state.isOffline) {
                            OfflineWarningBanner(lastSynced = state.lastUpdated)
                        }

                        if (filteredList.isEmpty()) {
                            EmptyStateLayout(
                                title = "No pull requests found",
                                message = "There are no pull requests matching your search state or criteria."
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredList) { pr ->
                                    PullRequestRowCard(pr = pr)
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        ErrorStateLayout(errorMessage = state.message) {
                            viewModel.loadIssuesAndPulls(owner, name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterIconButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IssueRowCard(issue: GithubIssue) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (issue.state == "open") Icons.Default.Adjust else Icons.Default.CheckCircle,
                    contentDescription = "Issue State",
                    tint = if (issue.state == "open") Color(0xFF2EA44F) else Color(0xFF8250DF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${issue.number} - ${issue.title}",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Opened by ${issue.author} • ${CommonUtils.formatDateISO(issue.createdAt)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Labels Flow
            if (issue.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    issue.labels.take(4).forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(getColorFromHex(label.color).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = getColorFromHex(label.color)
                            )
                        }
                    }
                }
            }

            // Assignee details
            if (issue.assignee != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, "Assignee", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Assigned to: ${issue.assignee}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PullRequestRowCard(pr: GithubPullRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MergeType,
                    contentDescription = "PR State",
                    tint = if (pr.state == "closed" && pr.mergedAt != null) Color(0xFF8250DF) else if (pr.state == "open") Color(0xFF2EA44F) else Color(0xFFCF222E),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${pr.number} - ${pr.title}",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val descriptionLine = when {
                        pr.state == "closed" && pr.mergedAt != null -> "Merged by ${pr.author} • ${CommonUtils.formatDateISO(pr.mergedAt)}"
                        pr.state == "closed" -> "Closed by ${pr.author} • ${CommonUtils.formatDateISO(pr.closedAt)}"
                        else -> "Opened by ${pr.author} • ${CommonUtils.formatDateISO(pr.createdAt)}"
                    }
                    Text(
                        text = descriptionLine,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pr.draft) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Draft PR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

/**
 * Parsers a GitHub hex color code safely (e.g., "ff0000" or "bfdadc")
 */
fun getColorFromHex(hex: String): Color {
    return try {
        val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
        val colorInt = cleanHex.toLong(16).toInt()
        val formattedColor = if (cleanHex.length == 6) {
            0xFF000000.toInt() or colorInt
        } else {
            colorInt
        }
        Color(formattedColor)
    } catch (e: Exception) {
        Color.Gray
    }
}
