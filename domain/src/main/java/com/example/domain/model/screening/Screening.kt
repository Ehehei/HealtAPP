package com.example.domain.model.screening

import com.example.domain.model.Gender

/**
 * Описание одного скрининга из государственной программы РК.
 *
 * code — стабильный идентификатор для хранения в БД (не name, потому что название может меняться).
 * ageRange — закрытый диапазон возрастов, в которых скрининг показан.
 * eligibleGender — null означает «оба пола».
 * intervalMonths — рекомендованный интервал между прохождениями. Если 0 — однократно/по показаниям.
 */
data class Screening(
    val code: String,
    val name: String,
    val description: String,
    val method: String,
    val ageRange: IntRange,
    val eligibleGender: Gender?,
    val intervalMonths: Int,
)
