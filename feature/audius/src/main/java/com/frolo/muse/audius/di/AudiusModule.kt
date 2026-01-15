package com.frolo.muse.audius.di

import com.frolo.muse.audius.api.AudiusApiService
import com.frolo.muse.audius.repository.AudiusRepository
import com.frolo.muse.audius.repository.AudiusRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class AudiusModule {

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.audius.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    fun provideAudiusApiService(retrofit: Retrofit): AudiusApiService {
        return retrofit.create(AudiusApiService::class.java)
    }

    @Provides
    fun provideAudiusRepository(apiService: AudiusApiService): AudiusRepository {
        return AudiusRepositoryImpl(apiService)
    }

    @Provides
    fun provideSchedulerProvider(): SchedulerProvider {
        return AudiusSchedulerProvider()
    }
}
