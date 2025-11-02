package com.vh.benedictcumberbatchmovies.presentation.mvvm.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vh.benedictcumberbatchmovies.R

class MovieLoadStateAdapter(
) : LoadStateAdapter<MovieLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state, parent, false)
        return LoadStateViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) =
        holder.bind(loadState)

    class LoadStateViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val progress: ProgressBar = view.findViewById(R.id.progress)
        private val errorMsg: TextView = view.findViewById(R.id.errorMsg)

        fun bind(loadState: LoadState) {
            progress.isVisible = loadState is LoadState.Loading
            errorMsg.isVisible = loadState is LoadState.Error
            if (loadState is LoadState.Error)
                errorMsg.text = loadState.error.localizedMessage
        }
    }
}
