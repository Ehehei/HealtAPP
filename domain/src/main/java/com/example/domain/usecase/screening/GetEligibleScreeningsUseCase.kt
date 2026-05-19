package com.example.domain.usecase.screening

import com.example.domain.model.screening.Screening
import com.example.domain.model.screening.ScreeningEligibility
import com.example.domain.model.screening.ScreeningRecord
import com.example.domain.model.screening.ScreeningStatus
import com.example.domain.repository.ScreeningCatalog
import com.example.domain.repository.ScreeningRecordRepository
import com.example.domain.repository.UserProfileRepository
import com.example.domain.usecase.profile.CalculateUserAgeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Объединяет каталог скринингов, профиль пользователя и историю прохождений
 * и выдаёт по каждому скринингу статус с датой следующего срока.
 */
class GetEligibleScreeningsUseCase(
    private val catalog: ScreeningCatalog,
    private val recordRepository: ScreeningRecordRepository,
    private val profileRepository: UserProfileRepository,
    private val calculateAge: CalculateUserAgeUseCase,
) {
    operator fun invoke(userId: String): Flow<List<ScreeningEligibility>> =
        combine(
            profileRepository.observeById(userId),
            recordRepository.observeByUserId(userId),
        ) { profile, records ->
            val age = profile?.birthDate?.let { calculateAge(it) }
            catalog.all().map { screening ->
                evaluate(screening, age, profile?.gender, records, LocalDate.now())
            }
        }

    fun once(
        screenings: List<Screening>,
        age: Int?,
        gender: com.example.domain.model.Gender?,
        records: List<ScreeningRecord>,
        today: LocalDate = LocalDate.now(),
    ): List<ScreeningEligibility> =
        screenings.map { evaluate(it, age, gender, records, today) }

    private fun evaluate(
        screening: Screening,
        age: Int?,
        gender: com.example.domain.model.Gender?,
        records: List<ScreeningRecord>,
        today: LocalDate,
    ): ScreeningEligibility {
        val ageOk = age != null && age in screening.ageRange
        val genderOk = screening.eligibleGender == null ||
            (gender != null && gender == screening.eligibleGender)

        if (!ageOk || !genderOk) {
            return ScreeningEligibility(screening, ScreeningStatus.NOT_ELIGIBLE)
        }

        val last = records.filter { it.screeningCode == screening.code }.maxByOrNull { it.date }
        if (last == null) {
            return ScreeningEligibility(screening, ScreeningStatus.DUE_NOW)
        }
        if (screening.intervalMonths <= 0) {
            // Однократный — пройдено, считаем UPCOMING без даты.
            return ScreeningEligibility(screening, ScreeningStatus.UPCOMING, last.date)
        }
        val nextDue = last.date.plusMonths(screening.intervalMonths.toLong())
        val status = if (!nextDue.isAfter(today)) ScreeningStatus.DUE_NOW else ScreeningStatus.UPCOMING
        return ScreeningEligibility(screening, status, last.date, nextDue)
    }
}
