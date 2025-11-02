package com.vh.benedictcumberbatchmovies.presentation.mvi

import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import com.vh.benedictcumberbatchmovies.presentation.mvi.intent.MovieDetailIntent
import com.vh.benedictcumberbatchmovies.presentation.mvi.state.MovieDetailState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieDetailViewModelTest {

    // UnconfinedTestDispatcher ensures launched coroutines run immediately in tests
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocked use case injected into ViewModel
    private val useCase: GetMoviesUseCase = mockk()

    // System under test
    private lateinit var vm: MovieDetailViewModel

    // Set Dispatchers.Main to our test dispatcher before each test
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vm = MovieDetailViewModel(useCase)
    }

    // Restore the real Main dispatcher after each test
    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Verifies that on success, state ends with movie set and no error
    @Test
    fun LoadMovieSuccessUpdatesState() = runTest {
        // Build a fake domain movie result
        val movie = Movie(7, "Seven", "/u", "desc")
        // Stub use case to emit Loading then Success(movie)
        every { useCase.getMovieDetails(7) } returns flow {
            emit(DataState.Loading)
            emit(DataState.Success(movie))
        }

        // Fire the intent the ViewModel listens to
        vm.handleIntent(MovieDetailIntent.LoadMovie(7))

        // Wait until state has either movie or error (avoid inspecting the initial state)
        val s: MovieDetailState = vm.state.first { it.movie != null || it.error != null }

        // assertEquals: the actual movie in state equals the expected movie
        assertEquals(movie, s.movie)
        // Ensure error is not set for success path
        assertEquals(null, s.error)
    }

    // Verifies that on error, state ends with error message and no movie
    @Test
    fun LoadMovieErrorUpdatesState() = runTest {
        // Stub use case to emit Loading then Error(message)
        every { useCase.getMovieDetails(9) } returns flow {
            emit(DataState.Loading)
            emit(DataState.Error("No internet connection."))
        }

        // Trigger the intent
        vm.handleIntent(MovieDetailIntent.LoadMovie(9))

        // Await a terminal state (either movie or error available)
        val s: MovieDetailState = vm.state.first { it.movie != null || it.error != null }

        // assertTrue: condition must be true; here error contains substring "No internet"
        assertTrue(s.error?.contains("No internet") == true)
        // Movie should be null on error path
        assertEquals(null, s.movie)
    }
}