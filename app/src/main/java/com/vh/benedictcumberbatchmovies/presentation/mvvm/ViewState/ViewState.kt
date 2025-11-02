package com.vh.benedictcumberbatchmovies.presentation.mvvm.ViewState

import androidx.paging.PagingData
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import kotlinx.coroutines.flow.Flow

/**
 * Represents the UI state for the movie list screen.
 */
data class MovieListUiState(
    val isLoading: Boolean = false,
    val movies: Flow<PagingData<Movie>>? = null,
    val errorMessage: String? = null
)