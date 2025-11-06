package com.vh.benedictcumberbatchmovies.presentation.mvi.intent

/**
 * Represents all user-driven actions (Intents)
 * that can trigger state changes in the MVI flow.
 */
sealed class MovieDetailIntent {
    data class LoadMovie(val movieId: Int) : MovieDetailIntent()
    data class LoadMovieAndSimilarMovies(val movieId: Int) : MovieDetailIntent()

}