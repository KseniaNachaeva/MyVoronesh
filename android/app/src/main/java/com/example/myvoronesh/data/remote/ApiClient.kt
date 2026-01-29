package com.example.myvoronesh.data.remote

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    //private const val BASE_URL = "http://192.168.0.105:8080/"

    private var tokenManager: TokenManager? = null
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        tokenManager = TokenManager.getInstance(context)
        retrofit = null
    }

    private fun getAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            val token = tokenManager?.token

            val newRequest = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .apply {
                    if (!token.isNullOrEmpty() && !originalRequest.url.encodedPath.contains("/auth")) {
                        addHeader("Authorization", "Bearer $token")
                    }

                }
                .build()

            Log.d("ApiClient", "Request: ${newRequest.url}")
            Log.d("ApiClient", "Token: ${token?.take(20)}...")

            chain.proceed(newRequest)
        }
    }

    private fun getClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("ApiClient", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(getAuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    val apiService: ApiService by lazy {
        getRetrofit().create(ApiService::class.java)
    }

    fun refreshClient() {
        retrofit = null
    }
}