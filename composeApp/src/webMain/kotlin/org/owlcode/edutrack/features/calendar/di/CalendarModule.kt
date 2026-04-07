package org.owlcode.edutrack.features.calendar.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.owlcode.edutrack.features.calendar.CalendarViewModel

val calendarModule = module {
    viewModel {
        CalendarViewModel(
            claseRepository    = get(),
            tareaRepository    = get(),
            examenRepository   = get(),
            personalRepository = get(),
            courseRepository   = get(),
            authRepository     = get()
        )
    }
}
