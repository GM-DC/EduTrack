package org.owlcode.edutrack.features.app.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.owlcode.edutrack.data.di.dataModule
import org.owlcode.edutrack.features.app.AppViewModel
import org.owlcode.edutrack.features.calendar.di.calendarModule
import org.owlcode.edutrack.features.courses.di.courseModule
import org.owlcode.edutrack.features.login.di.loginModule
import org.owlcode.edutrack.sync.SyncManager

val appModule = module {
    includes(dataModule, loginModule, calendarModule, courseModule)
    single { SyncManager(get()) }
    viewModel { AppViewModel(get(), get()) }
}



