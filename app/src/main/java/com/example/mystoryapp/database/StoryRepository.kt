package com.example.mystoryapp.database

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.StoryPagingSource
import com.example.mystoryapp.utils.Result
import com.example.mystoryapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(private val apiService: ApiService, private val pagingConfig: PagingConfig) {

    fun getStoryPaging(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).flow
    }

    fun upload(
        image: File,
        desc: String
    ) = liveData {
        emit(Result.Loading)
        try {
            val uploadResponse = apiService.upload(
                MultipartBody.Part.createFormData(
                    "photo",
                    image.name,
                    image.asRequestBody("image/jpeg".toMediaTypeOrNull())
                ),
                desc.toRequestBody("text/plain".toMediaType())
            )

            emit(Result.Success(uploadResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getStoriesWithLocation(location: Int) = liveData {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getStoriesWithLocation(location)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }
}