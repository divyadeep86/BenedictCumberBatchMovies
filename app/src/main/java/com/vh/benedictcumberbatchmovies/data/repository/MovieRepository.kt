package com.vh.benedictcumberbatchmovies.data.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import com.vh.benedictcumberbatchmovies.data.remote.MovieApiService
import com.vh.benedictcumberbatchmovies.data.remote.MoviePagingSource
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.common.safeApiCall
import kotlinx.coroutines.flow.Flow


class MovieRepository(
    private val api: MovieApiService
) {

    /**
     * Paging errors are surfaced via Paging's LoadState. We keep the source flow raw here,
     * because the UI (Paging adapters) already understands append/refresh errors.
     */
    fun getMovies(): Flow<PagingData<MovieDto>> {
        return Pager(
            config = PagingConfig(pageSize = 5, enablePlaceholders = false),
            pagingSourceFactory = { MoviePagingSource(api) }
        ).flow
    }


    /**
     * Detail call wrapped with DataState and common exception handling.
     */
    /** Detail as Flow<DataState<MovieDto>> so upper layers can observe Loading/Success/Error */
    suspend fun getMovieDetailFlow(movieId: Int): DataState<MovieDto> {
        return safeApiCall { api.getMovieDetail(movieId) }
    }
}
