package org.bkkz.lumaapp.data.di

import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.remote.AuthRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val remoteDataModules = module {
    single { AuthRepository(get()) }
    single { TokenManager(androidContext()) }
}