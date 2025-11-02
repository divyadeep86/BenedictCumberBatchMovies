package com.vh.benedictcumberbatchmovies.presentation.mvvm.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vh.benedictcumberbatchmovies.R
import com.vh.benedictcumberbatchmovies.domain.model.Movie


class MovieAdapter(
    private val onItemClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val image: ImageView = view.findViewById(R.id.ivPoster)

        fun bind(movie: Movie) {
            title.text = movie.title
            image.load(movie.posterUrl) {
                placeholder(R.drawable.baseline_local_movies_24)
                error(R.drawable.baseline_broken_image_24)
            }
            itemView.setOnClickListener { onItemClick(movie) }
        }
    }
}
