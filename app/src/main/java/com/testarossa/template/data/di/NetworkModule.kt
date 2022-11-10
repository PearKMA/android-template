package com.testarossa.template.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
//    @Singleton
//    @Provides
//    @Named("Weather")
//    fun provideRetrofitWeather(
//        @ApplicationContext app: Context
//    ): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(url)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideAppApi(
//        @Named("Weather") retrofit: Retrofit
//    ): WeatherService =
//        retrofit.create(WeatherService::class.java)
}