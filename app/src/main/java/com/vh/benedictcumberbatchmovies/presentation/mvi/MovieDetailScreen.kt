package com.vh.benedictcumberbatchmovies.presentation.mvi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.vh.benedictcumberbatchmovies.domain.model.Movie
import com.vh.benedictcumberbatchmovies.presentation.mvi.state.MovieDetailState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    state: MovieDetailState,
    similarMovies: LazyPagingItems<Movie>,
    onItemClick: (Movie) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.movie?.title ?: "Movie Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(
                                id = com.vh.benedictcumberbatchmovies.R.drawable.round_arrow_back_24
                            ),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(message = state.error, onRetry = onRetry)
                state.movie != null -> MovieContent(state, similarMovies, onItemClick = onItemClick)
            }
        }
    }
}

@Composable
private fun LoadingView(

) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String?, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message ?: "Something went wrong", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun MovieContent(
    state: MovieDetailState,
    similarMovies: LazyPagingItems<Movie>,
    onItemClick: (Movie) -> Unit
) {
    val movie = state.movie ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        Image(
            painter = rememberAsyncImagePainter(model = movie.posterUrl),
            contentDescription = movie.title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeight * 0.6f).dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(text = movie.overview, style = MaterialTheme.typography.bodyLarge)
        }

        // --- Similar Movies section ---
        Text(
            text = "Similar movies",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        SimilarMoviesRow(
            similar = similarMovies,
            onItemClick = onItemClick
        )
        Spacer(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun SimilarMoviesRow(
    similar: LazyPagingItems<Movie>,
    onItemClick: (Movie) -> Unit
) {
    val loadState = similar.loadState

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Items (use count + index; stable keys via peek to avoid triggering loads)
        items(
            count = similar.itemCount,
            key = { index -> similar.peek(index)?.id ?: "placeholder-$index" }
        ) { index ->
            val item = similar[index]
            if (item != null) {
                SimilarMovieCard(
                    movie = item,
                    onClick = { onItemClick(item) }
                )
            } else {
                // Placeholder while paging loads this slot
                PosterPlaceholder()
            }
        }

        // Append footer: progress or error with retry
        item(key = "footer") {
            when {
                loadState.append is LoadState.Loading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp)) }

                loadState.append is LoadState.Error -> {
                    val e = (loadState.append as LoadState.Error).error
                    AssistChip(
                        onClick = { similar.retry() },
                        label = { Text(e.localizedMessage ?: "Load more failed. Tap to retry.") }
                    )
                }
            }
        }
    }

    // Top-level refresh error (first load)
    if (loadState.refresh is LoadState.Error) {
        val e = (loadState.refresh as LoadState.Error).error
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = e.localizedMessage ?: "Couldn’t load similar movies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { similar.retry() }) { Text("Retry") }
        }
    }
}

@Composable
internal fun SimilarMovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Rounded poster
        Image(
            painter = rememberAsyncImagePainter(model = movie.posterUrl),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(2f / 3f)                     // poster aspect
                .clip(RoundedCornerShape(12.dp))           // rounded corners
        )

    }
}

@Composable
internal fun PosterPlaceholder() {
    Box(
        modifier = Modifier
            .width(120.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

