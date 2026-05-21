package com.project.luckysevens.data.online

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://luckysevensapklabs-default-rtdb.europe-west1.firebasedatabase.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val instance: FirebaseApiService by lazy {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .addCallAdapterFactory(
                RxJava3CallAdapterFactory.create()
            )
            .build()

        retrofit.create(FirebaseApiService::class.java)
    }
}