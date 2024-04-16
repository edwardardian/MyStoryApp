package com.example.mystoryapp.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystoryapp.database.StoryRepository
import java.io.File

class UploadViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    val imageFile = MutableLiveData<File>()
    val isLoading = MutableLiveData<Boolean>()
    val errorText = MutableLiveData<String>()

    fun upload(
        description: String,
    ) = storyRepository.upload(
        imageFile.value!!,
        description
    )
}