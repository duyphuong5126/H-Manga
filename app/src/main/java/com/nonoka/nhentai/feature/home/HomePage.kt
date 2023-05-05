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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.nonoka.nhentai.R
import com.nonoka.nhentai.paging.PagingDataSource
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.Grey400
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.extraNormalSpace
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    Scaffold(
        topBar = {
            Header()
        },
        containerColor = Black
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Gallery()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(homeViewModel: HomeViewModel = hiltViewModel()) {
    val lazyDoujinshis = remember {
        Pager(
            PagingConfig(
                pageSize = 25,
                prefetchDistance = 5,
            )
        ) {
            PagingDataSource(homeViewModel)
        }
    }.flow.collectAsLazyPagingItems()

    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .padding(start = smallSpace, end = smallSpace)
            .fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        verticalItemSpacing = smallSpace,
        horizontalArrangement = Arrangement.spacedBy(smallSpace),
        content = {
            if (lazyDoujinshis.itemCount > 0) {
                items(
                    count = lazyDoujinshis.itemCount + 2,
                    key = { index ->
                        when {
                            index == 0 -> "Scrollable header"
                            index < lazyDoujinshis.itemCount -> {
                                when (val item = lazyDoujinshis[index] as GalleryUiState) {
                                    is GalleryUiState.Title -> item.title
                                    is GalleryUiState.DoujinshiItem -> item.doujinshi.bookId
                                }
                            }

                            else -> "Loading footer"
                        }
                    },
                    span = { index ->
                        if (index > 0 && index < lazyDoujinshis.itemCount) {
                            when (lazyDoujinshis[index] as GalleryUiState) {
                                is GalleryUiState.Title -> StaggeredGridItemSpan.FullLine
                                is GalleryUiState.DoujinshiItem -> StaggeredGridItemSpan.SingleLane
                            }
                        } else {
                            StaggeredGridItemSpan.FullLine
                        }
                    },
                ) { index ->
                    when {
                        index == 0 -> GalleryHeader()
                        index < lazyDoujinshis.itemCount -> {
                            val galleryIndex = index - 1
                            when (val item = lazyDoujinshis[galleryIndex] as GalleryUiState) {
                                is GalleryUiState.Title -> GalleryTitle(item)
                                is GalleryUiState.DoujinshiItem -> DoujinshiCard(item)
                            }
                        }

                        else -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            } else {
                items(
                    count = 1,
                    span = {
                        StaggeredGridItemSpan.FullLine
                    },
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        },
    )
}

@Composable
private fun Header(modifier: Modifier = Modifier, homeViewModel: HomeViewModel = hiltViewModel()) {
    var searchText by remember { mutableStateOf("") }
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
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = mediumRadius, bottomEnd = mediumRadius))
                    .background(MainColor)
            )
        }
    }
}

@Composable
private fun GalleryTitle(title: GalleryUiState.Title) {
    Text(
        text = title.title,
        style = MaterialTheme.typography.bodyLarge.copy(color = White),
        modifier = Modifier.padding(start = smallSpace, top = normalSpace, bottom = mediumSpace),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryHeader(homeViewModel: HomeViewModel = hiltViewModel()) {
    if (homeViewModel.filters.isNotEmpty()) {
        FlowRow(modifier = Modifier.padding(mediumSpace)) {
            homeViewModel.filters.forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(end = smallSpace)
                        .clip(RoundedCornerShape(mediumRadius))
                        .background(MainColor)
                        .padding(horizontal = mediumSpace, vertical = smallSpace),
                    style = MaterialTheme.typography.bodyMedium.copy(White),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

