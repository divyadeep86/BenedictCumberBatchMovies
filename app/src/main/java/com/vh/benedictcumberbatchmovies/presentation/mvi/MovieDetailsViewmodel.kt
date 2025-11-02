package com.vh.benedictcumberbatchmovies.presentation.mvi


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import com.vh.benedictcumberbatchmovies.presentation.mvi.intent.MovieDetailIntent
import com.vh.benedictcumberbatchmovies.presentation.mvi.state.MovieDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel following MVI principles.
 * Receives Intents → Updates immutable State.
 */
@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetailUseCase: GetMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailState())
    val state: StateFlow<MovieDetailState> = _state

    fun handleIntent(intent: MovieDetailIntent) {
        when (intent) {
            is MovieDetailIntent.LoadMovie -> loadMovie(intent.movieId)
            MovieDetailIntent.LoadSimilarMovies -> {
                // call to load similar movies function
            }
        }
    }

    private fun loadMovie(movieId: Int) {
        getMovieDetailUseCase.getMovieDetails(movieId = movieId).onEach { dataState ->
            _state.update { state ->
                state.copy(
                    isLoading = dataState is DataState.Loading,
                    movie = if (dataState is DataState.Success) dataState.data else state.movie,
                    error = if (dataState is DataState.Error) dataState.message else null
                )
            }
        }.launchIn(viewModelScope)
    }
}
