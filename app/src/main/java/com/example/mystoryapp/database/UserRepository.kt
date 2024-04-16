package com.example.mystoryapp.database

import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.data.response.RegisterResponse
import com.example.mystoryapp.data.retrofit.ApiService

class UserRepository(private val apiService: ApiService) {
    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }
}