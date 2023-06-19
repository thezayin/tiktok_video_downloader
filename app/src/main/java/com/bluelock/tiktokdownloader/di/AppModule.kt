package com.bluelock.tiktokdownloader.di

import com.bluelock.tiktokdownloader.data.repository.Repository
import com.bluelock.tiktokdownloader.data.repository.RepositoryImpl
import com.bluelock.tiktokdownloader.data.remote.API
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): API {
        val inspector = HttpLoggingInterceptor()
        inspector.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
        client.addInterceptor(inspector)
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .baseUrl("https://www.tikwm.com/")
            .build()
            .create(API::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(api: API): Repository {
        return RepositoryImpl(api)
    }


}