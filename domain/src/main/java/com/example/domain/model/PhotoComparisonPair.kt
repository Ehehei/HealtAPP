package com.example.domain.model

data class PhotoComparisonPair(
    val before: BodyPhoto,
    val after: BodyPhoto,
    val daysBetween: Int
)
