package com.example.domain.model

data class MedicationCatalogItem(

    val inn: String,

    val tradeName: String,
    val form: MedicationForm,

    val registeredInKz: Boolean,

    val group: String,

    val note: String? = null,
)
