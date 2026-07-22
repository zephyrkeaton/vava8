package com.vava8.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.vava8.app.Vava8App
import com.vava8.app.ui.auth.LoginScreen
import com.vava8.app.ui.auth.RegisterScreen
import com.vava8.app.ui.channels.ChannelsScreen
import com.vava8.app.ui.components.SwipeBackContainer
import com.vava8.app.ui.components.TabSwipeContainer
import com.vava8.app.ui.create.CreateScreen
import com.vava8.app.ui.detail.ArticleDetailScreen
import com.vava8.app.ui.discover.DiscoverScreen
import com.vava8.app.ui.discover.DiscoverViewModel
import com.vava8.app.ui.history.BrowseHistoryScreen
import com.vava8.app.ui.home.HomeScreen
import com.vava8.app.ui.home.HomeViewModel
import com.vava8.app.ui.profile.AboutAppScreen
import com.vava8.app.ui.profile.PersonalCenterUrls
import com.vava8.app.ui.profile.ProfileScreen
import com.vava8.app.ui.profile.WebInfoScreen
import com.vava8.app.ui.search.SearchScreen
import com.vava8.app.ui.theme.BrandBlue
import com.vava8.app.ui.theme.ReadingFontSize
import com.vava8.app.ui.theme.ThemeMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Discover : Dest("discover")
    data object Create : Dest("create")
    data object Profile : Dest("profile")
    data object Search : Dest("search")
    data object Channels : Dest("channels")
    data object Login : Dest("login")
    data object Register : Dest("register")
    data object Article : Dest("article/{id}") {
        fun of(id: Long) = "article/$id"
    }
    data object ChannelFeed : Dest("channel/{id}/{name}") {
        fun of(id: Int, name: String): String {
            val encoded = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
            return "channel/$id/$encoded"
        }
    }
    data object WebInfo : Dest("webinfo/{title}/{url}") {
        fun of(title: String, url: String): String {
            val t = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val u = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            return "webinfo/$t/$u"
        }
    }
    data object AboutApp : Dest("about_app")
    data object BrowseHistory : Dest("browse_history")
}

private data class TabItem(
    val dest: Dest,
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector
)

private val tabs = listOf(
    TabItem(Dest.Home, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(Dest.Discover, "发现", Icons.Filled.Explore, Icons.Outlined.Explore),
    TabItem(Dest.Create, "发帖", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    TabItem(Dest.Profile, "我的", Icons.Filled.Person, Icons.Outlined.PersonOutline)
)

/** 手势已把当前页滑出时，跳过 NavHost 退出动画，避免闪回。 */
internal object TabTransitionState {
    @Volatile
    var suppressNextExit: Boolean = false
}

private fun tabIndex(route: String?): Int = when {
    route == Dest.Home.route || route?.startsWith("channel/") == true -> 0
    route == Dest.Discover.route -> 1
    route == Dest.Create.route -> 2
    route == Dest.Profile.route -> 3
    else -> -1
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition {
    val from = tabIndex(initialState.destination.route)
    val to = tabIndex(targetState.destination.route)
    if (from < 0 || to < 0) {
        return fadeIn(animationSpec = tween(180))
    }
    return if (to > from) {
        slideInHorizontally(
            animationSpec = tween(260),
            initialOffsetX = { it }
        ) + fadeIn(animationSpec = tween(200))
    } else {
        slideInHorizontally(
            animationSpec = tween(260),
            initialOffsetX = { -it }
        ) + fadeIn(animationSpec = tween(200))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition {
    val from = tabIndex(initialState.destination.route)
    val to = tabIndex(targetState.destination.route)
    if (from < 0 || to < 0) {
        return fadeOut(animationSpec = tween(180))
    }
    if (TabTransitionState.suppressNextExit) {
        TabTransitionState.suppressNextExit = false
        return ExitTransition.None
    }
    return if (to > from) {
        slideOutHorizontally(
            animationSpec = tween(260),
            targetOffsetX = { -it }
        ) + fadeOut(animationSpec = tween(200))
    } else {
        slideOutHorizontally(
            animationSpec = tween(260),
            targetOffsetX = { it }
        ) + fadeOut(animationSpec = tween(200))
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun Vava8BottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        tabs.forEach { tab ->
            val selected = route == tab.dest.route ||
                (tab.dest == Dest.Home && route?.startsWith("channel/") == true)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (route != tab.dest.route) {
                        navController.navigateTab(tab.dest.route)
                    }
                },
                icon = {
                    Icon(
                        if (selected) tab.selected else tab.unselected,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandBlue,
                    selectedTextColor = BrandBlue,
                    indicatorColor = BrandBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    onFontSizeChange: (ReadingFontSize) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNoImageModeChange: (Boolean) -> Unit
) {
    val repo = Vava8App.instance.repository
    val bar: @Composable () -> Unit = { Vava8BottomBar(navController) }

    NavHost(
        navController = navController,
        startDestination = Dest.Home.route,
        enterTransition = { tabEnter() },
        exitTransition = { tabExit() },
        popEnterTransition = { tabEnter() },
        popExitTransition = { tabExit() }
    ) {
        composable(Dest.Home.route) {
            TabSwipeContainer(
                onSwipeLeft = { navController.navigateTab(Dest.Discover.route) }
            ) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repo))
                HomeScreen(
                    viewModel = vm,
                    onOpenPost = { navController.navigate(Dest.Article.of(it)) },
                    onSearch = { navController.navigate(Dest.Search.route) },
                    onOpenFavorites = {
                        if (!repo.user.value.isLoggedIn) {
                            navController.navigate(Dest.Login.route)
                        } else {
                            navController.navigate(
                                Dest.WebInfo.of("我的收藏", PersonalCenterUrls.MY_FAVORITES)
                            )
                        }
                    },
                    bottomBar = bar
                )
            }
        }
        composable(
            Dest.ChannelFeed.route,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val name = URLDecoder.decode(
                entry.arguments?.getString("name").orEmpty(),
                StandardCharsets.UTF_8.toString()
            )
            val vm: HomeViewModel = viewModel(
                key = "channel-$id",
                factory = HomeViewModel.factory(repo, id, name)
            )
            SwipeBackContainer(onBack = { navController.popBackStack() }) {
                HomeScreen(
                    viewModel = vm,
                    onOpenPost = { navController.navigate(Dest.Article.of(it)) },
                    onSearch = { navController.navigate(Dest.Search.route) },
                    onOpenFavorites = {
                        if (!repo.user.value.isLoggedIn) {
                            navController.navigate(Dest.Login.route)
                        } else {
                            navController.navigate(
                                Dest.WebInfo.of("我的收藏", PersonalCenterUrls.MY_FAVORITES)
                            )
                        }
                    },
                    onBack = { navController.popBackStack() },
                    bottomBar = bar
                )
            }
        }
        composable(Dest.Discover.route) {
            TabSwipeContainer(
                onSwipeLeft = { navController.navigateTab(Dest.Create.route) },
                onSwipeRight = { navController.navigateTab(Dest.Home.route) }
            ) {
                val vm: DiscoverViewModel = viewModel(factory = DiscoverViewModel.factory(repo))
                DiscoverScreen(
                    viewModel = vm,
                    onOpenChannel = { id, name ->
                        navController.navigate(Dest.ChannelFeed.of(id, name))
                    },
                    onOpenPost = { navController.navigate(Dest.Article.of(it)) },
                    onAllChannels = { navController.navigate(Dest.Channels.route) },
                    bottomBar = bar
                )
            }
        }
        composable(Dest.Create.route) {
            TabSwipeContainer(
                onSwipeLeft = { navController.navigateTab(Dest.Profile.route) },
                onSwipeRight = { navController.navigateTab(Dest.Discover.route) }
            ) {
                CreateScreen(
                    onLoginRequired = { navController.navigate(Dest.Login.route) },
                    onCreated = {
                        navController.navigateTab(Dest.Home.route)
                    },
                    bottomBar = bar
                )
            }
        }
        composable(Dest.Profile.route) {
            TabSwipeContainer(
                onSwipeRight = { navController.navigateTab(Dest.Create.route) }
            ) {
                ProfileScreen(
                    onLogin = { navController.navigate(Dest.Login.route) },
                    onRegister = { navController.navigate(Dest.Register.route) },
                    onOpenWebInfo = { title, url ->
                        navController.navigate(Dest.WebInfo.of(title, url))
                    },
                    onOpenAboutApp = { navController.navigate(Dest.AboutApp.route) },
                    onOpenBrowseHistory = {
                        navController.navigate(Dest.BrowseHistory.route)
                    },
                    onFontSizeChange = onFontSizeChange,
                    onThemeModeChange = onThemeModeChange,
                    onNoImageModeChange = onNoImageModeChange,
                    bottomBar = bar
                )
            }
        }
        composable(Dest.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenPost = { navController.navigate(Dest.Article.of(it)) }
            )
        }
        composable(Dest.AboutApp.route) {
            AboutAppScreen(onBack = { navController.popBackStack() })
        }
        composable(Dest.BrowseHistory.route) {
            BrowseHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenPost = { navController.navigate(Dest.Article.of(it)) }
            )
        }
        composable(Dest.Channels.route) {
            ChannelsScreen(
                onBack = { navController.popBackStack() },
                onOpenChannel = { id, name ->
                    // 保留「全部频道」在返回栈中，板块列表返回时回到全部频道
                    navController.navigate(Dest.ChannelFeed.of(id, name))
                }
            )
        }
        composable(Dest.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                onGoRegister = { navController.navigate(Dest.Register.route) }
            )
        }
        composable(Dest.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Dest.Login.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Dest.Article.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            ArticleDetailScreen(
                articleId = id,
                onBack = { navController.popBackStack() },
                onLoginRequired = { navController.navigate(Dest.Login.route) },
                onOpenChannel = { cid, name ->
                    navController.navigate(Dest.ChannelFeed.of(cid, name))
                },
                onFontSizeChange = onFontSizeChange,
                onThemeModeChange = onThemeModeChange
            )
        }
        composable(
            Dest.WebInfo.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { entry ->
            val title = URLDecoder.decode(
                entry.arguments?.getString("title").orEmpty(),
                StandardCharsets.UTF_8.toString()
            )
            val url = URLDecoder.decode(
                entry.arguments?.getString("url").orEmpty(),
                StandardCharsets.UTF_8.toString()
            )
            WebInfoScreen(title = title, url = url, onBack = { navController.popBackStack() })
        }
    }
}
