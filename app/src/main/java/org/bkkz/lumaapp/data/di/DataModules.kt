package org.bkkz.lumaapp.data.di

import androidx.room.Room
import okhttp3.OkHttpClient
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.local.UserChatDatabase
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.LumaApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModules = module {
    single { Repository(get(), get(), get()) }
    single { TokenManager(androidContext()) }

    //RoomDB
    single {
        Room.databaseBuilder(
            androidContext(),
            UserChatDatabase::class.java, "userChat"
        ).build()
    }
    single {
        get<UserChatDatabase>().userChatDao()
    }

    //OkHttp & Retrofit
    single { OkHttpClient.Builder().build() }
    single {
        Retrofit.Builder()
            .baseUrl("https://lumaai-backend-672244117841.asia-southeast1.run.app/api/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>().create(LumaApi::class.java)
    }
}