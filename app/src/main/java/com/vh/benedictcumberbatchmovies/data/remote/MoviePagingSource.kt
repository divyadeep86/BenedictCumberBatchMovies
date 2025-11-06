package com.vh.benedictcumberbatchmovies.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import kotlinx.coroutines.delay
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MoviePagingSource(
    private val api: MovieApiService,
    private val loadSimilarMoviesById: Int = 0,

) : PagingSource<Int, MovieDto>() {

    override fun getRefreshKey(state: PagingState<Int, MovieDto>): Int? {
        // Find the page closest to the anchor and derive the new key
        val anchor = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchor)
        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieDto> {
        val page = params.key ?: 1

        // Demo-only: show a visible spinner on each page in debug builds
        delay(1000L)

        return try {
            val response = if (loadSimilarMoviesById != 0) {
                api.getSimilarMovies(movieId = loadSimilarMoviesById, page = page)
            } else {
                api.getMovies(page = page)
            }

            // NOTE: adjust `totalPages` to your DTO field name (`total_pages` vs `totalPages`)
            val totalPages = response.totalPages  // <-- change to `totalPages` if that's your model

            val nextKey = if (page < totalPages) page + 1 else null
            LoadResult.Page(
                data = response.results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (t: Throwable) {
            Log.e(
                "ExceptionInLoadPage", "${t.message}"
            )
            LoadResult.Error(toFriendlyPagingException(t))
        }
    }

    /** Map low-level exceptions to user-friendly messages so LoadState.Error is nice. */
    private fun toFriendlyPagingException(t: Throwable): Throwable = when (t) {
        is UnknownHostException -> PagingFriendlyException("No internet connection.", t)
        is SocketTimeoutException -> PagingFriendlyException(
            "Connection timed out. Please retry.",
            t
        )

        is IOException -> PagingFriendlyException("Network error. Please try again.", t)
        is HttpException -> {
            val code = t.code()
            val msg = when {
                code == 401 || code == 403 -> "You are not authorized (HTTP $code)."
                code == 404 -> "Requested resource was not found (404)."
                code == 429 -> "Too many requests (429). Please wait and retry."
                code >= 500 -> "Server error (HTTP $code). Please try later."
                else -> "Request failed (HTTP $code)."
            }
            PagingHttpException(msg, code, t)
        }

        is SerializationException -> PagingFriendlyException(
            "Couldn’t read the server response.",
            t
        )

        else -> PagingFriendlyException(t.message ?: "Something went wrong.", t)
    }
}

/** Lightweight exceptions with readable messages for UI (LoadState.Error). */
class PagingFriendlyException(message: String, cause: Throwable? = null) : Exception(message, cause)
class PagingHttpException(message: String, val code: Int, cause: Throwable? = null) :
    Exception(message, cause)