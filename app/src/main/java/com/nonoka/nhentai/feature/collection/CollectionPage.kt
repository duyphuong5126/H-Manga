package com.nonoka.nhentai.feature.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nonoka.nhentai.R
import com.nonoka.nhentai.feature.DoujinshiStatusViewModel
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.shared.LoadingDialog
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.extraNormalSpace
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.headlineLargeStyle
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun CollectionPage(
    onDoujinshiSelected: (String) -> Unit = {},
    collectionViewModel: CollectionViewModel,
    doujinshiStatusViewModel: DoujinshiStatusViewModel,
) {
    val coroutineContext = rememberCoroutineScope()

    val galleryState = rememberLazyStaggeredGridState()

    LaunchedEffect(Unit) {
        collectionViewModel.loadMore()
        doujinshiStatusViewModel.reload()
    }

    val onRefreshGallery: () -> Unit = {
        Timber.d("Collection>>> onRefreshGallery")
        coroutineContext.launch {
            galleryState.scrollToItem(0)
        }
        collectionViewModel.resetList()
    }

    val loadingState by collectionViewModel.loadingState
    if (loadingState is LoadingUiState.Loading) {
        Timber.d("Collection>>> Loading dialog: show")
        LoadingDialog(message = (loadingState as LoadingUiState.Loading).message)
    } else {
        Timber.d("Collection>>> Loading dialog: not show")
    }

    /*val reset by collectionViewModel.reset
    if (reset) {
        Timber.d("Test>>> reset")
        collectionViewModel.reset.value = false
        onRefreshGallery()
    }*/

    Scaffold(
        topBar = {
            Header(onRefreshGallery, collectionViewModel = collectionViewModel)
        },
        containerColor = Black
    ) {
        Gallery(
            collectionViewModel,
            it,
            galleryState,
            onDoujinshiSelected,
            doujinshiStatusViewModel
        )
    }
}

@Composable
private fun Gallery(
    viewModel: CollectionViewModel,
    paddingValues: PaddingValues,
    galleryState: LazyStaggeredGridState,
    onDoujinshiSelected: (String) -> Unit,
    doujinshiStatusViewModel: DoujinshiStatusViewModel,
) {
    val lazyDoujinshis = viewModel.galleryItems
    val loadingState by viewModel.loadingState
    val firstVisibleIndex = galleryState.firstVisibleItemIndex
    val itemCount = lazyDoujinshis.count { it is GalleryUiState.DoujinshiItem }
    Timber.d("Collection>>> itemCount=$itemCount, canScrollForward=${galleryState.canScrollForward}")
    if (itemCount > 0 && (firstVisibleIndex >= itemCount - 5 || !galleryState.canScrollForward)) {
        viewModel.loadMore()
    }
    if (lazyDoujinshis.size > 0) {
        val favoriteIds by remember {
            doujinshiStatusViewModel.favoriteIds
        }
        val downloadedIds by remember {
            doujinshiStatusViewModel.downloadedIds
        }
        val readIds by remember {
            doujinshiStatusViewModel.readIds
        }
        Timber.d("Collection>>> favoriteIds=$favoriteIds\ndownloadedIds=$downloadedIds\nreadIds=$readIds")

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = smallSpace, end = smallSpace)
                .fillMaxSize(),
            state = galleryState,
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            verticalItemSpacing = smallSpace,
            horizontalArrangement = Arrangement.spacedBy(smallSpace),
            content = {
                items(
                    count = lazyDoujinshis.size,
                    key = { index ->
                        when (val item = lazyDoujinshis[index]) {
                            is GalleryUiState.Title -> item.title
                            is GalleryUiState.DoujinshiItem -> item.doujinshi.id
                        }
                    },
                    span = { index ->
                        when (lazyDoujinshis[index]) {
                            is GalleryUiState.Title -> StaggeredGridItemSpan.FullLine
                            is GalleryUiState.DoujinshiItem -> StaggeredGridItemSpan.SingleLane
                        }
                    },
                ) { index ->
                    when (val item = lazyDoujinshis[index]) {
                        is GalleryUiState.Title -> GalleryTitle(item)
                        is GalleryUiState.DoujinshiItem -> {
                            Timber.d("Collection>>> doujin title=${item.doujinshi.title.prettyName}")
                            DoujinshiCard(
                                item,
                                onDoujinshiSelected = onDoujinshiSelected,
                                isFavorite = favoriteIds.contains(item.doujinshi.id),
                                isDownloaded = downloadedIds.contains(item.doujinshi.id),
                                isRead = readIds.contains(item.doujinshi.id)
                            )
                        }
                    }
                }
            },
        )
    } else if (loadingState is LoadingUiState.Loading) {
        LoadingDialog(message = "Loading, please wait.")
    } else {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.mipmap.ic_nothing_here_grey),
            contentDescription = "No data loaded",
        )
    }
}

@Composable
private fun GalleryTitle(title: GalleryUiState.Title) {
    Timber.d("Collection>>> title=${title.title}")
    Text(
        text = title.title,
        style = MaterialTheme.typography.bodyNormalBold.copy(color = White),
        modifier = Modifier.padding(start = smallSpace, top = normalSpace, bottom = smallSpace),
    )
}

@Composable
private fun Header(
    onRefreshGallery: () -> Unit,
    modifier: Modifier = Modifier,
    collectionViewModel: CollectionViewModel
) {
    Box(
        modifier = modifier
            .height(headerHeight)
            .background(Grey31)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    top = mediumSpace,
                    bottom = mediumSpace,
                    end = extraNormalSpace,
                    start = normalSpace
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nhentai_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(14.dp)
                    .clickable {
                        onRefreshGallery()
                    },
            )

            val countLabel by remember {
                collectionViewModel.collectionCountLabel
            }
            Text(
                text = "Collection${if (countLabel.isNotBlank()) " ($countLabel)" else ""}",
                style = MaterialTheme.typography.headlineLargeStyle.copy(color = White),
                modifier = Modifier.padding(start = normalSpace)
            )
        }
    }
}