package com.example.govind.di

import com.example.govind.domain.repository.GovindRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGovindRepository(
        impl: com.example.govind.data.repository.SupabaseGovindRepositoryImpl
    ): GovindRepository
}
