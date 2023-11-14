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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nonoka.nhentai.R
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.shared.LoadingDialog
import com.nonoka.nhentai.ui.shared.LoadingDialogContent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionPage(
    onDoujinshiSelected: (String) -> Unit = {},
    collectionViewModel: CollectionViewModel = hiltViewModel(),
) {
    val coroutineContext = rememberCoroutineScope()
    val lazyDoujinshis = collectionViewModel.collectionFlow.collectAsLazyPagingItems()

    val galleryState = rememberLazyStaggeredGridState()

    val onRefreshGallery: () -> Unit = {
        collectionViewModel.loadingState.value = LoadingUiState.Loading("Refreshing")
        lazyDoujinshis.refresh()
        coroutineContext.launch {
            galleryState.scrollToItem(0)
        }
    }

    val loadingState by collectionViewModel.loadingState
    if (loadingState is LoadingUiState.Loading) {
        Timber.d("Test>>> Loading dialog: show")
        LoadingDialog(message = (loadingState as LoadingUiState.Loading).message)
    } else {
        Timber.d("Test>>> Loading dialog: not show")
    }

    Scaffold(
        topBar = {
            Header(onRefreshGallery)
        },
        containerColor = Black
    ) {
        Gallery(
            it,
            lazyDoujinshis,
            galleryState,
            loadingState,
            onDoujinshiSelected
        )
    }
}

@Composable
private fun Gallery(
    paddingValues: PaddingValues,
    lazyDoujinshis: LazyPagingItems<GalleryUiState>,
    galleryState: LazyStaggeredGridState,
    loadingState: LoadingUiState,
    onDoujinshiSelected: (String) -> Unit,
) {
    if (lazyDoujinshis.itemCount > 0) {
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
                val realCount =
                    lazyDoujinshis.itemCount + if (loadingState is LoadingUiState.Loading) 1 else 0
                items(
                    count = realCount,
                    key = { index ->
                        when {
                            realCount > lazyDoujinshis.itemCount && index == realCount - 1 -> "Loading footer"

                            else -> {
                                when (val item = lazyDoujinshis[index] as GalleryUiState) {
                                    is GalleryUiState.Title -> item.title
                                    is GalleryUiState.DoujinshiItem -> item.doujinshi.id
                                }
                            }
                        }
                    },
                    span = { index ->
                        if (realCount > lazyDoujinshis.itemCount && index == realCount - 1) {
                            StaggeredGridItemSpan.FullLine
                        } else {
                            when (lazyDoujinshis[index] as GalleryUiState) {
                                is GalleryUiState.Title -> StaggeredGridItemSpan.FullLine
                                is GalleryUiState.DoujinshiItem -> StaggeredGridItemSpan.SingleLane
                            }
                        }
                    },
                ) { index ->
                    when {
                        realCount > lazyDoujinshis.itemCount && index == realCount - 1 -> {
                            LoadingDialogContent(
                                modifier = Modifier.padding(bottom = mediumSpace),
                                message = "Loading, please wait."
                            )
                        }

                        else -> {
                            when (val item = lazyDoujinshis[index] as GalleryUiState) {
                                is GalleryUiState.Title -> GalleryTitle(item)
                                is GalleryUiState.DoujinshiItem -> DoujinshiCard(
                                    item,
                                    onDoujinshiSelected = onDoujinshiSelected,
                                )
                            }
                        }
                    }
                }
            },
        )
    } else if (lazyDoujinshis.loadState.refresh == LoadState.Loading) {
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
    collectionViewModel: CollectionViewModel = hiltViewModel()
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