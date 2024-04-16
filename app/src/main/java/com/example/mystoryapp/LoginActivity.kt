package com.example.mystoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.MainActivity.Companion.KEY_TOKEN
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.database.UserRepository
import com.example.mystoryapp.databinding.ActivityLoginBinding
import com.example.mystoryapp.model.LoginViewModel
import com.example.mystoryapp.model.LoginViewModelFactory
import com.example.mystoryapp.preferences.UserPreference
import com.example.mystoryapp.preferences.datastore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val FIELD_REQUIRED = "Please fill this blank field!"
        const val TOKEN = "AUTH_TOKEN"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var userPreference: UserPreference
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiConfig.getApiService(TOKEN)
        userRepository = UserRepository(apiService)
        userPreference = UserPreference.getInstance(applicationContext.datastore)
        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(userRepository, userPreference)).get(
                LoginViewModel::class.java
            )

        observeViewModel()

        binding.btnLogin.setOnClickListener {
            checkUser()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkUser() {
        val email: String = binding.edLoginEmail.text.toString().trim()
        val password: String = binding.edLoginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, FIELD_REQUIRED, Toast.LENGTH_SHORT).show()
            return
        }
        userLogin(email, password)
    }

    private fun userLogin(email: String, password: String) {
        lifecycleScope.launch {
            loginViewModel.isLoading.postValue(true)
            try {
                val response = loginViewModel.login(email, password)
                loginViewModel.loginResponse.postValue(response)
                loginViewModel.isLoading.postValue(false)
            } catch (e: Exception) {
                loginViewModel.errorMessage.postValue(e.message)
                loginViewModel.isLoading.postValue(false)
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.apply {
            getToken().observe(this@LoginActivity) { token ->
                if (token.isNotEmpty()) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra(KEY_TOKEN, token)
                    startActivity(intent)
                }
            }

            isLoading.observe(this@LoginActivity) {
                showLoading(it)
            }

            loginResponse.observe(this@LoginActivity) { response ->
                val token = response.loginResult!!.token
                if (!token.isNullOrEmpty()) {
                    loginViewModel.setLoggedIn(true)
                    loginViewModel.setToken(token)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Token is empty or missing.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnLogin.isVisible = !isLoading
        }
    }
}
