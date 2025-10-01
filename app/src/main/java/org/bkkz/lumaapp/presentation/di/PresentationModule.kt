package org.bkkz.lumaapp.presentation.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

import org.bkkz.lumaapp.presentation.auth.login.LoginViewModel
import org.bkkz.lumaapp.presentation.auth.register.RegisterViewModel
import org.bkkz.lumaapp.presentation.main.chat.ChatViewModel
import org.bkkz.lumaapp.presentation.main.chat_history.ChatHistoryViewModel
import org.bkkz.lumaapp.presentation.main.home.HomeViewModel
import org.bkkz.lumaapp.presentation.main.report.ReportViewModel
import org.bkkz.lumaapp.presentation.main.task.add_task.AddTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.edit_task.EditTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel

val presentationModules = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ViewTaskViewModel(get()) }
    viewModel { AddTaskViewModel(get()) }
    viewModel { EditTaskViewModel(get()) }
    viewModel { ChatHistoryViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ReportViewModel(get()) }
}