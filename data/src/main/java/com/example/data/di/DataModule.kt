package com.example.data.di

import androidx.room.Room
import com.example.data.analysis.BodyPhotoDiffAnalyzer
import com.example.data.catalog.KzMedicationCatalog
import com.example.data.catalog.KzScreeningCatalog
import com.example.data.local.db.HealthDatabase
import com.example.data.local.db.MIGRATION_1_2
import com.example.data.local.db.MIGRATION_2_3
import com.example.data.local.db.MIGRATION_3_4
import com.example.data.local.db.MIGRATION_4_5
import com.example.data.notifications.ReminderNotifier
import com.example.data.notifications.ReminderScheduler
import com.example.data.report.PdfReportGenerator
import com.example.data.repository.BloodPressureRepositoryImpl
import com.example.data.repository.BodyPhotoRepositoryImpl
import com.example.data.repository.MedicationIntakeRepositoryImpl
import com.example.data.repository.MedicationRepositoryImpl
import com.example.data.repository.ReminderRepositoryImpl
import com.example.data.repository.ScreeningRecordRepositoryImpl
import com.example.data.repository.StateOfHealthRepositoryImpl
import com.example.data.repository.StepRepositoryImpl
import com.example.data.repository.UserProfileRepositoryImpl
import com.example.data.repository.WeightRepositoryImpl
import com.example.data.source.HealthConnectDataSourceImpl
import com.example.data.storage.PhotoStorage
import com.example.domain.repository.BloodPressureRepository
import com.example.domain.repository.BodyPhotoRepository
import com.example.domain.repository.HealthConnectDataSource
import com.example.domain.repository.MedicationCatalogRepository
import com.example.domain.repository.MedicationIntakeRepository
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.ReminderRepository
import com.example.domain.repository.ScreeningCatalog
import com.example.domain.repository.ScreeningRecordRepository
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
import com.example.domain.usecase.medication.DeleteMedicationUseCase
import com.example.domain.usecase.medication.LogMedicationIntakeUseCase
import com.example.domain.usecase.medication.ObserveMedicationIntakesUseCase
import com.example.domain.usecase.medication.ObserveMedicationsUseCase
import com.example.domain.usecase.medication.SaveMedicationUseCase
import com.example.domain.usecase.medication.SearchMedicationCatalogUseCase
import com.example.domain.usecase.photo.GetPhotoComparisonPairsUseCase
import com.example.domain.usecase.photo.ObserveBodyPhotosByTypeUseCase
import com.example.domain.usecase.photo.SaveBodyPhotoUseCase
import com.example.domain.usecase.profile.CalculateBmiUseCase
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import com.example.domain.usecase.profile.GetCurrentUserProfileUseCase
import com.example.domain.usecase.reminder.DeleteReminderUseCase
import com.example.domain.usecase.reminder.ObserveRemindersUseCase
import com.example.domain.usecase.reminder.SaveReminderUseCase
import com.example.domain.usecase.reminder.ToggleReminderUseCase
import com.example.domain.usecase.report.GenerateHealthReportDataUseCase
import com.example.domain.usecase.screening.GetEligibleScreeningsUseCase
import com.example.domain.usecase.screening.LogScreeningUseCase
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

    single {
        Room.databaseBuilder(
            androidContext(),
            HealthDatabase::class.java,
            "health_database",
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    single { get<HealthDatabase>().bloodPressureDao() }
    single { get<HealthDatabase>().bodyPhotoDao() }
    single { get<HealthDatabase>().stateOfHealthDao() }
    single { get<HealthDatabase>().stepDao() }
    single { get<HealthDatabase>().userProfileDao() }
    single { get<HealthDatabase>().weightDao() }
    single { get<HealthDatabase>().medicationDao() }
    single { get<HealthDatabase>().reminderDao() }
    single { get<HealthDatabase>().screeningRecordDao() }
    single { get<HealthDatabase>().medicationIntakeDao() }

    singleOf(::BloodPressureRepositoryImpl) { bind<BloodPressureRepository>() }
    singleOf(::BodyPhotoRepositoryImpl) { bind<BodyPhotoRepository>() }
    singleOf(::StateOfHealthRepositoryImpl) { bind<StateOfHealthRepository>() }
    singleOf(::StepRepositoryImpl) { bind<StepRepository>() }
    singleOf(::UserProfileRepositoryImpl) { bind<UserProfileRepository>() }
    singleOf(::WeightRepositoryImpl) { bind<WeightRepository>() }
    singleOf(::MedicationRepositoryImpl) { bind<MedicationRepository>() }
    singleOf(::ReminderRepositoryImpl) { bind<ReminderRepository>() }
    singleOf(::ScreeningRecordRepositoryImpl) { bind<ScreeningRecordRepository>() }
    singleOf(::MedicationIntakeRepositoryImpl) { bind<MedicationIntakeRepository>() }

    single<ScreeningCatalog> { KzScreeningCatalog() }
    single<MedicationCatalogRepository> { KzMedicationCatalog() }

    single<HealthConnectDataSource> { HealthConnectDataSourceImpl(androidContext()) }

    single { PhotoStorage(androidContext()) }
    single { BodyPhotoDiffAnalyzer(get()) }
    single { PdfReportGenerator(androidContext()) }

    single { ReminderScheduler(androidContext()) }
    single { ReminderNotifier(androidContext()) }
}

val domainModule = module {

    single { SaveBloodPressureUseCase(get()) }
    single { ObserveBloodPressureHistoryUseCase(get()) }
    single { GetBloodPressureStatsUseCase(get()) }

    single { GetDashboardSummaryUseCase(get(), get(), get(), get(), get()) }

    single { SaveStateOfHealthUseCase(get()) }
    single { GetHealthTrendUseCase(get()) }

    single { SaveBodyPhotoUseCase(get()) }
    single { ObserveBodyPhotosByTypeUseCase(get()) }
    single { GetPhotoComparisonPairsUseCase(get()) }

    single { GetCurrentUserProfileUseCase(get()) }
    single { CalculateBmiUseCase(get(), get()) }
    single { CalculateUserAgeUseCase() }

    single {
        GenerateHealthReportDataUseCase(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(),
            get(), get(),
        )
    }

    single { GetDailyStepSummaryUseCase(get()) }
    single { GetWeeklyStepStatsUseCase(get()) }
    single { SyncStepsFromHealthConnectUseCase(get(), get()) }

    single { SaveWeightRecordUseCase(get()) }
    single { GetWeightProgressUseCase(get(), get()) }

    single { SaveMedicationUseCase(get()) }
    single { ObserveMedicationsUseCase(get()) }
    single { DeleteMedicationUseCase(get()) }
    single { SearchMedicationCatalogUseCase(get()) }
    single { LogMedicationIntakeUseCase(get()) }
    single { ObserveMedicationIntakesUseCase(get()) }

    single { SaveReminderUseCase(get()) }
    single { ObserveRemindersUseCase(get()) }
    single { ToggleReminderUseCase(get()) }
    single { DeleteReminderUseCase(get()) }

    single { GetEligibleScreeningsUseCase(get(), get(), get(), get()) }
    single { LogScreeningUseCase(get(), get()) }
}
