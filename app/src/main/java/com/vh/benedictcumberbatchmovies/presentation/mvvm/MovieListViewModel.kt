package com.vh.benedictcumberbatchmovies.presentation.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(private val getMoviesUseCase: GetMoviesUseCase) :
    ViewModel() {

    // Expose only PagingData<Movie>; UI handles loading/errors via LoadState
    val movies: Flow<PagingData<Movie>> =
        getMoviesUseCase
            .getMovies()
            .cachedIn(viewModelScope) // keeps pages across config changes & shares collectors
}