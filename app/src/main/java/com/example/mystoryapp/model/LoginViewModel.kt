package com.example.mystoryapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.database.UserRepository
import com.example.mystoryapp.preferences.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val pref: UserPreference
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val loginResponse = MutableLiveData<LoginResponse>()
    val errorMessage = MutableLiveData<String>()

    suspend fun login(email: String, password: String): LoginResponse {
        return userRepository.loginUser(email, password)
    }

    fun isLoggedIn(): LiveData<Boolean> {
        return pref.isLoggedIn().asLiveData()
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        viewModelScope.launch {
            pref.setLoggedIn(isLoggedIn)
        }
    }

    fun setToken(token: String) {
        viewModelScope.launch {
            pref.setAuthToken(token)
        }
    }

    fun getToken() = pref.getAuthToken().asLiveData()
}

