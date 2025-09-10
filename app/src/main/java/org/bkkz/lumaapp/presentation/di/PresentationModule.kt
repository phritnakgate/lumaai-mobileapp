package org.bkkz.lumaapp.presentation.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

import org.bkkz.lumaapp.presentation.auth.login.LoginViewModel

val presentationModules = module {
    viewModel { LoginViewModel(get()) }
}