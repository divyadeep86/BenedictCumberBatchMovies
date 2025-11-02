package com.vh.benedictcumberbatchmovies.presentation.mvi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vh.benedictcumberbatchmovies.presentation.mvi.intent.MovieDetailIntent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailActivity : ComponentActivity() {

    private val viewModel: MovieDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val movieId = intent.getIntExtra("movie_id", -1)

        setContent {
            val state = viewModel.state.collectAsStateWithLifecycle().value
            MovieDetailScreen(
                state = state,
                onBack = { onBackPressedDispatcher.onBackPressed() },
                onRetry = { viewModel.handleIntent(MovieDetailIntent.LoadMovie(movieId)) }
            )
        }

        if (movieId != -1) {
            viewModel.handleIntent(MovieDetailIntent.LoadMovie(movieId))
        }
    }
}