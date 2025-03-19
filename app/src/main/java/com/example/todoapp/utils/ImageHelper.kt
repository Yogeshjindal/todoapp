package com.example.todoapp.utils

import com.example.todoapp.api.UnsplashApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

object ImageHelper {
    private const val ACCESS_KEY = "osy_gIK29WpgcHYgzJRosWLz4C4ejWxHviPthAmpVlw" // Replace with your actual Unsplash API key
    private val unsplashApi = UnsplashApiService.create()

    fun fetchImage(taskName: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = unsplashApi.searchPhotos(query = taskName, clientId = ACCESS_KEY)
                val imageUrl = response.results.firstOrNull()?.urls?.regular

                if (imageUrl != null) {
                    onSuccess(imageUrl)
                } else {
                    onError("No image found")
                }
            } catch (e: HttpException) {
                onError("HTTP error: ${e.message}")
            } catch (e: Exception) {
                onError("Error fetching image: ${e.localizedMessage}")
            }
        }
    }
}
