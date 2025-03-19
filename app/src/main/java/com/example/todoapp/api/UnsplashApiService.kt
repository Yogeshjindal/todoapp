package com.example.todoapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String, // Your API Key
        @Query("per_page") perPage: Int = 1 // Get only 1 best image
    ): UnsplashResponse

    companion object {
        private const val BASE_URL = "https://api.unsplash.com/"

        fun create(): UnsplashApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UnsplashApiService::class.java)
        }
    }
}

data class UnsplashResponse(val results: List<UnsplashPhoto>)
data class UnsplashPhoto(val urls: Urls)
data class Urls(val regular: String)
