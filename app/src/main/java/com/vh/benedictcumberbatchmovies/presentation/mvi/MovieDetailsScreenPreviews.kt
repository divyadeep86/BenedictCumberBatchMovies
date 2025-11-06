package com.vh.benedictcumberbatchmovies.presentation.mvi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.presentation.mvi.state.MovieDetailState
import kotlinx.coroutines.flow.flowOf

@Preview(
    name = "PosterPlaceholder",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PosterPlaceholderPreview() {
    MaterialTheme {
        PosterPlaceholder()
    }
}

@Preview(
    name = "SimilarMovieCard",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SimilarMovieCardPreview() {
    MaterialTheme {
        SimilarMovieCard(
            movie = Movie(
                id = 42,
                title = "Doctor Strange",
                posterUrl = "https://image.tmdb.org/t/p/w500/8YFL5QQVPy3AgrEQxNYVSgiPEbe.jpg",
                overview = "…"
            ),
            onClick = {}
        )
    }
}

@Preview(
    name = "SimilarMoviesRow (static)",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SimilarMoviesRowStaticPreview() {
    MaterialTheme {
        val samples = List(8) { i ->
            Movie(
                id = i + 1,
                title = "Sample #${i + 1}",
                posterUrl = "https://image.tmdb.org/t/p/w500/8YFL5QQVPy3AgrEQxNYVSgiPEbe.jpg",
                overview = ""
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = samples, key = { it.id }) { m ->
                SimilarMovieCard(movie = m, onClick = {})
            }
        }
    }
}

// --- FULL SUCCESS PREVIEW (movie + similar row) ---
@Preview(
    name = "MovieDetailScreen — Full",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 411, heightDp = 891 // Pixel 6-ish
)
@Composable
fun MovieDetailScreenFullPreview() {
    // 1) Sample detail state
    val sampleState = MovieDetailState(
        isLoading = false,
        movie = Movie(
            id = 1,
            title = "Doctor Strange in the Multiverse of Madness",
            posterUrl = "https://image.tmdb.org/t/p/w500/8YFL5QQVPy3AgrEQxNYVSgiPEbe.jpg",
            overview = "Dr. Stephen Strange casts a forbidden spell that opens a portal to the multiverse..."
        ),
        error = null
    )

    // 2) Sample similar items
    val samples = remember {
        List(10) { i ->
            Movie(
                id = 100 + i,
                title = "Similar #${i + 1}",
                posterUrl = "https://image.tmdb.org/t/p/w500/8YFL5QQVPy3AgrEQxNYVSgiPEbe.jpg",
                overview = ""
            )
        }
    }

    // 3) Fake paging stream → LazyPagingItems for preview
    val pagingFlow = remember { flowOf(PagingData.from(samples)) }
    val similar = pagingFlow.collectAsLazyPagingItems()

    MaterialTheme {
        MovieDetailScreen(
            state = sampleState,
            similarMovies = similar,
            onBack = {},
            onRetry = {},
            onItemClick = {}
        )
    }
}

// --- LOADING PREVIEW (spinner visible) ---
@Preview(
    name = "MovieDetailScreen — Loading",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 411, heightDp = 891
)
@Composable
fun MovieDetailScreenLoadingPreview() {
    val loadingState = MovieDetailState(isLoading = true, movie = null, error = null)
    val similar =
        remember { flowOf(PagingData.from(emptyList<Movie>())) }.collectAsLazyPagingItems()

    MaterialTheme {
        MovieDetailScreen(
            state = loadingState,
            similarMovies = similar,
            onBack = {},
            onRetry = {},
            onItemClick = {}
        )
    }
}

// --- ERROR PREVIEW (error message + retry) ---
@Preview(
    name = "MovieDetailScreen — Error",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 411, heightDp = 891
)
@Composable
fun MovieDetailScreenErrorPreview() {
    val errorState =
        MovieDetailState(isLoading = false, movie = null, error = "No internet connection.")
    val similar =
        remember { flowOf(PagingData.from(emptyList<Movie>())) }.collectAsLazyPagingItems()

    MaterialTheme {
        MovieDetailScreen(
            state = errorState,
            similarMovies = similar,
            onBack = {},
            onRetry = {},
            onItemClick = {}
        )
    }
}