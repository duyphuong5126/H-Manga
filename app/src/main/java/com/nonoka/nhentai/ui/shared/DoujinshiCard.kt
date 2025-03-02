package com.nonoka.nhentai.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.CHINESE_LANG
import com.nonoka.nhentai.domain.entity.JAPANESE_LANG
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import com.nonoka.nhentai.ui.theme.Black59
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodySmallBold
import com.nonoka.nhentai.ui.theme.captionRegular
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalRadius
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoujinshiCard(
    doujinshiItem: GalleryUiState.DoujinshiItem,
    onDoujinshiSelected: (String) -> Unit = {},
    size: Pair<Int, Int>? = null,
    isFavorite: Boolean = false,
    isDownloaded: Boolean = false,
    isRead: Boolean = false,
) {
    val doujinshi = doujinshiItem.doujinshi
    Box(
        modifier = ((if (size != null) Modifier
            .size(size.first.dp, size.second.dp) else Modifier
            .fillMaxWidth())
            .aspectRatio(doujinshi.thumbnailRatio))
            .clip(shape = RoundedCornerShape(size = mediumRadius))
            .clickable {
                onDoujinshiSelected(doujinshi.id)
            }
    ) {
        AsyncImage(
            model = doujinshi.thumbnail,
            contentDescription = "Thumbnail of ${doujinshi.id}",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Black59)
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
                    style = MaterialTheme.typography.bodySmallBold.copy(color = White),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                    inlineContent = inlineContent
                )

                Spacer(modifier = Modifier.width(mediumSpace))

                val items = arrayListOf<Status>()
                if (isFavorite) items.add(Status.Favorite)
                if (isDownloaded) items.add(Status.Downloaded)
                if (isRead) items.add(Status.Read)
                FlowRow(
                    modifier = Modifier.padding(horizontal = smallSpace)
                ) {
                    for (item in items) {
                        Box(modifier = Modifier.padding(vertical = tinySpace)) {
                            Tag(item)
                        }
                        Spacer(modifier = Modifier.width(smallSpace))
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(status: Status) {
    val (icon, backgroundColor, text) = when (status) {
        Status.Downloaded -> Triple(Icons.Outlined.Check, Color.Blue, "Downloaded")
        Status.Favorite -> Triple(Icons.Default.Favorite, Color.Red, "Favorite")
        Status.Read -> Triple(Icons.Default.Refresh, Color(0xFF669933), "Read")
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(normalRadius))
            .padding(horizontal = mediumSpace, vertical = smallSpace)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(normalSpace)
            )
            Spacer(modifier = Modifier.width(mediumSpace))
            Text(
                text = text,
                style = MaterialTheme.typography.captionRegular.copy(color = Color.White)
            )
        }
    }
}

private enum class Status {
    Downloaded, Favorite, Read
}

private fun getLanguageIconRes(language: String): Int {
    return when (language) {
        CHINESE_LANG -> R.drawable.ic_lang_cn
        JAPANESE_LANG -> R.drawable.ic_lang_jp
        else -> R.drawable.ic_lang_gb
    }
}