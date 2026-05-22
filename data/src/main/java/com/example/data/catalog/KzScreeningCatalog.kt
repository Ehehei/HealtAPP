package com.example.data.catalog

import com.example.domain.model.Gender
import com.example.domain.model.screening.Screening
import com.example.domain.repository.ScreeningCatalog

class KzScreeningCatalog : ScreeningCatalog {

    override val sourceLabel: String =
        "Программа СМП МЗ РК · приказ о профилактических осмотрах целевых групп"

    override val sourceUpdatedOn: String = "сверено: 2024-09"

    private val items: List<Screening> = listOf(
        Screening(
            code = "CVD_DM",
            name = "Скрининг ССЗ и диабета 2 типа",
            description = "Раннее выявление артериальной гипертензии, ишемической болезни сердца и сахарного диабета 2 типа.",
            method = "Измерение АД, общий холестерин, глюкоза крови, ИМТ, опрос по факторам риска.",
            ageRange = 30..70,
            eligibleGender = null,
            intervalMonths = 24,
        ),
        Screening(
            code = "COLORECTAL",
            name = "Скрининг колоректального рака",
            description = "Раннее выявление рака толстой и прямой кишки — самой управляемой по смертности онкологии.",
            method = "Анализ кала на скрытую кровь (гемокульттест), при положительном результате — колоноскопия.",
            ageRange = 50..70,
            eligibleGender = null,
            intervalMonths = 24,
        ),
        Screening(
            code = "CERVICAL",
            name = "Скрининг рака шейки матки",
            description = "Выявление предраковых изменений шейки матки на ранней стадии.",
            method = "Цитологическое исследование (PAP-тест) или ВПЧ-тест по показаниям.",
            ageRange = 30..70,
            eligibleGender = Gender.FEMALE,
            intervalMonths = 48,
        ),
        Screening(
            code = "BREAST",
            name = "Скрининг рака молочной железы",
            description = "Раннее выявление новообразований молочной железы.",
            method = "Маммография двух молочных желёз в двух проекциях.",
            ageRange = 40..70,
            eligibleGender = Gender.FEMALE,
            intervalMonths = 24,
        ),
        Screening(
            code = "HEPATITIS_BC",
            name = "Скрининг на вирусные гепатиты B и C",
            description = "Выявление носительства вирусов гепатитов B и C, в том числе бессимптомного.",
            method = "Серологические тесты на HBsAg и анти-HCV.",
            ageRange = 18..70,
            eligibleGender = null,
            intervalMonths = 0,
        ),
    )

    private val byCode: Map<String, Screening> = items.associateBy { it.code }

    override fun all(): List<Screening> = items

    override fun byCode(code: String): Screening? = byCode[code]
}
