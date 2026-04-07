package org.owlcode.edutrack.features.login.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.owlcode.edutrack.features.login.LoginViewModel

val loginModule = module {
    viewModel { LoginViewModel(get()) }
}

