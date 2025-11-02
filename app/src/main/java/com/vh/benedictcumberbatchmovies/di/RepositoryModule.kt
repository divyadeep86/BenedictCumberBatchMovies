package com.vh.benedictcumberbatchmovies.di

import com.vh.benedictcumberbatchmovies.data.remote.MovieApiService
import com.vh.benedictcumberbatchmovies.data.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideMovieRepository(
        movieApiService: MovieApiService
    ): MovieRepository = MovieRepository(movieApiService)
}