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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nonoka.nhentai.R
import com.nonoka.nhentai.feature.collection.CollectionPage
import com.nonoka.nhentai.feature.home.HomePage
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
    private val galleryCrawler = WebDataCrawler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crawlerMap[ClientType.Gallery] = galleryCrawler
        setContent {
            val navController = rememberNavController()
            NHentaiTheme {
                Scaffold(
                    bottomBar = {
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
                            composable(Tab.Home.id) {
                                HomePage()
                            }
                            composable(Tab.Collection.id) {
                                CollectionPage()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        crawlerMap.remove(ClientType.Gallery)
        galleryCrawler.clearRequester()
    }

    private fun getIconRes(tab: Tab): Int {
        return when (tab) {
            Tab.Home -> R.drawable.ic_home_solid_24dp
            Tab.Collection -> R.drawable.ic_collection_solid_24dp
        }
    }

    @Composable
    @SuppressLint("SetJavaScriptEnabled")
    private fun WebViews() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = galleryCrawler
                        settings.javaScriptEnabled = true
                        galleryCrawler.registerRequester(this::loadUrl)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    companion object {
        fun start(fromContext: Context) {
            fromContext.startActivity(Intent(fromContext, MainActivity::class.java))
        }
    }
}

enum class Tab(val id: String) {
    Home("Home"), Collection("collection")
}