package com.example.mystoryapp.model

import androidx.lifecycle.ViewModel
import com.example.mystoryapp.data.response.RegisterResponse
import com.example.mystoryapp.database.UserRepository

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return userRepository.registerUser(name, email, password)
    }
}
