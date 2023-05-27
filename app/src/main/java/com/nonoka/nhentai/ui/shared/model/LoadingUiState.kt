package com.nonoka.nhentai.ui.shared.model

sealed class LoadingUiState {
    data class Loading(val message: String = "") : LoadingUiState()
    object Idle : LoadingUiState()
}