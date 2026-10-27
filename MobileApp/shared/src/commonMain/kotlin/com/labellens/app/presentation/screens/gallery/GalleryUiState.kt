package com.labellens.app.presentation.screens.gallery

import com.labellens.app.domain.model.generation.GenerationOutput

data class GalleryUiState(
    val generations: List<GenerationOutput> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface GalleryUiEvent {
    data class OnClickItem(val item: GenerationOutput) : GalleryUiEvent
    data object OnClickGenerate : GalleryUiEvent
}
