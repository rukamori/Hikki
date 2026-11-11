@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
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
    val settings =
        when (val state = settingsState) {
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
private fun HikkiNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showNavigation = currentRoute in TopLevelRoutes

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 600.dp) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    AnimatedVisibility(
                        visible = showNavigation,
                        enter = slideInVertically(tween(320, easing = EmphasizedDecelerate)) { it } + fadeIn(tween(180)),
                        exit = slideOutVertically(tween(200, easing = EmphasizedAccelerate)) { it } + fadeOut(tween(120)),
                    ) {
                        HikkiNavigationBar(
                            currentRoute = currentRoute,
                            onNavigate = { navController.navigateTopLevel(it.route) },
                        )
                    }
                },
            ) { innerPadding ->
                HikkiNavHost(
                    navController = navController,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                )
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showNavigation,
                    enter = expandHorizontally(tween(320, easing = EmphasizedDecelerate)) + fadeIn(tween(180)),
                    exit = shrinkHorizontally(tween(200, easing = EmphasizedAccelerate)) + fadeOut(tween(120)),
                ) {
                    HikkiNavigationRail(
                        currentRoute = currentRoute,
                        onNavigate = { navController.navigateTopLevel(it.route) },
                    )
                }
                HikkiNavHost(
                    navController = navController,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(
                                if (showNavigation) {
                                    Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                                } else {
                                    Modifier
                                },
                            ),
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
        enterTransition = { enterFor(initialState, targetState, pop = false) },
        exitTransition = { exitFor(initialState, targetState, pop = false) },
        popEnterTransition = { enterFor(initialState, targetState, pop = true) },
        popExitTransition = { exitFor(initialState, targetState, pop = true) },
    ) {
        composable(
            route = Routes.Splash,
            exitTransition = { fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 1.08f) },
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) { entry ->
            val message by entry.resultMessage()
            HomeScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenSearch = { navController.navigateTopLevel(Routes.Search) },
                resultMessageRes = message,
                onResultMessageShown = { entry.clearResultMessage() },
            )
        }
        composable(Routes.Search) { entry ->
            val message by entry.resultMessage()
            SearchScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                resultMessageRes = message,
                onResultMessageShown = { entry.clearResultMessage() },
            )
        }
        composable(Routes.Folders) {
            FoldersScreen(
                onOpenTags = { navController.navigate(Routes.Tags) },
            )
        }
        composable(Routes.Archive) { entry ->
            val message by entry.resultMessage()
            ArchiveScreen(
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenTrash = { navController.navigate(Routes.Trash) },
                resultMessageRes = message,
                onResultMessageShown = { entry.clearResultMessage() },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onOpenAppearance = { navController.navigate(Routes.Appearance) },
                onOpenAbout = { navController.navigate(Routes.About) },
                onOpenTrash = { navController.navigate(Routes.Trash) },
                onOpenArchive = { navController.navigateTopLevel(Routes.Archive) },
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
                onClosed = { messageRes ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(Routes.ResultMessageKey, messageRes)
                    navController.popBackStack()
                },
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

private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private fun isTopLevelSwitch(
    from: NavBackStackEntry,
    to: NavBackStackEntry,
): Boolean = from.destination.route in TopLevelRoutes && to.destination.route in TopLevelRoutes

private fun enterFor(
    from: NavBackStackEntry,
    to: NavBackStackEntry,
    pop: Boolean,
): EnterTransition =
    if (isTopLevelSwitch(from, to)) {
        fadeIn(tween(durationMillis = 260, delayMillis = 90, easing = EmphasizedDecelerate)) +
            scaleIn(tween(durationMillis = 320, delayMillis = 90, easing = EmphasizedDecelerate), initialScale = 0.94f)
    } else {
        slideInHorizontally(tween(420, easing = Emphasized)) { width -> if (pop) -width / 5 else width / 5 } +
            fadeIn(tween(durationMillis = 220, delayMillis = 60))
    }

private fun exitFor(
    from: NavBackStackEntry,
    to: NavBackStackEntry,
    pop: Boolean,
): ExitTransition =
    if (isTopLevelSwitch(from, to)) {
        fadeOut(tween(durationMillis = 90, easing = EmphasizedAccelerate))
    } else {
        slideOutHorizontally(tween(420, easing = Emphasized)) { width -> if (pop) width / 5 else -width / 5 } +
            fadeOut(tween(durationMillis = 140))
    }

@Composable
private fun HikkiNavigationBar(
    currentRoute: String?,
    onNavigate: (TopDestination) -> Unit,
) {
    ShortNavigationBar {
        TopDestination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            ShortNavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.titleRes)) },
            )
        }
    }
}

@Composable
private fun HikkiNavigationRail(
    currentRoute: String?,
    onNavigate: (TopDestination) -> Unit,
) {
    val state = rememberWideNavigationRailState(WideNavigationRailValue.Collapsed)
    val expanded = state.targetValue == WideNavigationRailValue.Expanded
    val scope = rememberCoroutineScope()
    WideNavigationRail(
        state = state,
        header = {
            IconButton(
                onClick = {
                    scope.launch {
                        if (expanded) state.collapse() else state.expand()
                    }
                },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.padding(start = 20.dp),
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Outlined.Close else Icons.Outlined.Menu,
                    contentDescription = stringResource(if (expanded) R.string.collapse_navigation else R.string.expand_navigation),
                )
            }
        },
    ) {
        TopDestination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            WideNavigationRailItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.titleRes)) },
                railExpanded = expanded,
            )
        }
    }
}

@Composable
private fun NavBackStackEntry.resultMessage(): State<Int?> =
    savedStateHandle.getStateFlow<Int?>(Routes.ResultMessageKey, null).collectAsStateWithLifecycle()

private fun NavBackStackEntry.clearResultMessage() {
    savedStateHandle[Routes.ResultMessageKey] = null
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

enum class TopDestination(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Notes(Routes.Home, R.string.notes, Icons.Outlined.Description, Icons.Filled.Description),
    Search(Routes.Search, R.string.search, Icons.Outlined.Search, Icons.Filled.Search),
    Folders(Routes.Folders, R.string.folders, Icons.Outlined.Folder, Icons.Filled.Folder),
    Archive(Routes.Archive, R.string.archive, Icons.Outlined.Archive, Icons.Filled.Archive),
    Settings(Routes.Settings, R.string.settings, Icons.Outlined.Settings, Icons.Filled.Settings),
}

private val TopLevelRoutes: Set<String> = TopDestination.entries.map { it.route }.toSet()

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
    const val ResultMessageKey = "result_message"

    fun editor(noteId: Long): String = "editor/$noteId"

    fun preview(noteId: Long): String = "preview/$noteId"
}
