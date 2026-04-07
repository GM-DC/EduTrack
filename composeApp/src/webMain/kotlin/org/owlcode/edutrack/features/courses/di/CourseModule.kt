package org.owlcode.edutrack.features.courses.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.owlcode.edutrack.features.courses.CourseDetailViewModel
import org.owlcode.edutrack.features.courses.CourseViewModel

val courseModule = module {
    viewModel { CourseViewModel(get()) }
    viewModel { params ->
        CourseDetailViewModel(
            courseId         = params.get(),
            courseRepository = get(),
            claseRepository  = get(),
            tareaRepository  = get(),
            examenRepository = get()
        )
    }
}
