package com.nonoka.nhentai.feature.doujinshi_page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.feature.collection.CollectionViewModel
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.shared.LoadingDialog
import com.nonoka.nhentai.ui.shared.LoadingDialogContent
import com.nonoka.nhentai.ui.shared.YesNoDialog
import com.nonoka.nhentai.ui.shared.model.LoadingUiState
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Black59
import com.nonoka.nhentai.ui.theme.Black95
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.bodyNormalRegular
import com.nonoka.nhentai.ui.theme.bodySmallBold
import com.nonoka.nhentai.ui.theme.bodySmallRegular
import com.nonoka.nhentai.ui.theme.captionRegular
import com.nonoka.nhentai.ui.theme.doujinshiPrimaryTitleStyle
import com.nonoka.nhentai.ui.theme.doujinshiSecondaryTitleStyle
import com.nonoka.nhentai.ui.theme.mediumPlusSpace
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalIconSize
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallPlusSpace
import com.nonoka.nhentai.ui.theme.smallRadius
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.worker.DoujinshiDownloadWorker
import com.nonoka.nhentai.worker.DoujinshiDownloadWorker.Companion.DOUJINSHI_ID
import com.nonoka.nhentai.worker.DoujinshiDownloadWorker.Companion.PROGRESS_KEY
import com.nonoka.nhentai.worker.DoujinshiDownloadWorker.Companion.TOTAL_KEY
import java.text.DecimalFormat
import timber.log.Timber


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoujinshiPage(
    doujinshiId: String,
    startReading: (String, Int) -> Unit = { _, _ -> },
    onTagSelected: (String) -> Unit = { _ -> },
    onDoujinshiChanged: (String) -> Unit = {},
    viewModel: DoujinshiViewModel,
    collectionViewModel: CollectionViewModel,
    onBackPressed: () -> Unit,
    lastReadPage: Int? = null,
) {
    LaunchedEffect(
        key1 = doujinshiId,
        block = {
            viewModel.init(doujinshiId)
        },
    )

    LaunchedEffect(
        key1 = lastReadPage,
        block = {
            if (lastReadPage != null) {
                viewModel.loadLastReadPageIndex(doujinshiId)
            }
        },
    )

    val mainLoadingState by remember {
        viewModel.mainLoadingState
    }
    if (mainLoadingState is LoadingUiState.Loading) {
        LoadingDialog(message = (mainLoadingState as LoadingUiState.Loading).message)
    }
    val doujinshi = viewModel.doujinshiState.value
    Scaffold(
        containerColor = Black
    ) {
        if (doujinshi != null) {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(doujinshi.coverRatio)
                    ) {
                        AsyncImage(
                            model = doujinshi.coverUrl,
                            contentDescription = "Thumbnail of ${doujinshi.id}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    startReading(doujinshi.id, 0)
                                },
                        )

                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Black95,
                                            Color.Transparent
                                        )
                                    )
                                ),
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
                        }
                    }
                }

                item {
                    Text(
                        modifier = Modifier.padding(
                            start = mediumSpace,
                            end = mediumSpace,
                            top = mediumSpace
                        ),
                        text = doujinshi.primaryTitle,
                        style = MaterialTheme.typography.doujinshiPrimaryTitleStyle.copy(color = White),
                    )
                }

                if (!doujinshi.secondaryTitle.isNullOrBlank()) {
                    item {
                        Text(
                            modifier = Modifier.padding(
                                start = mediumSpace,
                                end = mediumSpace,
                                bottom = normalSpace
                            ),
                            text = doujinshi.secondaryTitle,
                            style = MaterialTheme.typography.doujinshiSecondaryTitleStyle.copy(color = White),
                        )
                    }
                }

                val tagGroups = doujinshi.tags.keys.sorted()
                item {
                    Row(
                        modifier = Modifier.padding(
                            start = mediumSpace,
                            end = mediumSpace,
                            bottom = mediumSpace
                        )
                    ) {
                        val context = LocalContext.current
                        Text(
                            text = "ID:",
                            modifier = Modifier
                                .padding(end = smallSpace)
                                .padding(top = smallPlusSpace),
                            style = MaterialTheme.typography.bodyNormalBold.copy(White),
                        )
                        Text(
                            text = doujinshi.id,
                            modifier = Modifier
                                .padding(end = smallSpace, top = smallSpace)
                                .clip(RoundedCornerShape(mediumRadius))
                                .background(Grey31)
                                .padding(horizontal = mediumSpace, vertical = smallSpace)
                                .clickable {
                                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                        .setPrimaryClip(
                                            ClipData.newPlainText("Doujinshi ID", doujinshi.id)
                                        )
                                },
                            style = MaterialTheme.typography.bodySmallBold.copy(White),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                items(count = tagGroups.size) { index ->
                    val tagType = tagGroups[index]
                    Row(
                        modifier = Modifier.padding(
                            start = mediumSpace,
                            end = mediumSpace,
                            bottom = mediumSpace
                        )
                    ) {
                        Text(
                            text = "${tagType}:",
                            modifier = Modifier
                                .padding(end = smallSpace)
                                .padding(top = smallPlusSpace),
                            style = MaterialTheme.typography.bodyNormalBold.copy(White),
                        )

                        FlowRow {
                            doujinshi.tags[tagType]?.forEach { tag ->
                                val tagInfo = buildAnnotatedString {
                                    append("${tag.label} ")
                                    withStyle(style = SpanStyle(color = Grey77)) {
                                        append(tag.count)
                                    }
                                }
                                Text(
                                    text = tagInfo,
                                    modifier = Modifier
                                        .padding(end = smallSpace, top = smallSpace)
                                        .clip(RoundedCornerShape(mediumRadius))
                                        .background(Grey31)
                                        .padding(horizontal = mediumSpace, vertical = smallSpace)
                                        .clickable {
                                            onTagSelected(tag.label)
                                        },
                                    style = MaterialTheme.typography.bodySmallBold.copy(White),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                val otherInfo = arrayOf(doujinshi.pageCount, doujinshi.updatedAt, doujinshi.comment)
                items(count = otherInfo.size) { index ->
                    val info = otherInfo[index]
                    Text(
                        text = info,
                        modifier = Modifier
                            .padding(start = mediumSpace, end = smallSpace)
                            .padding(top = smallPlusSpace),
                        style = MaterialTheme.typography.bodyNormalRegular.copy(White),
                    )
                }

                item {
                    val downloadRequestId by remember {
                        viewModel.downloadRequestId
                    }
                    val context = LocalContext.current
                    var isDownloading = false
                    var progressData: Data? = null
                    if (downloadRequestId != null) {
                        val workInfo: WorkInfo? by WorkManager.getInstance(context)
                            // requestId is the WorkRequest id
                            .getWorkInfoByIdLiveData(downloadRequestId!!)
                            .observeAsState()
                        progressData = workInfo?.progress
                        if (progressData != null) {
                            val progress = progressData.getInt(PROGRESS_KEY, -1)
                            val total = progressData.getInt(TOTAL_KEY, -1)
                            isDownloading = progress in 0..total
                        }
                    }
                    Column {
                        Row {
                            val isFavorite by remember {
                                viewModel.favoriteStatus
                            }
                            Button(
                                onClick = {
                                    viewModel.toggleFavoriteStatus(doujinshi.origin) {
                                        collectionViewModel.reset()
                                    }
                                },
                                shape = RoundedCornerShape(mediumRadius),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFavorite) White else MainColor
                                ),
                                contentPadding = PaddingValues(horizontal = mediumPlusSpace),
                                modifier = Modifier.padding(start = mediumSpace, top = mediumSpace)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_favorite_solid_24dp),
                                    contentDescription = doujinshi.favoritesLabel,
                                    tint = if (isFavorite) MainColor else White,
                                    modifier = Modifier
                                        .padding(end = smallSpace)
                                        .size(normalSpace)
                                )

                                Text(
                                    text = doujinshi.favoritesLabel,
                                    style = MaterialTheme.typography.bodyNormalBold,
                                    color = if (isFavorite) MainColor else White
                                )
                            }

                            val isDownloaded = doujinshi.isDownloaded
                            Timber.d("Download>>> doujinshi=${doujinshi.id}, downloadRequestId=$downloadRequestId, isDownloaded=$isDownloaded, isDownloading=$isDownloading")
                            if (!isDownloaded && !isDownloading) {
                                val downloadClickId = remember {
                                    mutableStateOf<Long?>(null)
                                }
                                if (downloadClickId.value != null) {
                                    YesNoDialog(
                                        title = "Download Doujinshi",
                                        description = "Do you want to start downloading this doujinshi?",
                                        onDismiss = {
                                            downloadClickId.value = null
                                        },
                                        onAnswerYes = {
                                            downloadClickId.value = null
                                            viewModel.downloadRequestId.value =
                                                DoujinshiDownloadWorker.start(context, doujinshi.id)
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        downloadClickId.value = System.currentTimeMillis()
                                    },
                                    shape = RoundedCornerShape(mediumRadius),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Grey31
                                    ),
                                    contentPadding = PaddingValues(horizontal = mediumPlusSpace),
                                    modifier = Modifier.padding(
                                        start = mediumSpace,
                                        top = mediumSpace
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_download_solid_24dp),
                                        contentDescription = "Download",
                                        tint = White,
                                        modifier = Modifier
                                            .padding(end = smallSpace)
                                            .size(normalSpace)
                                    )

                                    Text(
                                        text = "Download",
                                        style = MaterialTheme.typography.bodyNormalBold
                                    )
                                }
                            }
                        }

                        if (progressData != null) {
                            val progress = progressData.getInt(PROGRESS_KEY, -1)
                            val total = progressData.getInt(TOTAL_KEY, -1)
                            val downloadingDoujinshiId = progressData.getString(DOUJINSHI_ID)
                            if (progress in 0..total && downloadingDoujinshiId == doujinshi.id) {
                                val progressValue = progress.toFloat() / total
                                Row(
                                    modifier = Modifier.padding(mediumSpace),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = normalSpace)
                                            .height(normalSpace)
                                            .clip(
                                                RoundedCornerShape(mediumRadius)
                                            )
                                            .weight(1f),
                                        progress = { progressValue },
                                        color = MainColor
                                    )

                                    val complete = progress == total
                                    if (complete) {
                                        viewModel.loadDownloadedStatus(doujinshi.id) {
                                            collectionViewModel.reset()
                                        }
                                    }
                                    val progressLabel =
                                        if (complete) "Complete" else "($progress/$total)"
                                    Text(
                                        text = progressLabel,
                                        style = MaterialTheme.typography.bodyNormalBold.copy(
                                            White
                                        ),
                                    )
                                }
                                if (progress == total) {
                                    Timber.d("Downloader - UI reset")
                                    viewModel.downloadRequestId.value = null
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Preview",
                        modifier = Modifier
                            .padding(start = mediumSpace, top = mediumSpace),
                        style = MaterialTheme.typography.bodyNormalBold.copy(White),
                    )
                }

                item {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(top = mediumSpace)
                            .height(408.dp),
                        verticalArrangement = Arrangement.spacedBy(mediumSpace),
                        horizontalArrangement = Arrangement.spacedBy(mediumSpace),
                        contentPadding = PaddingValues(horizontal = mediumSpace)
                    ) {
                        items(count = doujinshi.previewThumbnails.size) { index ->
                            PageThumbnail(
                                doujinshiId = doujinshi.id,
                                thumbnailUrl = doujinshi.previewThumbnails[index],
                                index = index,
                                startReading = startReading
                            )
                        }
                    }
                }

                val lastReadPageIndex = viewModel.lastReadPageIndex.value
                if (lastReadPageIndex != null && lastReadPageIndex >= 0) {
                    item {
                        val resetRequestId = remember {
                            mutableStateOf<Long?>(null)
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = "Continue reading",
                                modifier = Modifier
                                    .padding(start = mediumSpace, top = normalSpace),
                                style = MaterialTheme.typography.bodyNormalBold.copy(White),
                            )

                            if (resetRequestId.value != null) {
                                YesNoDialog(
                                    title = "Delete Reading History",
                                    description = "Do you want to delete your reading history?",
                                    onDismiss = {
                                        resetRequestId.value = null
                                    },
                                    onAnswerYes = {
                                        viewModel.resetLastReadPage(doujinshi.id) {
                                            collectionViewModel.reset()
                                        }
                                    }
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.ic_un_seen),
                                contentDescription = "Delete reading history",
                                colorFilter = ColorFilter.tint(MainColor),
                                modifier = Modifier
                                    .padding(start = smallSpace)
                                    .height(24.dp)
                                    .scale(0.8f)
                                    .clickable {
                                        resetRequestId.value = System.currentTimeMillis()
                                    },
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(start = mediumSpace, top = mediumSpace)) {
                            PageThumbnail(
                                doujinshiId = doujinshi.id,
                                thumbnailUrl = doujinshi.previewThumbnails[lastReadPageIndex],
                                index = lastReadPageIndex,
                                startReading = startReading
                            )
                        }
                    }
                }

                item {
                    if (doujinshi.isDownloaded) {
                        val deleteClickId = remember {
                            mutableStateOf<Long?>(null)
                        }
                        if (deleteClickId.value != null) {
                            YesNoDialog(
                                title = "Delete Downloaded Data",
                                description = "Do you want to delete the downloaded data of this doujinshi?",
                                onDismiss = {
                                    deleteClickId.value = null
                                },
                                onAnswerYes = {
                                    deleteClickId.value = null
                                    viewModel.deleteDownloadedData(doujinshi.origin) {
                                        collectionViewModel.reset()
                                    }
                                }
                            )
                        }

                        Button(
                            onClick = {
                                deleteClickId.value = System.currentTimeMillis()
                            },
                            shape = RoundedCornerShape(mediumRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Grey31
                            ),
                            contentPadding = PaddingValues(horizontal = mediumPlusSpace),
                            modifier = Modifier
                                .padding(
                                    start = mediumSpace,
                                    end = mediumSpace,
                                    top = normalSpace
                                )
                                .fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_trash_24dp),
                                contentDescription = "Delete downloaded data",
                                tint = White,
                                modifier = Modifier
                                    .padding(end = smallSpace)
                                    .size(normalSpace)
                            )

                            Text(
                                text = "Delete downloaded data",
                                style = MaterialTheme.typography.bodyNormalBold
                            )
                        }
                    }
                }

                item {
                    RecommendedDoujinshis(viewModel, onDoujinshiChanged)
                }

                val comments = viewModel.comments
                if (comments.isNotEmpty()) {
                    val decimalFormat = DecimalFormat("#,###")
                    item {
                        Text(
                            text = "Comments (${decimalFormat.format(comments.size)})",
                            modifier = Modifier
                                .padding(start = mediumSpace, top = mediumSpace),
                            style = MaterialTheme.typography.bodyNormalBold.copy(White),
                        )
                    }

                    items(count = comments.size) { index ->
                        val comment = comments[index]
                        Comment(comment, index)
                    }
                } else {
                    item {
                        val commentLoadingState by remember {
                            viewModel.commentLoadingState
                        }
                        if (commentLoadingState is LoadingUiState.Loading) {
                            LoadingDialogContent(
                                modifier = Modifier
                                    .padding(mediumSpace)
                                    .fillMaxWidth(),
                                message = "Loading, please wait."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedDoujinshis(
    viewModel: DoujinshiViewModel,
    onDoujinshiChanged: (String) -> Unit,
) {
    val recommendedDoujinshis = viewModel.recommendedDoujinshis

    if (recommendedDoujinshis.isNotEmpty()) {
        Text(
            text = "Recommended",
            modifier = Modifier
                .padding(start = mediumSpace, top = normalSpace),
            style = MaterialTheme.typography.bodyNormalBold.copy(White),
        )

        LazyRow(
            modifier = Modifier
                .height(308.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(mediumSpace),
            contentPadding = PaddingValues(horizontal = mediumSpace)
        ) {
            items(count = recommendedDoujinshis.size) { index ->
                DoujinshiCard(
                    doujinshiItem = recommendedDoujinshis[index],
                    size = Pair(200, 300),
                    onDoujinshiSelected = onDoujinshiChanged,
                )
            }
        }
    } else {
        val recommendationLoadingState by remember {
            viewModel.recommendationLoadingState
        }
        if (recommendationLoadingState is LoadingUiState.Loading) {
            LoadingDialogContent(
                modifier = Modifier
                    .padding(mediumSpace)
                    .fillMaxWidth(),
                message = "Loading, please wait."
            )
        }
    }
}

@Composable
private fun Comment(comment: CommentState, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = smallPlusSpace,
                end = smallPlusSpace,
                top = smallPlusSpace
            )
            .clip(RoundedCornerShape(mediumRadius))
            .background(Grey77)
            .padding(mediumSpace),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.avatarUrl,
            contentDescription = "Avatar of user ${comment.userName}",
            modifier = Modifier
                .size(normalIconSize)
                .clip(CircleShape)
        )

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = mediumSpace)
        ) {
            Row {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.bodyNormalBold.copy(color = White),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.captionRegular.copy(color = White)
                )
            }

            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodySmallRegular.copy(color = White),
                modifier = Modifier
                    .padding(top = mediumSpace)
                    .fillMaxWidth()
            )

            Text(
                modifier = Modifier
                    .padding(top = normalSpace),
                text = comment.postDate,
                style = MaterialTheme.typography.captionRegular.copy(color = Grey31)
            )
        }
    }
}

@Composable
fun PageThumbnail(
    doujinshiId: String,
    thumbnailUrl: String,
    index: Int,
    startReading: (String, Int) -> Unit,
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "Thumbnail of $doujinshiId",
            modifier = Modifier
                .height(200.dp)
                .width(150.dp)
                .clip(RoundedCornerShape(smallRadius))
                .background(Grey31)
                .clickable {
                    startReading(doujinshiId, index)
                },
            contentScale = ContentScale.FillBounds
        )

        Text(
            modifier = Modifier
                .width(150.dp)
                .background(Black59)
                .padding(vertical = smallSpace),
            text = "${index + 1}",
            style = MaterialTheme.typography.bodySmallBold.copy(color = White),
            textAlign = TextAlign.Center,
        )
    }
}