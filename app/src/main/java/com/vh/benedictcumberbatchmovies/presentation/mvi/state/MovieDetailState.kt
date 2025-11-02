package com.vh.benedictcumberbatchmovies.presentation.mvi.state

import com.vh.benedictcumberbatchmovies.domain.model.Movie


/**
 * Immutable state describing what the UI should render.
 */
data class MovieDetailState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    val error: String? = null
)
