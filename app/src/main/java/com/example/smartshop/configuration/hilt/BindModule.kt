package com.example.smartshop.configuration.hilt

import com.example.smartshop.data.local.dao.ProductRepositoryImpl
import com.example.smartshop.data.remote.firebase.authentication.AuthService
import com.example.smartshop.data.remote.firebase.dao.ProductFirestoreDao
import com.example.smartshop.data.remote.firebase.dao.ProductFirestoreDaoImpl
import com.example.smartshop.domain.repository.AuthRepository
import com.example.smartshop.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds @Singleton
    abstract fun bindRepo(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindRemoteDao(impl: ProductFirestoreDaoImpl): ProductFirestoreDao

    @Binds @Singleton
    abstract fun bindAuth(impl: AuthService): AuthRepository

}

