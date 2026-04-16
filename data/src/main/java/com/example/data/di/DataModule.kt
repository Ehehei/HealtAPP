package com.example.data.di

import androidx.room.Room
import com.example.data.analysis.BodyPhotoDiffAnalyzer
import com.example.data.local.db.HealthDatabase
import com.example.data.report.PdfReportGenerator
import com.example.data.repository.BloodPressureRepositoryImpl
import com.example.data.repository.BodyPhotoRepositoryImpl
import com.example.data.repository.StateOfHealthRepositoryImpl
import com.example.data.repository.StepRepositoryImpl
import com.example.data.repository.UserProfileRepositoryImpl
import com.example.data.repository.WeightRepositoryImpl
import com.example.data.source.HealthConnectDataSourceImpl
import com.example.data.storage.PhotoStorage
import com.example.domain.repository.BloodPressureRepository
import com.example.domain.repository.BodyPhotoRepository
import com.example.domain.repository.HealthConnectDataSource
import com.example.domain.repository.StateOfHealthRepository
import com.example.domain.repository.StepRepository
import com.example.domain.repository.UserProfileRepository
import com.example.domain.repository.WeightRepository
import com.example.domain.usecase.bloodpressure.GetBloodPressureStatsUseCase
import com.example.domain.usecase.bloodpressure.ObserveBloodPressureHistoryUseCase
import com.example.domain.usecase.bloodpressure.SaveBloodPressureUseCase
import com.example.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.example.domain.usecase.health.GetHealthTrendUseCase
import com.example.domain.usecase.health.SaveStateOfHealthUseCase
import com.example.domain.usecase.photo.GetPhotoComparisonPairsUseCase
import com.example.domain.usecase.photo.ObserveBodyPhotosByTypeUseCase
import com.example.domain.usecase.photo.SaveBodyPhotoUseCase
import com.example.domain.usecase.profile.CalculateBmiUseCase
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import com.example.domain.usecase.profile.GetCurrentUserProfileUseCase
import com.example.domain.usecase.report.GenerateHealthReportDataUseCase
import com.example.domain.usecase.steps.GetDailyStepSummaryUseCase
import com.example.domain.usecase.steps.GetWeeklyStepStatsUseCase
import com.example.domain.usecase.steps.SyncStepsFromHealthConnectUseCase
import com.example.domain.usecase.weight.GetWeightProgressUseCase
import com.example.domain.usecase.weight.SaveWeightRecordUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {

    // ---- Database ----
    single {
        Room.databaseBuilder(
            androidContext(),
            HealthDatabase::class.java,
            "health_database",
        ).build()
    }

    single { get<HealthDatabase>().bloodPressureDao() }
    single { get<HealthDatabase>().bodyPhotoDao() }
    single { get<HealthDatabase>().stateOfHealthDao() }
    single { get<HealthDatabase>().stepDao() }
    single { get<HealthDatabase>().userProfileDao() }
    single { get<HealthDatabase>().weightDao() }

    // ---- Repositories ----
    singleOf(::BloodPressureRepositoryImpl) { bind<BloodPressureRepository>() }
    singleOf(::BodyPhotoRepositoryImpl) { bind<BodyPhotoRepository>() }
    singleOf(::StateOfHealthRepositoryImpl) { bind<StateOfHealthRepository>() }
    singleOf(::StepRepositoryImpl) { bind<StepRepository>() }
    singleOf(::UserProfileRepositoryImpl) { bind<UserProfileRepository>() }
    singleOf(::WeightRepositoryImpl) { bind<WeightRepository>() }

    // ---- External sources ----
    single<HealthConnectDataSource> { HealthConnectDataSourceImpl(androidContext()) }

    // ---- Storage / utilities ----
    single { PhotoStorage(androidContext()) }
    single { BodyPhotoDiffAnalyzer(get()) }
    single { PdfReportGenerator(androidContext()) }
}

val domainModule = module {
    // Blood pressure
    single { SaveBloodPressureUseCase(get()) }
    single { ObserveBloodPressureHistoryUseCase(get()) }
    single { GetBloodPressureStatsUseCase(get()) }

    // Dashboard
    single { GetDashboardSummaryUseCase(get(), get(), get(), get(), get()) }

    // Health
    single { SaveStateOfHealthUseCase(get()) }
    single { GetHealthTrendUseCase(get()) }

    // Photo
    single { SaveBodyPhotoUseCase(get()) }
    single { ObserveBodyPhotosByTypeUseCase(get()) }
    single { GetPhotoComparisonPairsUseCase(get()) }

    // Profile
    single { GetCurrentUserProfileUseCase(get()) }
    single { CalculateBmiUseCase(get(), get()) }
    single { CalculateUserAgeUseCase() }

    // Report
    single { GenerateHealthReportDataUseCase(get(), get(), get(), get(), get()) }

    // Steps
    single { GetDailyStepSummaryUseCase(get()) }
    single { GetWeeklyStepStatsUseCase(get()) }
    single { SyncStepsFromHealthConnectUseCase(get(), get()) }

    // Weight
    single { SaveWeightRecordUseCase(get()) }
    single { GetWeightProgressUseCase(get(), get()) }
}
