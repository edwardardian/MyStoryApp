package com.example.mystoryapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.StoryViewModel
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.preferences.UserPreference

class StoryViewModelFactory(
    private val storyRepository: StoryRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            return StoryViewModel(storyRepository, userPreference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
