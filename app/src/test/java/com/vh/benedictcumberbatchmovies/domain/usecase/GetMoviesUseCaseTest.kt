package com.vh.benedictcumberbatchmovies.domain.usecase

import androidx.paging.PagingData
import com.vh.benedictcumberbatchmovies.data.model.MovieDto
import com.vh.benedictcumberbatchmovies.data.repository.MovieRepository
import com.vh.benedictcumberbatchmovies.domain.common.DataState
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMoviesUseCaseTest {
    // A deterministic dispatcher for coroutine tests
    private val testDispatcher = StandardTestDispatcher()

    // @Before runs before each @Test; set Main dispatcher to our test dispatcher
    @org.junit.Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    // @After runs after each @Test; restore the real Main dispatcher
    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Backtick-name allows free-form test names in Kotlin; here we assert type of emission
    @Test
    fun GetMoviesEmitsPagingDataOfMovie() = runTest {
        // Mock the repository’s getMovies() to emit one PagingData<DTO>
        val repo = mockk<MovieRepository>()
        every { repo.getMovies() } returns MutableStateFlow(
            PagingData.from(listOf(MovieDto(1, "A", "/a.jpg", "ov", "2020")))
        )
        // Wire the use case with the mocked repo
        val useCase = GetMoviesUseCase(repo)

        // Pull the first PagingData<Movie> from the use-case flow
        val emitted = useCase.getMovies().first()

        // assertTrue(condition): the condition must be true, else the test fails
        @Suppress("USELESS_IS_CHECK")
        assertTrue(emitted is PagingData<Movie>)
    }

    // Verifies Loading → Success emission order on detail flow
    @Test
    fun GetMovieDetailsEmitsLoadingThenSuccess() = runTest {
        // Mock repository to return a successful DataState with a DTO
        val repo = mockk<MovieRepository>()
        coEvery { repo.getMovieDetailFlow(7) } returns DataState.Success(
            MovieDto(7, "Seven", "/s.jpg", "desc", "2021")
        )
        val useCase = GetMoviesUseCase(repo)

        // Collect only the first two emissions from the flow (Loading, Success)
        val emissions = mutableListOf<DataState<Movie>>()
        useCase.getMovieDetails(7).collect {
            emissions += it
            if (emissions.size == 2) return@collect
        }

        // First should be Loading
        assertTrue(emissions[0] is DataState.Loading)
        // Second should be Success (with mapped Movie)
        assertTrue(emissions[1] is DataState.Success)
    }

    // Verifies Loading → Error emission order on detail flow
    @Test
    fun GetMovieDetailsEmitsLoadingThenError() = runTest {
        // Mock repository to return an Error DataState
        val repo = mockk<MovieRepository>()
        coEvery { repo.getMovieDetailFlow(9) } returns DataState.Error("No internet connection.")
        val useCase = GetMoviesUseCase(repo)

        // Collect two emissions (Loading, then Error)
        val emissions = mutableListOf<DataState<Movie>>()
        useCase.getMovieDetails(9).collect {
            emissions += it
            if (emissions.size == 2) return@collect
        }

        // Validate emission order and types
        assertTrue(emissions[0] is DataState.Loading)
        assertTrue(emissions[1] is DataState.Error)
    }

}