package com.vh.benedictcumberbatchmovies.presentation.mvvm

import androidx.paging.PagingData
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieListViewModelTest {
    // Deterministic dispatcher for coroutine tests
    private val testDispatcher = StandardTestDispatcher()

    // Mocked use case dependency
    private val useCase: GetMoviesUseCase = mockk()

    // System under test
    private lateinit var vm: MovieListViewModel

    // Install test dispatcher and initialize ViewModel before each test
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock useCase.getMovies() to emit a single PagingData<Movie>
        every { useCase.getMovies() } returns MutableStateFlow(
            PagingData.from(listOf(Movie(1, "A", "/u", "ov")))
        )
        vm = MovieListViewModel(useCase)
    }

    // Restore the real dispatcher after each test
    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Smoke test: the flow emits a PagingData<Movie> object
    @Test
    fun moviesEmitsPagingData() = runTest {
        // Take the first emission from the movies flow
        val pd = vm.movies.first()
        // assertTrue: ensure the emitted type is PagingData<Movie>
        @Suppress("USELESS_IS_CHECK")
        assertTrue(pd is PagingData<Movie>)
    }
}