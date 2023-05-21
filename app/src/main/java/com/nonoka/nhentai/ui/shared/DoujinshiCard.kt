package com.nonoka.nhentai.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.nonoka.nhentai.R
import com.nonoka.nhentai.domain.entity.CHINESE_LANG
import com.nonoka.nhentai.domain.entity.JAPANESE_LANG
import com.nonoka.nhentai.feature.home.GalleryUiState
import com.nonoka.nhentai.ui.theme.Black96
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.bodySmallBold
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace

@Composable
fun DoujinshiCard(
    doujinshiItem: GalleryUiState.DoujinshiItem,
    onDoujinshiSelected: (String) -> Unit = {},
    size: Pair<Int, Int>? = null
) {
    val doujinshi = doujinshiItem.doujinshi
    Box(
        modifier = (if (size != null) Modifier
            .size(size.first.dp, size.second.dp) else Modifier
            .fillMaxWidth()
            .aspectRatio(doujinshi.thumbnailRatio))
            .clip(shape = RoundedCornerShape(size = mediumRadius))
            .clickable {
                onDoujinshiSelected(doujinshi.bookId)
            }
    ) {
        AsyncImage(
            model = doujinshi.thumbnail,
            contentDescription = "Thumbnail of ${doujinshi.bookId}",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize()
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
                    style = MaterialTheme.typography.bodySmallBold.copy(color = White),
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