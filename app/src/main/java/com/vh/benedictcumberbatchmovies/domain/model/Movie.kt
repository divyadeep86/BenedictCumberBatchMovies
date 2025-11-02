package com.vh.benedictcumberbatchmovies.domain.model

/**
 * Domain entity representing a Movie.
 * Decoupled from the network layer (MovieDto).
 */
data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val overview: String,
)