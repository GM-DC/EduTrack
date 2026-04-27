package org.owlcode.edutrack.data.di

import org.koin.dsl.module
import org.owlcode.edutrack.core.di.coreModule
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.ClaseLocalDataSource
import org.owlcode.edutrack.data.local.datasource.CourseLocalDataSource
import org.owlcode.edutrack.data.local.datasource.ExamenLocalDataSource
import org.owlcode.edutrack.data.local.datasource.PersonalLocalDataSource
import org.owlcode.edutrack.data.local.datasource.TareaLocalDataSource
import org.owlcode.edutrack.data.remote.api.AuthApiService
import org.owlcode.edutrack.data.remote.api.ClaseApiService
import org.owlcode.edutrack.data.remote.api.CourseApiService
import org.owlcode.edutrack.data.remote.api.EventoPersonalApiService
import org.owlcode.edutrack.data.remote.api.ExamenApiService
import org.owlcode.edutrack.data.remote.api.TareaApiService
import org.owlcode.edutrack.data.repository.AuthRepositoryImpl
import org.owlcode.edutrack.data.repository.ClaseRepositoryImpl
import org.owlcode.edutrack.data.repository.CourseRepositoryImpl
import org.owlcode.edutrack.data.repository.ExamenRepositoryImpl
import org.owlcode.edutrack.data.repository.MockCalendarRepository
import org.owlcode.edutrack.data.repository.PersonalRepositoryImpl
import org.owlcode.edutrack.data.repository.TareaRepositoryImpl
import org.owlcode.edutrack.domain.repository.AuthRepository
import org.owlcode.edutrack.domain.repository.CalendarRepository
import org.owlcode.edutrack.domain.repository.ClaseRepository
import org.owlcode.edutrack.domain.repository.CourseRepository
import org.owlcode.edutrack.domain.repository.ExamenRepository
import org.owlcode.edutrack.domain.repository.PersonalRepository
import org.owlcode.edutrack.core.config.AppConfig
import org.owlcode.edutrack.domain.repository.TareaRepository

val dataModule = module {
    includes(coreModule)

    // ── API Services ──────────────────────────────────────────────────────────
    // BASE_URL se toma de AppConfig (generado por Gradle desde .env o variable de entorno)
    single { AuthApiService(get(), AppConfig.API_BASE_URL) }
    single { CourseApiService(get(), AppConfig.API_BASE_URL) }
    single { ClaseApiService(get(), AppConfig.API_BASE_URL) }
    single { TareaApiService(get(), AppConfig.API_BASE_URL) }
    single { ExamenApiService(get(), AppConfig.API_BASE_URL) }
    single { EventoPersonalApiService(get(), AppConfig.API_BASE_URL) }

    // ── Datasources locales ────────────────────────────────────────────────────
    single { AuthLocalDataSource(get()) }
    single { CourseLocalDataSource(get()) }
    single { ClaseLocalDataSource(get()) }
    single { TareaLocalDataSource(get()) }
    single { ExamenLocalDataSource(get()) }
    single { PersonalLocalDataSource(get()) }

    // ── Repositorios ──────────────────────────────────────────────────────────
    single<AuthRepository>     { AuthRepositoryImpl(get(), get()) }
    single<CourseRepository>   { CourseRepositoryImpl(get(), get(), get()) }
    single<ClaseRepository>    { ClaseRepositoryImpl(get(), get(), get()) }
    single<TareaRepository>    { TareaRepositoryImpl(get(), get(), get()) }
    single<ExamenRepository>   { ExamenRepositoryImpl(get(), get(), get()) }
    single<PersonalRepository> { PersonalRepositoryImpl(get(), get(), get()) }

    // CalendarRepository sólo lo usa SyncManager; no tiene endpoint directo en el API
    single<CalendarRepository> { MockCalendarRepository() }
}
