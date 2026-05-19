package com.example.domain.model

data class Medication(
    val id: Long,
    val userId: String,
    val name: String,
    val dose: String,
    val form: MedicationForm,
    val instructions: String? = null,
    val registeredInKz: Boolean = false,
)

enum class MedicationForm {
    TABLET,
    CAPSULE,
    DROPS,
    INJECTION,
    OINTMENT,
    OTHER,
}
