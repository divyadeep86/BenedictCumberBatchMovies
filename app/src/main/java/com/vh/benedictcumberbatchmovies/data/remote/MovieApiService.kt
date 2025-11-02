package com.vh.benedictcumberbatchmovies.data.remote

import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import com.vh.benedictcumberbatchmovies.data.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("discover/movie")
    suspend fun getMovies(
        @Query("with_people") peopleId: Int = 71580,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String = "en-US",
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDto
}