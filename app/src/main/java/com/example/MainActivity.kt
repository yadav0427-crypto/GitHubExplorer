package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.common.AppContainer
import com.example.feature.bookmarks.BookmarksScreen
import com.example.feature.bookmarks.BookmarksViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.feature.issues.IssuesScreen
import com.example.feature.issues.IssuesViewModel
import com.example.feature.repository.RepositoryScreen
import com.example.feature.repository.RepositoryViewModel
import com.example.feature.search.SearchScreen
import com.example.feature.search.SearchViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract container
        val appContainer = (application as GitExplorerApplication).container

        setContent {
            MyApplicationTheme {
                GitExplorerApp(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun GitExplorerApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: HOME
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        getTrendingRepositoriesUseCase = appContainer.getTrendingRepositoriesUseCase,
                        getBookmarkedRepositoriesUseCase = appContainer.getBookmarkedRepositoriesUseCase,
                        toggleBookmarkUseCase = appContainer.toggleBookmarkUseCase
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToRepository = { owner, name ->
                        navController.navigate("repository/$owner/$name")
                    },
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToBookmarks = { navController.navigate("bookmarks") }
                )
            }

            // Screen 2: SEARCH
            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.Factory(
                        searchRepositoriesUseCase = appContainer.searchRepositoriesUseCase,
                        toggleBookmarkUseCase = appContainer.toggleBookmarkUseCase,
                        gitRepository = appContainer.gitRepository
                    )
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToRepository = { owner, name ->
                        navController.navigate("repository/$owner/$name")
                    }
                )
            }

            // Screen 3: BOOKMARKS
            composable("bookmarks") {
                val bookmarksViewModel: BookmarksViewModel = viewModel(
                    factory = BookmarksViewModel.Factory(
                        getBookmarkedRepositoriesUseCase = appContainer.getBookmarkedRepositoriesUseCase,
                        toggleBookmarkUseCase = appContainer.toggleBookmarkUseCase
                    )
                )
                BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onNavigateToRepository = { owner, name ->
                        navController.navigate("repository/$owner/$name")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 4: REPOSITORY DETAILS
            composable(
                route = "repository/{owner}/{name}",
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""

                val repoViewModel: RepositoryViewModel = viewModel(
                    factory = RepositoryViewModel.Factory(
                        getRepositoryDetailsUseCase = appContainer.getRepositoryDetailsUseCase,
                        getReadmeUseCase = appContainer.getReadmeUseCase,
                        toggleBookmarkUseCase = appContainer.toggleBookmarkUseCase,
                        repository = appContainer.gitRepository
                    )
                )

                RepositoryScreen(
                    viewModel = repoViewModel,
                    owner = owner,
                    name = name,
                    onNavigateToIssues = { ownerArg, nameArg ->
                        navController.navigate("issues/$ownerArg/$nameArg")
                    },
                    onNavigateToPulls = { ownerArg, nameArg ->
                        navController.navigate("pulls/$ownerArg/$nameArg")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 5: ISSUES LIST
            composable(
                route = "issues/{owner}/{name}",
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""

                val issuesViewModel: IssuesViewModel = viewModel(
                    factory = IssuesViewModel.Factory(
                        getIssuesUseCase = appContainer.getIssuesUseCase,
                        getPullRequestsUseCase = appContainer.getPullRequestsUseCase
                    )
                )

                IssuesScreen(
                    viewModel = issuesViewModel,
                    owner = owner,
                    name = name,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 6: PULL REQUESTS LIST
            composable(
                route = "pulls/{owner}/{name}",
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""

                val pullsViewModel: IssuesViewModel = viewModel(
                    factory = IssuesViewModel.Factory(
                        getIssuesUseCase = appContainer.getIssuesUseCase,
                        getPullRequestsUseCase = appContainer.getPullRequestsUseCase
                    )
                )

                IssuesScreen(
                    viewModel = pullsViewModel,
                    owner = owner,
                    name = name,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
