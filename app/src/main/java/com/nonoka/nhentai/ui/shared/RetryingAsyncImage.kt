package com.nonoka.nhentai.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.ImageResult
import timber.log.Timber

sealed class ImageSource {
    data class Remote(val url: String) : ImageSource()
    data class Local(val drawableResId: Int) : ImageSource()
}

@Composable
fun RetryingAsyncImage(
    sources: List<ImageSource>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onImageLoaded: (() -> Unit)? = null,
    onImageFailed: (() -> Unit)? = null,
    onImageLoading: (() -> Unit)? = null,
    fallbackDrawable: Int? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    var currentSourceIndex by remember { mutableIntStateOf(0) }
    var imageResult by remember { mutableStateOf<ImageResult?>(null) }
    val currentSource = sources.getOrNull(currentSourceIndex)

    if (currentSource != null) {
        when (currentSource) {
            is ImageSource.Remote -> {
                val request =
                    ImageRequest.Builder(LocalContext.current)
                        .data(currentSource.url)
                        .build()

                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Success -> {
                                imageResult = state.result
                                onImageLoaded?.invoke()
                            }

                            is AsyncImagePainter.State.Error -> {
                                imageResult = state.result
                                onImageFailed?.invoke()
                                if (currentSourceIndex < sources.size - 1) {
                                    currentSourceIndex++ // Try the next source
                                    Timber.d("Retrying image load with source: ${sources[currentSourceIndex]}")
                                } else {
                                    Timber.e("All image sources failed.")
                                }
                            }

                            is AsyncImagePainter.State.Loading -> {
                                onImageLoading?.invoke()
                            }

                            else -> {}
                        }
                    },
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter,
                )
            }

            is ImageSource.Local -> {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = currentSource.drawableResId),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter,
                )
            }
        }
    } else {
        // Handle the case where all sources have failed
        if (fallbackDrawable != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = fallbackDrawable),
                contentDescription = contentDescription,
                modifier = modifier
            )
        } else {
            androidx.compose.material3.Text("Image failed to load.")
        }
    }
}