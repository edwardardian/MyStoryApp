package com.example.mystoryapp.data.retrofit

import com.example.mystoryapp.LoginActivity.Companion.TOKEN
import kotlinx.coroutines.flow.Flow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
    companion object {
        fun getApiService(token: String): ApiService {
            val loggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = if (token != TOKEN) {
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(Interceptor { chain ->
                        val req = chain.request()
                        val requestHeaders = req.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                        chain.proceed(requestHeaders)
                    })
                    .build()
            } else {
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            }
            val retrofit = Retrofit.Builder()
                .baseUrl("https://story-api.dicoding.dev/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}