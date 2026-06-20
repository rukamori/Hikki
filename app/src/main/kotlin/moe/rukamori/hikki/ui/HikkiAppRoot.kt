package moe.rukamori.hikki.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.screen.AboutScreen
import moe.rukamori.hikki.ui.screen.AppearanceScreen
import moe.rukamori.hikki.ui.screen.ArchiveScreen
import moe.rukamori.hikki.ui.screen.FoldersScreen
import moe.rukamori.hikki.ui.screen.HomeScreen
import moe.rukamori.hikki.ui.screen.NoteEditorScreen
import moe.rukamori.hikki.ui.screen.NotePreviewScreen
import moe.rukamori.hikki.ui.screen.SearchScreen
import moe.rukamori.hikki.ui.screen.SettingsScreen
import moe.rukamori.hikki.ui.screen.SplashScreen
import moe.rukamori.hikki.ui.screen.TagsScreen
import moe.rukamori.hikki.ui.screen.TrashScreen
import moe.rukamori.hikki.ui.theme.HikkiTheme
import moe.rukamori.hikki.viewmodel.SettingsViewModel

@Composable
fun HikkiAppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val settings = when (val state = settingsState) {
        is ScreenState.Success -> state.data.settings
        else -> AppSettings()
    }

    HikkiTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor,
        pureBlack = settings.pureBlack,
    ) {
        val navController = rememberNavController()
        HikkiNavigation(navController = navController)
    }
}

@Composable
private fun HikkiNavigation(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val topDestinations = remember { TopDestination.entries }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp
        if (compact) {
            Scaffold(
                bottomBar = {
                    if (currentDestination.isTopLevel()) {
                        HikkiNavigationBar(
                            destinations = topDestinations,
                            currentDestination = currentDestination,
                            onNavigate = { navController.navigateTopLevel(it.route) },
                        )
                    }
                },
            ) { innerPadding ->
                HikkiNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                if (currentDestination.isTopLevel()) {
                    HikkiNavigationRail(
                        destinations = topDestinations,
                        currentDestination = currentDestination,
                        onNavigate = { navController.navigateTopLevel(it.route) },
                    )
                }
                HikkiNavHost(
                    navController = navController,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HikkiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenSearch = { navController.navigateTopLevel(Routes.Search) },
            )
        }
        composable(Routes.Search) {
            SearchScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
            )
        }
        composable(Routes.Folders) {
            FoldersScreen(
                onOpenTags = { navController.navigate(Routes.Tags) },
            )
        }
        composable(Routes.Archive) {
            ArchiveScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenTrash = { navController.navigate(Routes.Trash) },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onOpenAppearance = { navController.navigate(Routes.Appearance) },
                onOpenAbout = { navController.navigate(Routes.About) },
                onOpenTrash = { navController.navigate(Routes.Trash) },
            )
        }
        composable(Routes.Tags) {
            TagsScreen(onBack = navController::popBackStack)
        }
        composable(Routes.Trash) {
            TrashScreen(onBack = navController::popBackStack)
        }
        composable(Routes.Appearance) {
            AppearanceScreen(onBack = navController::popBackStack)
        }
        composable(Routes.About) {
            AboutScreen(onBack = navController::popBackStack)
        }
        composable(
            route = Routes.Editor,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
        ) {
            NoteEditorScreen(
                onBack = navController::popBackStack,
                onOpenPreview = { noteId -> navController.navigate(Routes.preview(noteId)) },
            )
        }
        composable(
            route = Routes.Preview,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
        ) {
            NotePreviewScreen(onBack = navController::popBackStack)
        }
    }
}

@Composable
private fun HikkiNavigationBar(
    destinations: List<TopDestination>,
    currentDestination: NavDestination?,
    onNavigate: (TopDestination) -> Unit,
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination?.route == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes),
                    )
                },
                label = { Text(stringResource(destination.titleRes)) },
            )
        }
    }
}

@Composable
private fun HikkiNavigationRail(
    destinations: List<TopDestination>,
    currentDestination: NavDestination?,
    onNavigate: (TopDestination) -> Unit,
) {
    NavigationRail {
        destinations.forEach { destination ->
            NavigationRailItem(
                selected = currentDestination?.route == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes),
                    )
                },
                label = { Text(stringResource(destination.titleRes)) },
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.isTopLevel(): Boolean =
    this?.route in TopDestination.entries.map { it.route }

enum class TopDestination(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
) {
    Notes(Routes.Home, R.string.notes, Icons.Outlined.Create),
    Search(Routes.Search, R.string.search, Icons.Outlined.Search),
    Folders(Routes.Folders, R.string.folders, Icons.Outlined.Folder),
    Archive(Routes.Archive, R.string.archive, Icons.Outlined.Archive),
    Settings(Routes.Settings, R.string.settings, Icons.Outlined.Settings),
}

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Search = "search"
    const val Folders = "folders"
    const val Archive = "archive"
    const val Settings = "settings"
    const val Tags = "tags"
    const val Trash = "trash"
    const val Appearance = "appearance"
    const val About = "about"
    const val Editor = "editor/{noteId}"
    const val Preview = "preview/{noteId}"

    fun editor(noteId: Long): String = "editor/$noteId"

    fun preview(noteId: Long): String = "preview/$noteId"
}
