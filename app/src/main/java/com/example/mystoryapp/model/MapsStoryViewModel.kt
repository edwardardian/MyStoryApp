package com.example.mystoryapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.StoryResponse
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapsStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val errorMessage = MutableLiveData<String>()

    init {
        getStoriesWithLocation(1)
    }

    fun getStoriesWithLocation(location: Int) {
        viewModelScope.launch {
            storyRepository.getStoriesWithLocation(location).asFlow().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        isLoading.postValue(true)
                    }

                    is Result.Success -> {
                        isLoading.postValue(false)
                        storiesWithLocation.postValue(result.data.listStory as ArrayList<ListStoryItem>?)
                    }

                    is Result.Error -> {
                        isLoading.postValue(false)
                        errorMessage.postValue(result.error)
                    }
                }
            }
        }
    }
}
