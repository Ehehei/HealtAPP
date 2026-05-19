package com.example.data.mapper

import com.example.data.local.entity.BloodPressureEntity
import com.example.data.local.entity.BodyPhotoEntity
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.MedicationIntakeEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.ScreeningRecordEntity
import com.example.data.local.entity.StateOfHealthEntity
import com.example.data.local.entity.StepEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WeightEntity
import com.example.domain.model.BloodPressure
import com.example.domain.model.BloodType
import com.example.domain.model.BodyPhoto
import com.example.domain.model.FeelingLevel
import com.example.domain.model.Gender
import com.example.domain.model.Medication
import com.example.domain.model.MedicationForm
import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.model.PhotoType
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.domain.model.screening.ScreeningRecord
import com.example.domain.model.StateOfHealth
import com.example.domain.model.StepRecord
import com.example.domain.model.UserProfile
import com.example.domain.model.WeightRecord
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private val zone: ZoneId = ZoneId.systemDefault()

fun LocalDateTime.toEpochMillis(): Long =
    atZone(zone).toInstant().toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

// ----- BloodPressure -----
fun BloodPressureEntity.toDomain() = BloodPressure(
    id = id,
    userId = userId,
    systolicPressure = systolicPressure,
    diastolicPressure = diastolicPressure,
    pulse = pulse,
    date = dateEpochMillis.toLocalDateTime(),
)

fun BloodPressure.toEntity() = BloodPressureEntity(
    id = id,
    userId = userId,
    systolicPressure = systolicPressure,
    diastolicPressure = diastolicPressure,
    pulse = pulse,
    dateEpochMillis = date.toEpochMillis(),
)

// ----- BodyPhoto -----
fun BodyPhotoEntity.toDomain() = BodyPhoto(
    id = id,
    userId = userId,
    filePath = filePath,
    type = PhotoType.valueOf(type),
    note = note,
    date = dateEpochMillis.toLocalDateTime(),
)

fun BodyPhoto.toEntity() = BodyPhotoEntity(
    id = id,
    userId = userId,
    filePath = filePath,
    type = type.name,
    note = note,
    dateEpochMillis = date.toEpochMillis(),
)

// ----- StateOfHealth -----
fun StateOfHealthEntity.toDomain() = StateOfHealth(
    id = id,
    userId = userId,
    feelingLevel = FeelingLevel.valueOf(feelingLevel),
    bloodSugar = bloodSugar,
    temperature = temperature,
    notes = notes,
    date = LocalDate.ofEpochDay(dateEpochDay),
)

fun StateOfHealth.toEntity() = StateOfHealthEntity(
    id = id,
    userId = userId,
    feelingLevel = feelingLevel.name,
    bloodSugar = bloodSugar,
    temperature = temperature,
    notes = notes,
    dateEpochDay = date.toEpochDay(),
)

// ----- Step -----
fun StepEntity.toDomain() = StepRecord(
    id = id,
    userId = userId,
    steps = steps,
    date = LocalDate.ofEpochDay(dateEpochDay),
)

fun StepRecord.toEntity() = StepEntity(
    id = id,
    userId = userId,
    steps = steps,
    dateEpochDay = date.toEpochDay(),
)

// ----- UserProfile -----
fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    name = name,
    height = height,
    initialWeightKg = initialWeightKg,
    birthDate = LocalDate.ofEpochDay(birthDateEpochDay),
    gender = Gender.valueOf(gender),
    bloodType = runCatching { BloodType.valueOf(bloodType) }.getOrDefault(BloodType.UNKNOWN),
    allergies = allergies,
    chronicConditions = chronicConditions,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    name = name,
    height = height,
    initialWeightKg = initialWeightKg,
    birthDateEpochDay = birthDate.toEpochDay(),
    gender = gender.name,
    bloodType = bloodType.name,
    allergies = allergies,
    chronicConditions = chronicConditions,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
)

// ----- Weight -----
fun WeightEntity.toDomain() = WeightRecord(
    id = id,
    userId = userId,
    weightKg = weightKg,
    date = LocalDate.ofEpochDay(dateEpochDay),
)

fun WeightRecord.toEntity() = WeightEntity(
    id = id,
    userId = userId,
    weightKg = weightKg,
    dateEpochDay = date.toEpochDay(),
)

// ----- Medication -----
fun MedicationEntity.toDomain() = Medication(
    id = id,
    userId = userId,
    name = name,
    dose = dose,
    form = MedicationForm.valueOf(form),
    instructions = instructions,
    registeredInKz = registeredInKz,
)

fun Medication.toEntity() = MedicationEntity(
    id = id,
    userId = userId,
    name = name,
    dose = dose,
    form = form.name,
    instructions = instructions,
    registeredInKz = registeredInKz,
)

// ----- MedicationIntake -----
fun MedicationIntakeEntity.toDomain() = MedicationIntakeRecord(
    id = id,
    userId = userId,
    medicationId = medicationId,
    takenAt = Instant.ofEpochMilli(takenAtMillis),
    reminderId = reminderId,
    dose = dose,
)

fun MedicationIntakeRecord.toEntity() = MedicationIntakeEntity(
    id = id,
    userId = userId,
    medicationId = medicationId,
    takenAtMillis = takenAt.toEpochMilli(),
    reminderId = reminderId,
    dose = dose,
)

// ----- Reminder -----
private val ALL_DAYS: Set<DayOfWeek> = DayOfWeek.entries.toSet()

fun Set<DayOfWeek>.toMask(): Int =
    fold(0) { acc, d -> acc or (1 shl (d.value - 1)) }

fun Int.toDaysOfWeek(): Set<DayOfWeek> {
    if (this == 0) return ALL_DAYS
    return DayOfWeek.entries.filter { (this shr (it.value - 1)) and 1 == 1 }.toSet()
}

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    userId = userId,
    type = ReminderType.valueOf(type),
    title = title,
    timeOfDay = LocalTime.ofSecondOfDay(timeOfDaySec.toLong()),
    daysOfWeek = daysOfWeekMask.toDaysOfWeek(),
    medicationId = medicationId,
    doseOverride = doseOverride,
    enabled = enabled,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    userId = userId,
    type = type.name,
    title = title,
    timeOfDaySec = timeOfDay.toSecondOfDay(),
    daysOfWeekMask = daysOfWeek.toMask(),
    medicationId = medicationId,
    doseOverride = doseOverride,
    enabled = enabled,
    createdAtMillis = createdAt.toEpochMilli(),
)

// ----- ScreeningRecord -----
fun ScreeningRecordEntity.toDomain() = ScreeningRecord(
    id = id,
    userId = userId,
    screeningCode = screeningCode,
    date = LocalDate.ofEpochDay(dateEpochDay),
    notes = notes,
)

fun ScreeningRecord.toEntity() = ScreeningRecordEntity(
    id = id,
    userId = userId,
    screeningCode = screeningCode,
    dateEpochDay = date.toEpochDay(),
    notes = notes,
)
