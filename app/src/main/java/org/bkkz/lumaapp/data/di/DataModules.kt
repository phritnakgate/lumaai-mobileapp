package org.bkkz.lumaapp.data.di

import androidx.room.Room
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.local.UserChatDatabase
import org.bkkz.lumaapp.data.Repository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModules = module {
    single { Repository(get(), get()) }
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
}