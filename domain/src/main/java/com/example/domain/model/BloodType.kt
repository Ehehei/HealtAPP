package com.example.domain.model

enum class BloodType(val label: String) {
    UNKNOWN("Не указана"),
    O_NEG("O (I) Rh−"),
    O_POS("O (I) Rh+"),
    A_NEG("A (II) Rh−"),
    A_POS("A (II) Rh+"),
    B_NEG("B (III) Rh−"),
    B_POS("B (III) Rh+"),
    AB_NEG("AB (IV) Rh−"),
    AB_POS("AB (IV) Rh+"),
}
