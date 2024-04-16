package com.example.mystoryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.database.UserRepository
import com.example.mystoryapp.databinding.ActivityRegisterBinding
import com.example.mystoryapp.model.RegisterViewModel
import com.example.mystoryapp.model.RegisterViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    companion object {
        private const val FIELD_REQUIRED = "Please fill this blank field!"
        private const val TOKEN = "AUTH_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiConfig.getApiService(TOKEN)
        val userRepository = UserRepository(apiService)

        val viewModelFactory = RegisterViewModelFactory(userRepository)

        viewModel = ViewModelProvider(this, viewModelFactory).get(RegisterViewModel::class.java)

        binding.btnRegister.setOnClickListener {
            checkUser()
        }
    }

    private fun checkUser() {
        var name: String = binding.edRegisterName.text.toString().trim()
        var email: String = binding.edRegisterEmail.text.toString().trim()
        var password: String = binding.edRegisterPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.edRegisterName.error = FIELD_REQUIRED
        }

        if (email.isEmpty()) {
            binding.edRegisterEmail.error = FIELD_REQUIRED
        }

        if (password.isEmpty()) {
            binding.edRegisterPassword.error = FIELD_REQUIRED
        }

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            userRegister(name, email, password)
        }
    }

    private fun userRegister(name: String, email: String, password: String) {
        lifecycleScope.launch {
            val response = viewModel.registerUser(name, email, password)
            if (response.error == false) {
                Toast.makeText(this@RegisterActivity, "Register Success!", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
//                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@RegisterActivity, "Register Failed: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
