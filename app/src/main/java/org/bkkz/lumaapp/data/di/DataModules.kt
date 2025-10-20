package org.bkkz.lumaapp.data.di

import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.local.AppDatabase
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.AuthInterceptor
import org.bkkz.lumaapp.data.remote.LumaApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val dataModules = module {
    single { Repository(get(), get(), get(), get()) }
    single { TokenManager(androidContext()) }

    //RoomDB
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "AppDatabase"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        get<AppDatabase>().userChatDao()
    }
    single{
        get<AppDatabase>().userReportDao()
    }

    //OkHttp & Retrofit & Interceptor
    single { OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("https://lumaai-backend-672244117841.asia-southeast1.run.app/api/") // http://10.0.2.2:8080/api/
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>().create(LumaApi::class.java)
    }
}