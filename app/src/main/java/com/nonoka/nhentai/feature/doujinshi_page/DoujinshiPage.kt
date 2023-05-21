package com.nonoka.nhentai.feature.doujinshi_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.ui.shared.DoujinshiCard
import com.nonoka.nhentai.ui.theme.Black
import com.nonoka.nhentai.ui.theme.Black96
import com.nonoka.nhentai.ui.theme.Grey31
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodyRegularBold
import com.nonoka.nhentai.ui.theme.bodyRegularThin
import com.nonoka.nhentai.ui.theme.doujinshiPrimaryTitleStyle
import com.nonoka.nhentai.ui.theme.doujinshiSecondaryTitleStyle
import com.nonoka.nhentai.ui.theme.smallPlusSpace
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.bodySmallBold
import com.nonoka.nhentai.ui.theme.mediumPlusSpace
import com.nonoka.nhentai.ui.theme.smallRadius

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoujinshiPage(doujinshiId: String, viewModel: DoujinshiViewModel = hiltViewModel()) {
    viewModel.init(doujinshiId)
    val doujinshi = viewModel.doujinshiState.value
    if (doujinshi != null) {
        Scaffold(
            containerColor = Black
        ) {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    AsyncImage(
                        model = doujinshi.coverUrl,
                        contentDescription = "Thumbnail of ${doujinshi.id}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(doujinshi.coverRatio),
                    )
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
                        Text(
                            text = "ID:",
                            modifier = Modifier
                                .padding(end = smallSpace)
                                .padding(top = smallPlusSpace),
                            style = MaterialTheme.typography.bodyRegularBold.copy(White),
                        )
                        Text(
                            text = doujinshi.id,
                            modifier = Modifier
                                .padding(end = smallSpace, top = smallSpace)
                                .clip(RoundedCornerShape(mediumRadius))
                                .background(Grey31)
                                .padding(horizontal = mediumSpace, vertical = smallSpace),
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
                            style = MaterialTheme.typography.bodyRegularBold.copy(White),
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
                                        .padding(horizontal = mediumSpace, vertical = smallSpace),
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
                        style = MaterialTheme.typography.bodyRegularThin.copy(White),
                    )
                }

                item {
                    Row {
                        Button(
                            onClick = {

                            },
                            shape = RoundedCornerShape(mediumRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColor
                            ),
                            contentPadding = PaddingValues(horizontal = mediumPlusSpace),
                            modifier = Modifier.padding(start = mediumSpace, top = mediumSpace)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_favorite_solid_24dp),
                                contentDescription = "Favorite",
                                tint = White,
                                modifier = Modifier
                                    .padding(end = smallSpace)
                                    .size(16.dp)
                            )

                            Text(
                                text = "Favorite",
                                style = MaterialTheme.typography.bodyRegularBold
                            )
                        }

                        Button(
                            onClick = {

                            },
                            shape = RoundedCornerShape(mediumRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Grey31
                            ),
                            contentPadding = PaddingValues(horizontal = mediumPlusSpace),
                            modifier = Modifier.padding(start = mediumSpace, top = mediumSpace)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_download_solid_24dp),
                                contentDescription = "Download",
                                tint = White,
                                modifier = Modifier
                                    .padding(end = smallSpace)
                                    .size(16.dp)
                            )

                            Text(
                                text = "Download",
                                style = MaterialTheme.typography.bodyRegularBold
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Preview",
                        modifier = Modifier
                            .padding(start = mediumSpace, top = mediumSpace),
                        style = MaterialTheme.typography.bodyRegularBold.copy(White),
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
                            Box(contentAlignment = Alignment.BottomCenter) {
                                AsyncImage(
                                    model = doujinshi.previewThumbnails[index],
                                    contentDescription = "Thumbnail of ${doujinshi.id}",
                                    modifier = Modifier
                                        .height(200.dp)
                                        .width(150.dp)
                                        .clip(RoundedCornerShape(smallRadius))
                                        .background(Grey31),
                                    contentScale = ContentScale.FillBounds
                                )

                                Text(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .background(Black96)
                                        .padding(vertical = smallSpace),
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmallBold.copy(color = White),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                item {
                    RecommendedDoujinshis()
                }
            }
        }
    }
}

@Composable
private fun RecommendedDoujinshis(viewModel: DoujinshiViewModel = hiltViewModel()) {
    val recommendedDoujinshis = viewModel.recommendedDoujinshis
    if (recommendedDoujinshis.isNotEmpty()) {
        Text(
            text = "Recommended",
            modifier = Modifier
                .padding(start = mediumSpace, top = normalSpace),
            style = MaterialTheme.typography.bodyRegularBold.copy(White),
        )

        LazyRow(
            modifier = Modifier
                .height(408.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(mediumSpace),
            contentPadding = PaddingValues(horizontal = mediumSpace)
        ) {
            items(count = recommendedDoujinshis.size) { index ->
                DoujinshiCard(doujinshiItem = recommendedDoujinshis[index], size = Pair(200, 300))
            }
        }
    }
}