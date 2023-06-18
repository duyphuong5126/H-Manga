package com.nonoka.nhentai.feature

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.TAG
import com.nonoka.nhentai.feature.collection.CollectionPage
import com.nonoka.nhentai.feature.doujinshi_page.DoujinshiPage
import com.nonoka.nhentai.feature.home.HomePage
import com.nonoka.nhentai.feature.reader.ReaderPage
import com.nonoka.nhentai.feature.recommendation.RecommendationPage
import com.nonoka.nhentai.helper.ClientType
import com.nonoka.nhentai.helper.WebDataCrawler
import com.nonoka.nhentai.helper.crawlerMap
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.NHentaiTheme
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.normalIconSize
import com.nonoka.nhentai.ui.theme.smallSpace
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NHentaiTheme {
                Scaffold(
                    bottomBar = {
                        if (Tab.isTabValid(currentRoute(navController).orEmpty())) {
                            BottomNavigation(
                                backgroundColor = Grey31,
                                elevation = 0.dp,
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination

                                Tab.values().forEach { tab ->
                                    val isSelected = currentDestination?.route == tab.id
                                    BottomNavigationItem(
                                        selected = isSelected,
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = getIconRes(tab)),
                                                contentDescription = tab.id,
                                                modifier = Modifier
                                                    .size(normalIconSize)
                                                    .padding(bottom = smallSpace),
                                                tint = if (isSelected) MainColor else White
                                            )
                                        },
                                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = ContentAlpha.disabled
                                        ),
                                        onClick = {
                                            navController.navigate(tab.id) {
                                                // Pop up to the start destination of the graph to
                                                // avoid building up a large stack of destinations
                                                // on the back stack as users select items
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                // Avoid multiple copies of the same destination when
                                                // re-selecting the same item
                                                launchSingleTop = true
                                                // Restore state when re-selecting a previously selected item
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                    ) {
                        WebViews()

                        NavHost(
                            navController = navController,
                            startDestination = Tab.Home.id,
                        ) {
                            composable(Tab.Home.id) { backStackEntry ->
                                HomePage(
                                    selectedTag = backStackEntry.savedStateHandle[TAG],
                                    onDoujinshiSelected = { id ->
                                        val route = "doujinshiPage/$id"
                                        navController.navigate(route) {
                                            popUpTo(route)
                                        }
                                    },
                                )
                            }

                            composable(Tab.Collection.id) {
                                CollectionPage(
                                    onDoujinshiSelected = { id ->
                                        val route = "doujinshiPage/$id"
                                        navController.navigate(route) {
                                            popUpTo(route)
                                        }
                                    },
                                )
                            }

                            composable(Tab.Recommendation.id) {
                                RecommendationPage()
                            }

                            composable(
                                "doujinshiPage/{doujinshiId}",
                                arguments = listOf(navArgument("doujinshiId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                backStackEntry.arguments?.getString("doujinshiId")?.let { id ->
                                    DoujinshiPage(
                                        doujinshiId = id,
                                        startReading = { doujinshiId, index ->
                                            navController.navigate("readerPage/$doujinshiId?pageIndex=$index")
                                        },
                                        onTagSelected = { tag ->
                                            navController
                                                .previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set(TAG, tag)
                                            navController.popBackStack()
                                        },
                                        onBackPressed = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            composable(
                                "readerPage/{doujinshiId}?pageIndex={pageIndex}",
                                arguments = listOf(
                                    navArgument("doujinshiId") {
                                        type = NavType.StringType
                                    },
                                    navArgument("pageIndex") {
                                        type = NavType.IntType
                                    },
                                )
                            ) { backStackEntry ->
                                backStackEntry.arguments?.getString("doujinshiId")?.let { id ->
                                    ReaderPage(
                                        doujinshiId = id,
                                        startIndex = backStackEntry.arguments?.getInt("pageIndex")
                                            ?: -1,
                                        onBackPressed = {
                                            navController.popBackStack()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        crawlerMap[ClientType.Gallery]?.clearRequester()
        crawlerMap[ClientType.Detail]?.clearRequester()
        crawlerMap[ClientType.Recommendation]?.clearRequester()
        crawlerMap[ClientType.Comment]?.clearRequester()
    }

    private fun getIconRes(tab: Tab): Int {
        return when (tab) {
            Tab.Home -> R.drawable.ic_home_solid_24dp
            Tab.Collection -> R.drawable.ic_collection_solid_24dp
            Tab.Recommendation -> R.drawable.ic_recommendation_solid_24dp
        }
    }

    @Composable
    private fun WebViews() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            crawlerMap.values.forEach {
                WebView(dataCrawler = it)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun WebView(dataCrawler: WebDataCrawler) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = dataCrawler
                    settings.javaScriptEnabled = true
                    dataCrawler.registerRequester(this::loadUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    companion object {
        fun start(fromContext: Context) {
            fromContext.startActivity(Intent(fromContext, MainActivity::class.java))
        }
    }
}

enum class Tab(val id: String) {
    Home("Home"),
    Collection("Collection"),
    Recommendation("Recommendation");

    companion object {
        fun isTabValid(tabId: String): Boolean = values().any {
            it.id == tabId
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}