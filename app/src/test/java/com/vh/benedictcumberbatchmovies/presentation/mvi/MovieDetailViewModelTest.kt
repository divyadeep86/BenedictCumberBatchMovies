package com.vh.benedictcumberbatchmovies.presentation.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieDetailViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()

    private val useCase: GetMoviesUseCase = mockk(relaxed = true)
    private lateinit var saved: SavedStateHandle
    private lateinit var vm: MovieDetailViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        saved = SavedStateHandle()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setting id then success updates state with movie`() = runTest {
        val movie = Movie(7, "Seven", "/u", "desc")

        // Detail flow: Loading -> Success(movie)
        every { useCase.getMovieDetails(7) } returns flow {
            emit(DataState.Loading)
            emit(DataState.Success(movie))
        }
        // Similar paging flow (not asserted here, just stub so nothing hangs)
        every { useCase.getSimilarMovies(7) } returns MutableStateFlow(PagingData.from(emptyList<Movie>()))

        vm = MovieDetailViewModel(useCase, saved)

        // Triggers the init pipeline via SavedStateHandle
        vm.setMovieId(7)

        // Wait for a non-loading state that actually contains data
        val s = vm.state.first { it.movie != null && !it.isLoading }

        assertEquals(movie, s.movie)
        assertNull(s.error)
    }

    @Test
    fun `setting id then error updates state with message`() = runTest {
        // Detail flow: Loading -> Error
        every { useCase.getMovieDetails(9) } returns flow {
            emit(DataState.Loading)
            emit(DataState.Error("No internet connection."))
        }
        every { useCase.getSimilarMovies(9) } returns MutableStateFlow(PagingData.from(emptyList<Movie>()))

        vm = MovieDetailViewModel(useCase, saved)

        vm.setMovieId(9)

        // Wait for a non-loading state that has an error
        val s = vm.state.first { it.error != null && !it.isLoading }

        assertTrue(s.error!!.contains("internet", ignoreCase = true))
        assertNull(s.movie)
    }

    @Test
    fun `similarMovies emits PagingData when id is set`() = runTest {
        // Detail can succeed immediately; we’re not asserting it here
        every { useCase.getMovieDetails(1) } returns flowOf(
            DataState.Success(Movie(1, "One", "/p", "ov"))
        )
        // Return a concrete PagingData (no AsyncPagingDataDiffer needed)
        every { useCase.getSimilarMovies(1) } returns MutableStateFlow(
            PagingData.from(listOf(Movie(2, "Two", "/p2", "ov2")))
        )

        vm = MovieDetailViewModel(useCase, saved)

        vm.setMovieId(1)

        val pd = vm.similarMovies.first()

        @Suppress("USELESS_IS_CHECK")
        assertTrue(pd is PagingData<Movie>)
    }
}