package com.nonoka.nhentai.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.book.SortOption
import com.nonoka.nhentai.paging.PagingDataSource
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.shared.LoadingDialog
import com.nonoka.nhentai.ui.shared.LoadingDialogContent
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.Grey400
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.extraNormalSpace
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    onDoujinshiSelected: (String) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val coroutineContext = rememberCoroutineScope()
    val lazyDoujinshis = remember {
        Pager(
            PagingConfig(
                pageSize = 25,
                prefetchDistance = 5,
                initialLoadSize = 25,
            )
        ) {
            PagingDataSource(homeViewModel)
        }.flow
    }.collectAsLazyPagingItems()

    val galleryState = rememberLazyStaggeredGridState()
    val onRefreshGallery: () -> Unit = {
        lazyDoujinshis.refresh()
        homeViewModel.loadingState.value = LoadingUiState.Loading("Refreshing")
        coroutineContext.launch {
            galleryState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            Header(onRefreshGallery)
        },
        containerColor = Black
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Gallery(lazyDoujinshis, galleryState, onRefreshGallery, onDoujinshiSelected)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(
    lazyDoujinshis: LazyPagingItems<GalleryUiState>,
    galleryState: LazyStaggeredGridState,
    onRefreshGallery: () -> Unit,
    onDoujinshiSelected: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val loadingState by remember {
        viewModel.loadingState
    }
    if (lazyDoujinshis.itemCount > 0) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .padding(start = smallSpace, end = smallSpace)
                .fillMaxSize(),
            state = galleryState,
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            verticalItemSpacing = smallSpace,
            horizontalArrangement = Arrangement.spacedBy(smallSpace),
            content = {
                items(
                    count = lazyDoujinshis.itemCount + 2,
                    key = { index ->
                        when {
                            index == 0 -> "Scrollable header"
                            index <= lazyDoujinshis.itemCount -> {
                                val galleryIndex = index - 1
                                when (val item = lazyDoujinshis[galleryIndex] as GalleryUiState) {
                                    is GalleryUiState.Title -> item.title
                                    is GalleryUiState.DoujinshiItem -> item.doujinshi.bookId
                                }
                            }

                            else -> "Loading footer"
                        }
                    },
                    span = { index ->
                        if (index > 0 && index <= lazyDoujinshis.itemCount) {
                            val galleryIndex = index - 1
                            when (lazyDoujinshis[galleryIndex] as GalleryUiState) {
                                is GalleryUiState.Title -> StaggeredGridItemSpan.FullLine
                                is GalleryUiState.DoujinshiItem -> StaggeredGridItemSpan.SingleLane
                            }
                        } else {
                            StaggeredGridItemSpan.FullLine
                        }
                    },
                ) { index ->
                    when {
                        index == 0 -> GalleryHeader(onRefreshGallery)
                        index <= lazyDoujinshis.itemCount -> {
                            val galleryIndex = index - 1
                            when (val item = lazyDoujinshis[galleryIndex] as GalleryUiState) {
                                is GalleryUiState.Title -> GalleryTitle(item)
                                is GalleryUiState.DoujinshiItem -> DoujinshiCard(
                                    item,
                                    onDoujinshiSelected = onDoujinshiSelected,
                                )
                            }
                        }

                        else -> {
                            LoadingDialogContent(
                                modifier = Modifier.padding(bottom = mediumSpace),
                                message = "Loading, please wait."
                            )
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
    if (loadingState is LoadingUiState.Loading) {
        LoadingDialog(message = (loadingState as LoadingUiState.Loading).message)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Header(
    onRefreshGallery: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                modifier = Modifier.height(14.dp),
            )

            Box(
                modifier = Modifier
                    .padding(start = normalSpace)
                    .fillMaxHeight()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = mediumRadius, bottomStart = mediumRadius))
                    .background(White)
                    .padding(horizontal = mediumSpace, vertical = smallSpace)
            ) {
                BasicTextField(
                    modifier = Modifier.align(Alignment.CenterStart),
                    value = searchText,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        homeViewModel.addFilter(searchText)
                        searchText = ""
                        keyboardController?.hide()
                        onRefreshGallery()
                    }),
                    onValueChange = { newText ->
                        searchText = newText
                    },
                    maxLines = 1,
                    decorationBox = { innerTextField ->
                        if (searchText.isBlank()) {
                            Text(
                                text = "e.g. tag:\"big breasts\" pages:>15 -milf",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Grey400)
                            )
                        }
                        innerTextField()
                    }
                )
            }
            TextButton(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .wrapContentSize(),
                shape = RoundedCornerShape(topEnd = mediumRadius, bottomEnd = mediumRadius),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MainColor,
                ),
                onClick = {}
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_random_black_24dp),
                    contentDescription = "Randomize doujinshis",
                    colorFilter = ColorFilter.tint(White),
                )
            }
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryHeader(
    onRefreshGallery: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    if (homeViewModel.filters.isNotEmpty() || homeViewModel.galleryCountLabel.value.isNotBlank()) {
        FlowRow(modifier = Modifier.padding(mediumSpace)) {
            if (homeViewModel.filters.isNotEmpty()) {
                homeViewModel.filters.forEach {
                    Text(
                        text = it,
                        modifier = Modifier
                            .padding(end = smallSpace, top = smallSpace)
                            .clip(RoundedCornerShape(mediumRadius))
                            .background(MainColor)
                            .padding(horizontal = mediumSpace, vertical = smallSpace),
                        style = MaterialTheme.typography.bodyMedium.copy(White),
                        textAlign = TextAlign.Center
                    )
                }
            }

            SortOptions(onRefreshGallery)

            if (homeViewModel.galleryCountLabel.value.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .padding(top = normalSpace)
                        .fillMaxSize(),
                    text = homeViewModel.galleryCountLabel.value,
                    style = MaterialTheme.typography.bodyNormalBold.copy(color = White),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SortOptions(
    onRefreshGallery: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val selectedOption = homeViewModel.sortOption.value
    LazyRow {
        item {
            TextButton(
                modifier = Modifier
                    .padding(end = smallSpace),
                shape = RoundedCornerShape(mediumRadius),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (selectedOption == SortOption.Recent) Grey77 else Grey31,
                    contentColor = White
                ),
                onClick = {
                    homeViewModel.selectSortOption(SortOption.Recent)
                    onRefreshGallery()
                }
            ) {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.bodyNormalBold
                )
            }
        }

        item {
            TextButton(
                modifier = Modifier
                    .padding(end = 1.dp),
                shape = RoundedCornerShape(
                    topStart = mediumRadius,
                    bottomStart = mediumRadius
                ),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Grey31,
                    contentColor = White
                ),
                onClick = {},
            ) {
                Text(
                    text = "Popular:",
                    style = MaterialTheme.typography.bodyNormalBold
                )
            }
        }

        item {
            TextButton(
                modifier = Modifier
                    .padding(end = 1.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (selectedOption == SortOption.PopularToday) Grey77 else Grey31,
                    contentColor = White
                ),
                onClick = {
                    homeViewModel.selectSortOption(SortOption.PopularToday)
                    onRefreshGallery()
                }
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.bodyNormalBold
                )
            }
        }

        item {
            TextButton(
                modifier = Modifier
                    .padding(end = 1.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (selectedOption == SortOption.PopularWeek) Grey77 else Grey31,
                    contentColor = White
                ),
                onClick = {
                    homeViewModel.selectSortOption(SortOption.PopularWeek)
                    onRefreshGallery()
                }
            ) {
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.bodyNormalBold
                )
            }
        }

        item {
            TextButton(
                shape = RoundedCornerShape(
                    topEnd = mediumRadius,
                    bottomEnd = mediumRadius
                ),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (selectedOption == SortOption.PopularAllTime) Grey77 else Grey31,
                    contentColor = White
                ),
                onClick = {
                    homeViewModel.selectSortOption(SortOption.PopularAllTime)
                    onRefreshGallery()
                }
            ) {
                Text(
                    text = "All time",
                    style = MaterialTheme.typography.bodyNormalBold
                )
            }
        }
    }
}

