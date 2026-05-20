package com.example.health.di

import com.example.health.ui.catalog.MedicationCatalogViewModel
import com.example.health.ui.dashboard.DashboardViewModel
import com.example.health.ui.health.StateOfHealthViewModel
import com.example.health.ui.photos.PhotosViewModel
import com.example.health.ui.pressure.PressureViewModel
import com.example.health.ui.profile.ProfileViewModel
import com.example.health.ui.reminders.RemindersViewModel
import com.example.health.ui.report.ReportViewModel
import com.example.health.ui.screenings.ScreeningsViewModel
import com.example.health.ui.sos.SosViewModel
import com.example.health.ui.steps.StepsViewModel
import com.example.health.ui.weight.WeightViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { StepsViewModel(get(), get(), get()) }
    viewModel { WeightViewModel(get(), get(), get()) }
    viewModel { PressureViewModel(get(), get(), get()) }
    viewModel { StateOfHealthViewModel(get(), get(), get()) }
    viewModel { PhotosViewModel(get(), get(), get(), get(), get()) }
    viewModel { ReportViewModel(get(), get()) }
    viewModel { RemindersViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ScreeningsViewModel(get(), get(), get()) }
    viewModel { SosViewModel(get()) }
    viewModel { MedicationCatalogViewModel(get(), get()) }
}
