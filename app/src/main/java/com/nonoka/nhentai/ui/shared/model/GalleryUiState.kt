package com.nonoka.nhentai.ui.shared.model

import com.nonoka.nhentai.domain.entity.doujinshi.Doujinshi

sealed class GalleryUiState {
    data class Title(val title: String) : GalleryUiState()
    data class DoujinshiItem(val doujinshi: Doujinshi) : GalleryUiState()
}
