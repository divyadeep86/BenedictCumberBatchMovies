package com.vh.benedictcumberbatchmovies.di

import com.vh.benedictcumberbatchmovies.data.repository.MovieRepository
import com.vh.benedictcumberbatchmovies.domain.usecase.GetMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideGetMoviesUseCase(repo: MovieRepository): GetMoviesUseCase = GetMoviesUseCase(repo)

}