package com.vh.benedictcumberbatchmovies.presentation.mvi


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import com.vh.benedictcumberbatchmovies.presentation.mvi.intent.MovieDetailIntent
import com.vh.benedictcumberbatchmovies.presentation.mvi.state.MovieDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel following MVI principles.
 * Receives Intents → Updates immutable State.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetailUseCase: GetMoviesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    companion object {
        private const val MOVIE_ID_KEY = "movie_id"
    }

    private val _state = MutableStateFlow(MovieDetailState(isLoading = true))
    val state: StateFlow<MovieDetailState> = _state.asStateFlow()

    // one source of truth for the ID
    private val movieIdFlow: Flow<Int> =
        savedStateHandle.getStateFlow(MOVIE_ID_KEY, -1)
            .filter { it > 0 }
            .distinctUntilChanged()

    init {
        // DETAIL: react to id → fetch, update state via side-effects
        movieIdFlow
            .flatMapLatest { id -> getMovieDetailUseCase.getMovieDetails(id) } // Flow<DataState<Movie>>
            .onEach { ds ->
                _state.update { prev ->
                    when (ds) {
                        DataState.Loading -> prev.copy(isLoading = true, error = null)
                        is DataState.Success -> MovieDetailState(false, ds.data, null)
                        is DataState.Error -> prev.copy(isLoading = false, error = ds.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // SIMILAR: paging derived from the same id stream
    val similarMovies: Flow<PagingData<Movie>> =
        movieIdFlow
            .flatMapLatest { id -> getMovieDetailUseCase.getSimilarMovies(id) } // Flow<PagingData<Movie>>
            .cachedIn(viewModelScope)

    fun handleIntent(intent: MovieDetailIntent) {
        when (intent) {
            is MovieDetailIntent.LoadMovie -> {
                // Call if we want to load only movie data
            }

            is MovieDetailIntent.LoadMovieAndSimilarMovies -> {
                setMovieId(intent.movieId)
            }
        }
    }

    fun setMovieId(movieId: Int) {
        savedStateHandle[MOVIE_ID_KEY] = movieId
    }

}
