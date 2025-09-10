package org.bkkz.lumaapp

import android.app.Application
import org.bkkz.lumaapp.data.di.remoteDataModules
import org.bkkz.lumaapp.presentation.di.presentationModules
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin()
    }

    private fun startKoin(){
        org.koin.core.context.startKoin {
            androidContext(this@MyApplication)
            modules(listOf(presentationModules, remoteDataModules))
        }
    }
}