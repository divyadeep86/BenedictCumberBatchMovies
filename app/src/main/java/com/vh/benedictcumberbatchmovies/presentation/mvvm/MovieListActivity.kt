package com.vh.benedictcumberbatchmovies.presentation.mvvm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.vh.benedictcumberbatchmovies.databinding.ActivityMovieListBinding
import com.vh.benedictcumberbatchmovies.presentation.mvi.MovieDetailActivity
import com.vh.benedictcumberbatchmovies.presentation.mvvm.adapter.MovieAdapter
import com.vh.benedictcumberbatchmovies.presentation.mvvm.adapter.MovieLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieListActivity : AppCompatActivity() {
    private val viewModel: MovieListViewModel by viewModels()
    private lateinit var binding: ActivityMovieListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = MovieAdapter { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.recyclerMovies.adapter = adapter.withLoadStateFooter(
            footer = MovieLoadStateAdapter()
        )

        lifecycleScope.launch {
            viewModel.movies.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->
                // refresh spinner (first load)
                binding.progressBar.isVisible = loadState.refresh is LoadState.Loading

                // top-level error (first load failed)
                val error = loadState.refresh as? LoadState.Error
                binding.tvError.isVisible = error != null
                binding.tvError.text = error?.error?.message ?: ""
            }
        }


    }
}