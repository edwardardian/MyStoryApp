package com.example.mystoryapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.preferences.UserPreference
import kotlinx.coroutines.launch

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val pref: UserPreference
) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>().apply { value = false }
    val storiesResponse: LiveData<PagingData<ListStoryItem>> = MutableLiveData()
    val errorMessage = MutableLiveData<String>()

    init {
        getStories()
    }

    fun getStories() {
        isLoading.postValue(true)
        viewModelScope.launch {
            storyRepository.getStoryPaging().collect { pagingData ->
                (storiesResponse as MutableLiveData).postValue(pagingData)

                isLoading.postValue(false)
            }
        }
    }

    fun clearPreferences() {
        viewModelScope.launch {
            pref.clearPrefs()
        }
    }
}

