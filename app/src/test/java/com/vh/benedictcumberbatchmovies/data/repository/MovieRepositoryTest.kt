package com.vh.benedictcumberbatchmovies.data.repository

import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import com.vh.benedictcumberbatchmovies.data.model.MovieResponse
import com.vh.benedictcumberbatchmovies.data.remote.MovieApiService
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

// A tiny fake implementation of MovieApiService to control responses in tests
private class FakeApi : MovieApiService {
    // A lambda we can swap to return a success DTO or throw exceptions
    var fakeResponse: () -> MovieDto = { MovieDto(42, "Detail", "/p.jpg", "desc", "2020") }

    // Unused in these tests — return a trivial page
    override suspend fun getMovies(
        peopleId: Int, page: Int, sortBy: String, language: String, includeAdult: Boolean
    ) = MovieResponse(page, emptyList(), 1)

    // Delegate to the configurable lambda to simulate different behaviors
    override suspend fun getMovieDetail(
        movieId: Int,
        language: String
    ): MovieDto = fakeResponse()
}


class MovieRepositoryDetailTest {
    // @Test marks a method that JUnit should execute as a test case
    @Test
    fun getMovieDetailFlowReturnsSuccess() = runBlocking {
        // Create repository with a happy-path fake API
        val repo = MovieRepository(FakeApi())
        // Call the repo wrapper that maps exceptions to DataState
        val res = repo.getMovieDetailFlow(42)
        // Cast result to Success to assert the data fields
        val ok = res as DataState.Success
        // assertEquals(expected, actual): verifies the two values are equal
        assertEquals("Detail", ok.data.title)
        // Verify poster path flowed through correctly
        assertEquals("/p.jpg", ok.data.posterPath)
    }

    // Validates that UnknownHostException turns into a friendly "No internet" error
    @Test
    fun UnknownHostMapsToNoInternetError() = runBlocking {
        // Configure fake API to throw UnknownHostException (no network)
        val api = FakeApi().apply { fakeResponse = { throw UnknownHostException() } }
        val repo = MovieRepository(api)
        // Repo should catch and turn it into DataState.Error
        val res = repo.getMovieDetailFlow(1) as DataState.Error
        // Human-friendly error message
        assertEquals("No internet connection.", res.message)
        // And the underlying cause is preserved
        assertTrue(res.cause is UnknownHostException)
    }

    // Validates that IOException becomes a generic network error message
    @Test
    fun IOExceptionMapsToNetworkError() = runBlocking {
        // Throw an IOException (timeouts, connection resets, etc.)
        val api = FakeApi().apply { fakeResponse = { throw IOException("timeout") } }
        val repo = MovieRepository(api)
        // Should map to DataState.Error with our message
        val res = repo.getMovieDetailFlow(1) as DataState.Error
        assertEquals("Network error. Please try again.", res.message)
        assertTrue(res.cause is IOException)
    }

    // Validates that HTTP 500 maps to "Server is unreachable" with code
    @Test
    fun http500MapsToServerUnreachable() = runBlocking {
        // Build a fake 500 error response
        val response = Response.error<MovieDto>(
            500, ResponseBody.create("application/json".toMediaType(), "{}")
        )
        // Configure fake API to throw HttpException(500)
        val api = FakeApi().apply { fakeResponse = { throw HttpException(response) } }
        val repo = MovieRepository(api)
        // Expect DataState.Error with code and friendly message
        val res = repo.getMovieDetailFlow(1) as DataState.Error
        assertEquals(500, res.code)
        assertTrue(res.message.contains("Server is unreachable"))
    }
}

