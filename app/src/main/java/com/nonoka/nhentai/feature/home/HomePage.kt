package com.nonoka.nhentai.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.CHINESE_LANG
import com.nonoka.nhentai.domain.entity.JAPANESE_LANG
import com.nonoka.nhentai.paging.PagingDataSource
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Black96
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.extraNormalSpace
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace

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
                    count = lazyDoujinshis.itemCount + 1,
                    key = { index ->
                        if (index < lazyDoujinshis.itemCount) {
                            when (val item = lazyDoujinshis[index] as GalleryUiState) {
                                is GalleryUiState.Title -> item.title
                                is GalleryUiState.DoujinshiItem -> item.doujinshi.bookId
                            }
                        } else {
                            "Loading footer"
                        }
                    },
                    span = { index ->
                        if (index < lazyDoujinshis.itemCount) {
                            when (lazyDoujinshis[index] as GalleryUiState) {
                                is GalleryUiState.Title -> StaggeredGridItemSpan.FullLine
                                is GalleryUiState.DoujinshiItem -> StaggeredGridItemSpan.SingleLane
                            }
                        } else {
                            StaggeredGridItemSpan.FullLine
                        }
                    },
                ) { index ->
                    if (index < lazyDoujinshis.itemCount) {
                        when (val item = lazyDoujinshis[index] as GalleryUiState) {
                            is GalleryUiState.Title -> Title(item)
                            is GalleryUiState.DoujinshiItem -> DoujinshiThumbnail(item)
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
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
private fun Header(modifier: Modifier = Modifier) {
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
                    onValueChange = { newText ->
                        searchText = newText
                    },
                    maxLines = 1,
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
private fun DoujinshiThumbnail(doujinshiItem: GalleryUiState.DoujinshiItem) {
    val doujinshi = doujinshiItem.doujinshi
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(doujinshi.thumbnailRatio)
            .clip(shape = RoundedCornerShape(size = mediumRadius))
    ) {
        AsyncImage(
            model = doujinshi.thumbnail,
            contentDescription = "Thumbnail of ${doujinshi.bookId}",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Black96)
            ) {
                val iconId = "inlineContent"
                val text = buildAnnotatedString {
                    appendInlineContent(iconId, "[myBox]")
                    append(doujinshi.previewTitle)
                }
                val inlineContent = mapOf(
                    Pair(
                        // This tells the [CoreText] to replace the placeholder string "[icon]" by
                        // the composable given in the [InlineTextContent] object.
                        iconId,
                        InlineTextContent(
                            // Placeholder tells text layout the expected size and vertical alignment of
                            // children composable.
                            Placeholder(
                                width = 20.sp,
                                height = 20.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Bottom
                            )
                        ) {
                            // This Icon will fill maximum size, which is specified by the [Placeholder]
                            // above. Notice the width and height in [Placeholder] are specified in TextUnit,
                            // and are converted into pixel by text layout.

                            Image(
                                painter = painterResource(id = getLanguageIconRes(doujinshi.language)),
                                contentDescription = doujinshi.language,
                                modifier = Modifier.padding(top = 7.dp, end = smallSpace)
                            )
                        }
                    )
                )
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = smallSpace, vertical = tinySpace),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = White),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                    inlineContent = inlineContent
                )
            }
        }
    }
}

private fun getLanguageIconRes(language: String): Int {
    return when (language) {
        CHINESE_LANG -> R.drawable.ic_lang_cn
        JAPANESE_LANG -> R.drawable.ic_lang_jp
        else -> R.drawable.ic_lang_gb
    }
}

@Composable
private fun Title(title: GalleryUiState.Title) {
    Text(
        text = title.title,
        style = MaterialTheme.typography.bodyLarge.copy(color = White),
        modifier = Modifier.padding(start = smallSpace, top = normalSpace, bottom = mediumSpace),
    )
}

