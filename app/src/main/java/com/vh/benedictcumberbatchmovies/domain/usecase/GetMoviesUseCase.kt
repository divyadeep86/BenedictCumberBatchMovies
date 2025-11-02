package com.vh.benedictcumberbatchmovies.domain.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import com.vh.benedictcumberbatchmovies.data.repository.MovieRepository
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * UseCase to fetch a single movie's detail by its ID.
 */
class GetMoviesUseCase(
    private val repository: MovieRepository
) {
    /**
     * use raw paging flow + let UI handle LoadState.
     */
    fun getMovies(): Flow<PagingData<Movie>> =
        repository.getMovies().map { pagingData ->
            pagingData.map { it.toDomain() }
        }

    /**
     * Detail call returns Flow<DataState<Movie>>
     */
    fun getMovieDetails(movieId: Int): Flow<DataState<Movie>> = flow {
        emit(DataState.Loading)
        val response = repository.getMovieDetailFlow(movieId)
        when (response) {
            is DataState.Success -> emit(DataState.Success(data = response.data.toDomain()))
            is DataState.Error -> emit(response)
            else -> emit(DataState.Loading)
        }
    }

}

/** DTO → Domain mapper kept close to use case for clarity. */
private fun MovieDto.toDomain(): Movie =
    Movie(
        id = id,
        title = title ?: "No title available.",
        posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
        overview = overview ?: "No description available."
    )