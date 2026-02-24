package com.example.domain.usecase.profile

import java.time.LocalDate
import java.time.Period

class CalculateUserAgeUseCase {
    operator fun invoke(birthDate: LocalDate): Int =
        Period.between(birthDate, LocalDate.now()).years
}
