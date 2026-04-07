package org.owlcode.edutrack.data.di

import org.koin.dsl.module
import org.owlcode.edutrack.core.di.coreModule
import org.owlcode.edutrack.data.local.datasource.ClaseLocalDataSource
import org.owlcode.edutrack.data.local.datasource.ExamenLocalDataSource
import org.owlcode.edutrack.data.local.datasource.PersonalLocalDataSource
import org.owlcode.edutrack.data.local.datasource.TareaLocalDataSource
import org.owlcode.edutrack.data.repository.ClaseRepositoryImpl
import org.owlcode.edutrack.data.repository.ExamenRepositoryImpl
import org.owlcode.edutrack.data.repository.MockAuthRepository
import org.owlcode.edutrack.data.repository.MockCalendarRepository
import org.owlcode.edutrack.data.repository.MockCourseRepository
import org.owlcode.edutrack.data.repository.PersonalRepositoryImpl
import org.owlcode.edutrack.data.repository.TareaRepositoryImpl
import org.owlcode.edutrack.domain.repository.AuthRepository
import org.owlcode.edutrack.domain.repository.CalendarRepository
import org.owlcode.edutrack.domain.repository.ClaseRepository
import org.owlcode.edutrack.domain.repository.CourseRepository
import org.owlcode.edutrack.domain.repository.ExamenRepository
import org.owlcode.edutrack.domain.repository.PersonalRepository
import org.owlcode.edutrack.domain.repository.TareaRepository

val dataModule = module {
    includes(coreModule)

    // ── Auth / Courses (mock hasta tener backend) ──────────────────────────
    single<AuthRepository>    { MockAuthRepository() }
    single<CourseRepository>  { MockCourseRepository() }
    // CalendarRepository necesario para SyncManager (mock hasta tener backend)
    single<CalendarRepository>{ MockCalendarRepository() }

    // ── Datasources locales ────────────────────────────────────────────────
    single { ClaseLocalDataSource(get()) }
    single { TareaLocalDataSource(get()) }
    single { ExamenLocalDataSource(get()) }
    single { PersonalLocalDataSource(get()) }

    // ── Repositorios tipados ───────────────────────────────────────────────
    single<ClaseRepository>   { ClaseRepositoryImpl(get()) }
    single<TareaRepository>   { TareaRepositoryImpl(get()) }
    single<ExamenRepository>  { ExamenRepositoryImpl(get()) }
    single<PersonalRepository>{ PersonalRepositoryImpl(get()) }
}
