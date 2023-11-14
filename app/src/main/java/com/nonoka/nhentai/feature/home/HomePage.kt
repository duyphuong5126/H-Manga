package com.nonoka.nhentai.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.doujinshi.SortOption
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.shared.LoadingDialog
import com.nonoka.nhentai.ui.shared.LoadingDialogContent
import com.nonoka.nhentai.ui.shared.YesNoDialog
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.Grey400
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.bodyNormalRegular
import com.nonoka.nhentai.ui.theme.extraNormalSpace
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalIconSize
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    selectedTag: String? = null,
    onDoujinshiSelected: (String) -> Unit = {},
    onSelectedTagApplied: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val coroutineContext = rememberCoroutineScope()
    val lazyDoujinshis = homeViewModel.lazyDoujinshisFlow.collectAsLazyPagingItems()

    val galleryState = rememberLazyStaggeredGridState()
    val onRefreshGallery: (String) -> Unit = {
        homeViewModel.loadingState.value = LoadingUiState.Loading(it)
        coroutineContext.launch {
            galleryState.scrollToItem(0)
        }
        lazyDoujinshis.refresh()
    }
    val loadingState by homeViewModel.loadingState
    if (loadingState is LoadingUiState.Loading) {
        Timber.d("Test>>> Loading dialog: show")
        LoadingDialog(message = (loadingState as LoadingUiState.Loading).message)
    } else {
        Timber.d("Test>>> Loading dialog: not show")
    }
    val reset by homeViewModel.reset
    if (reset) {
        onRefreshGallery("Refreshing")
        homeViewModel.reset.value = false
    }

    Scaffold(
        topBar = {
            Header(onRefreshGallery, onDoujinshiSelected)
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

    Timber.d("Gallery>>> rebuild with selectedTag=$selectedTag")
    LaunchedEffect(
        key1 = selectedTag,
        block = {
            if (!selectedTag.isNullOrBlank()) {
                homeViewModel.loadingState.value = LoadingUiState.Loading("Searching")
                coroutineContext.launch {
                    delay(1000)
                    homeViewModel.addFilter(selectedTag)
                    onRefreshGallery("Searching")
                    onSelectedTagApplied()
                }
            }
        },
    )

    val randomDoujinshi by remember {
        homeViewModel.randomDoujinshi
    }
    LaunchedEffect(key1 = randomDoujinshi, block = {
        if (randomDoujinshi != null) {
            onDoujinshiSelected(randomDoujinshi!!.id)
            homeViewModel.randomDoujinshi.value = null
        }
    })
}

@Composable
private fun Gallery(
    lazyDoujinshis: LazyPagingItems<GalleryUiState>,
    galleryState: LazyStaggeredGridState,
    onRefreshGallery: (String) -> Unit,
    onDoujinshiSelected: (String) -> Unit,
) {
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
                    contentType = { index ->
                        when {
                            index == 0 -> 0
                            index <= lazyDoujinshis.itemCount -> 1
                            else -> 2
                        }
                    },
                    key = { index ->
                        when {
                            index == 0 -> "Scrollable header"
                            index <= lazyDoujinshis.itemCount -> {
                                val galleryIndex = index - 1
                                when (val item = lazyDoujinshis[galleryIndex] as GalleryUiState) {
                                    is GalleryUiState.Title -> item.title
                                    is GalleryUiState.DoujinshiItem -> item.doujinshi.id
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
                            if (!lazyDoujinshis.loadState.prepend.endOfPaginationReached && !lazyDoujinshis.loadState.append.endOfPaginationReached) {
                                LoadingDialogContent(
                                    modifier = Modifier.padding(bottom = mediumSpace),
                                    message = "Loading, please wait."
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
        Column {
            GalleryHeader(onRefreshGallery)

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.mipmap.ic_nothing_here_grey),
                contentDescription = "No data loaded",
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Header(
    onRefreshGallery: (String) -> Unit,
    onDoujinshiSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val resetRequestId = remember {
        mutableStateOf<Long?>(null)
    }
    if (resetRequestId.value != null) {
        YesNoDialog(
            title = "Reset Gallery",
            description = "Do you want to clear all filters?",
            onDismiss = {
                resetRequestId.value = null
            },
            onAnswerYes = {
                homeViewModel.clearFilters()
                onRefreshGallery("Refreshing")
            }
        )
    }

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
                        resetRequestId.value = System.currentTimeMillis()
                    },
            )

            Box(
                modifier = Modifier
                    .padding(start = normalSpace)
                    .fillMaxHeight()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = mediumRadius, bottomStart = mediumRadius))
                    .background(White)
                    .padding(start = mediumSpace, end = mediumSpace, top = 5.dp)
            ) {
                val blankSearchTerm = homeViewModel.searchTerm.value.isNotBlank()
                var dropdownExpanded by remember(key1 = homeViewModel.searchTerm.value) {
                    mutableStateOf(
                        blankSearchTerm
                    )
                }
                Timber.d("Dropdown>>> init dropdown expanded=$dropdownExpanded, search term=${homeViewModel.searchTerm.value}")
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = {
                        Timber.d("Dropdown>>> onExpandedChange=$dropdownExpanded")
                        dropdownExpanded = it
                    },

                    ) {
                    BasicTextField(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = homeViewModel.searchTerm.value,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                keyboardController?.hide()
                                if (homeViewModel.searchTerm.value.toLongOrNull() != null) {
                                    onDoujinshiSelected(homeViewModel.searchTerm.value)
                                } else {
                                    homeViewModel.addFilter(homeViewModel.searchTerm.value)
                                    onRefreshGallery("Searching")
                                }
                                homeViewModel.searchTerm.value = ""
                            },
                        ),
                        onValueChange = { newText ->
                            homeViewModel.searchTerm.value = newText
                        },
                        maxLines = 1,
                        decorationBox = { innerTextField ->
                            if (homeViewModel.searchTerm.value.isBlank()) {
                                Text(
                                    text = "e.g. tag:\"lolicon\" pages:>9 -bald",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Grey400),
                                )
                            }
                            innerTextField()
                        },
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // filter options based on text field value (i.e. crude autocomplete)
                    val selectedOption = homeViewModel.searchTerm.value.trim().lowercase()
                    val filterOptions = if (selectedOption.isNotBlank())
                        homeViewModel.filterHistory.filter { it.contains(selectedOption) } else emptyList()
                    if (filterOptions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            modifier = Modifier
                                .background(White)
                                .exposedDropdownSize(matchTextFieldWidth = true),
                            expanded = dropdownExpanded,
                            onDismissRequest = {
                                Timber.d("Dropdown>>> onDismissRequest=$dropdownExpanded")
                                dropdownExpanded = false
                            },
                        ) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option,
                                            style = MaterialTheme.typography.bodyNormalRegular,
                                        )
                                    },
                                    onClick = {
                                        Timber.d("Dropdown>>> onClick=$dropdownExpanded")
                                        keyboardController?.hide()
                                        homeViewModel.addFilter(option)
                                        onRefreshGallery("Searching")
                                        homeViewModel.searchTerm.value = ""
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val randomRequestId = remember {
                mutableStateOf<Long?>(null)
            }
            if (randomRequestId.value != null) {
                YesNoDialog(
                    title = "Randomize Doujinshis",
                    description = "Do you want to open a random doujinshi?",
                    onDismiss = {
                        randomRequestId.value = null
                    },
                    onAnswerYes = {
                        randomRequestId.value = null
                        homeViewModel.openRandomDoujinshi()
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
                onClick = {
                    randomRequestId.value = System.currentTimeMillis()
                }
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
        modifier = Modifier.padding(start = smallSpace, bottom = smallSpace),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryHeader(
    onRefreshGallery: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    if (homeViewModel.filters.isNotEmpty() || homeViewModel.galleryCountLabel.value.isNotBlank()) {
        FlowRow(modifier = Modifier.padding(mediumSpace)) {
            val hasFilter = homeViewModel.filters.isNotEmpty()
            if (hasFilter) {
                val deleteFilterHolder = remember {
                    mutableStateOf<String?>(null)
                }
                val deleteFilter = deleteFilterHolder.value
                if (deleteFilter != null) {
                    YesNoDialog(
                        title = "Delete filter",
                        description = "Do you want to delete the filter \"$deleteFilter\"?",
                        onDismiss = {
                            deleteFilterHolder.value = null
                        },
                        onAnswerYes = {
                            homeViewModel.removeFilter(deleteFilter)
                            onRefreshGallery("Refreshing")
                        }
                    )
                }
                homeViewModel.filters.forEach {
                    Row(
                        modifier = Modifier
                            .padding(end = smallSpace, top = smallSpace)
                            .clip(RoundedCornerShape(mediumRadius))
                            .background(if (it.startsWith("-")) Grey31 else MainColor)
                            .padding(horizontal = mediumSpace, vertical = smallSpace),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium.copy(White),
                            textAlign = TextAlign.Center
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_close_solid_24dp),
                            contentDescription = "Remove $it filter",
                            modifier = Modifier
                                .padding(top = tinySpace)
                                .size(normalIconSize)
                                .padding(bottom = smallSpace)
                                .clickable {
                                    deleteFilterHolder.value = it
                                },
                            tint = White
                        )
                    }
                }

                SortOptions(onRefreshGallery)
            }

            if (homeViewModel.galleryCountLabel.value.isNotBlank()) {
                Text(
                    modifier = if (hasFilter) Modifier
                        .padding(top = normalSpace)
                        .fillMaxSize() else Modifier.fillMaxSize(),
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
    onRefreshGallery: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val selectedOption = homeViewModel.sortOption.value
    LazyRow(
        modifier = Modifier.padding(top = smallSpace)
    ) {
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
                    onRefreshGallery("Refreshing")
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
                    onRefreshGallery("Refreshing")
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
                    onRefreshGallery("Refreshing")
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
                    onRefreshGallery("Refreshing")
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

