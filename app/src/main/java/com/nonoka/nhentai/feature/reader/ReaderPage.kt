package com.nonoka.nhentai.feature.reader

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.feature.collection.CollectionViewModel
import com.nonoka.nhentai.ui.shared.SpaceItemDecoration
import com.nonoka.nhentai.ui.shared.zoomable.ZoomableBookLayout
import com.nonoka.nhentai.ui.shared.zoomable.ZoomableRecyclerView
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Black59
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.captionRegular
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.readerTitleStyle
import com.nonoka.nhentai.ui.theme.smallRadius
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace
import kotlinx.coroutines.launch
import timber.log.Timber

const val thumbnailScrollOffset = 2

@Composable
fun ReaderPage(
    doujinshiId: String,
    onCustomBackPressed: () -> Unit,
    onPageSelected: (Int) -> Unit,
    startIndex: Int = -1,
    viewModel: ReaderViewModel = hiltViewModel(),
    collectionViewModel: CollectionViewModel,
) {
    viewModel.init(doujinshiId)
    val thumbnailListState = rememberLazyListState()
    val coroutineContext = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        //todo: collectionViewModel.reset.value = true
    }
    Scaffold(
        containerColor = Black,
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            val readerState = viewModel.state.value
            if (readerState != null) {
                Reader(
                    readerState = readerState,
                    onFocusedIndexChanged = { index ->
                        viewModel.onPageFocused(index)
                        coroutineContext.launch {
                            thumbnailListState.scrollToItem(if (index - thumbnailScrollOffset >= 0) index - thumbnailScrollOffset else index)
                        }
                        onPageSelected(index)
                    },
                    startIndex = startIndex
                )

                val barsVisible by remember {
                    viewModel.toolBarsVisible
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopCenter),
                    visible = barsVisible,
                    enter = slideInVertically(
                        initialOffsetY = { offset ->
                            -offset
                        },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { offset ->
                            -offset
                        },
                    )
                ) {
                    TopBar(readerState, onCustomBackPressed)
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    visible = barsVisible,
                    enter = slideInVertically(
                        initialOffsetY = { offset ->
                            offset
                        },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { offset ->
                            offset
                        },
                    )
                ) {
                    BottomBar(readerState = readerState, thumbnailListState = thumbnailListState)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopBar(
    readerState: ReaderState,
    onBackPressed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black59),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            modifier = Modifier
                .wrapContentSize(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = White,
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(size = 0.dp),
            onClick = onBackPressed
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_solid_24dp),
                contentDescription = "Back",
                colorFilter = ColorFilter.tint(White),
            )
        }

        Text(
            text = readerState.title,
            style = MaterialTheme.typography.readerTitleStyle.copy(color = Color.White),
            modifier = Modifier
                .padding(top = mediumSpace, bottom = mediumSpace)
                .basicMarquee(iterations = Int.MAX_VALUE),
            maxLines = 1
        )
    }
}

@Composable
private fun BottomBar(
    readerState: ReaderState,
    thumbnailListState: LazyListState,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    if (readerState.images.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black59),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                contentPadding = PaddingValues(vertical = mediumSpace, horizontal = normalSpace),
                horizontalArrangement = Arrangement.spacedBy(mediumSpace),
                state = thumbnailListState,
            ) {
                items(count = readerState.thumbnailList.size) { index ->
                    val thumbnail = readerState.thumbnailList[index]
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(92.dp)
                            .clip(shape = RoundedCornerShape(size = mediumRadius)),
                    ) {
                        if (viewModel.focusedIndex.intValue == index) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(size = smallRadius))
                                    .background(color = MainColor),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .width(60.dp)
                                .height(90.dp)
                                .clip(shape = RoundedCornerShape(size = mediumRadius))
                                .background(Black59)
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        viewModel.requestReadingPage(index)
                                    },
                                model = thumbnail, contentDescription = "Thumb ${index + 1}",
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .fillMaxWidth()
                                .clip(
                                    shape = RoundedCornerShape(
                                        bottomStart = mediumRadius,
                                        bottomEnd = mediumRadius,
                                    )
                                )
                                .background(Black59)
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = smallSpace, vertical = tinySpace),
                                text = "${index + 1}",
                                style = MaterialTheme.typography.captionRegular.copy(color = White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = mediumSpace)
                        .padding(bottom = mediumSpace),
                    text = readerState.pageIndicatorTemplate.format(viewModel.focusedIndex.intValue + 1),
                    style = MaterialTheme.typography.bodyNormalBold.copy(color = White)
                )

                Image(
                    modifier = Modifier
                        .padding(bottom = mediumSpace),
                    painter = painterResource(id = if (readerState.isDownloaded) R.drawable.ic_folder_24dp else R.drawable.ic_global_24dp),
                    contentDescription = if (readerState.isDownloaded) "Downloaded file" else "Remote"
                )
            }
        }
    }
}

@Composable
private fun Reader(
    readerState: ReaderState,
    startIndex: Int = -1,
    onFocusedIndexChanged: (Int) -> Unit,
    viewModel: ReaderViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    AndroidView(
        factory = { context ->
            ZoomableBookLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false).apply {
                        // Enable item prefetching
                        isItemPrefetchEnabled = true

                        // Set the number of items to prefetch
                        initialPrefetchItemCount = 3 // Adjust this number as needed
                    }

                val reader = ZoomableRecyclerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    adapter = ReaderAdapter(readerState.images)

                    addItemDecoration(SpaceItemDecoration(context, R.dimen.app_medium_space))

                    this.layoutManager = layoutManager

                    val scrollListener = object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val focusedIndex =
                                if (dy > 0) layoutManager.findLastVisibleItemPosition() else layoutManager.findFirstVisibleItemPosition()
                            if (focusedIndex != RecyclerView.NO_POSITION) {
                                viewModel.focusedIndex.intValue = focusedIndex
                                onFocusedIndexChanged(focusedIndex)
                            }
                        }
                    }
                    addOnScrollListener(scrollListener)

                    tapListener = {
                        viewModel.toolBarsVisible.value = !viewModel.toolBarsVisible.value
                    }
                }

                addView(reader)

                Timber.d("Test>>> startIndex=$startIndex")
                if (startIndex > 0) {
                    layoutManager.scrollToPosition(startIndex)
                }

                lifecycleOwner.lifecycleScope.launch {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        viewModel.focusingIndexRequest.collect { index ->
                            if (index != null && index >= 0) {
                                reader.scrollToPosition(index)
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxHeight()
    )
}