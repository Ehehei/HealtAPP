package com.example.data.catalog

import com.example.domain.model.MedicationCatalogItem
import com.example.domain.model.MedicationForm
import com.example.domain.repository.MedicationCatalogRepository
import java.util.Locale

class KzMedicationCatalog : MedicationCatalogRepository {

    override val sourceLabel: String =
        "Государственный реестр ЛС РК · ndda.kz (учебная выборка)"

    override val sourceUpdatedOn: String = "сверено: 2024-09"

    private val items: List<MedicationCatalogItem> = listOf(

        item("Амлодипин", "Амлодипин-Тева", MedicationForm.TABLET, true, GROUP_CVD, "по рецепту"),
        item("Эналаприл", "Энап", MedicationForm.TABLET, true, GROUP_CVD, "по рецепту"),
        item("Лозартан", "Лориста", MedicationForm.TABLET, true, GROUP_CVD, "по рецепту"),
        item("Бисопролол", "Конкор", MedicationForm.TABLET, true, GROUP_CVD, "по рецепту"),
        item("Индапамид", "Арифон ретард", MedicationForm.TABLET, true, GROUP_CVD, "по рецепту"),
        item("Нитроглицерин", "Нитроминт", MedicationForm.OTHER, true, GROUP_CVD, "сублингвально / спрей"),

        item("Ацетилсалициловая кислота", "Кардиомагнил", MedicationForm.TABLET, true, GROUP_BLOOD, "безрецептурный"),
        item("Аторвастатин", "Аторис", MedicationForm.TABLET, true, GROUP_BLOOD, "по рецепту"),
        item("Розувастатин", "Крестор", MedicationForm.TABLET, false, GROUP_BLOOD, "не во всех аптеках РК"),

        item("Метформин", "Глюкофаж", MedicationForm.TABLET, true, GROUP_ENDO, "по рецепту"),
        item("Гликлазид", "Диабетон MR", MedicationForm.TABLET, true, GROUP_ENDO, "по рецепту"),
        item("Левотироксин натрия", "L-Тироксин", MedicationForm.TABLET, true, GROUP_ENDO, "по рецепту"),
        item("Инсулин гларгин", "Лантус СолоСтар", MedicationForm.INJECTION, true, GROUP_ENDO, "по рецепту"),

        item("Парацетамол", "Панадол", MedicationForm.TABLET, true, GROUP_PAIN, "безрецептурный"),
        item("Ибупрофен", "Нурофен", MedicationForm.TABLET, true, GROUP_PAIN, "безрецептурный"),
        item("Кеторолак", "Кетанов", MedicationForm.TABLET, true, GROUP_PAIN, "по рецепту"),
        item("Диклофенак", "Вольтарен", MedicationForm.OINTMENT, true, GROUP_PAIN, "наружное"),

        item("Омепразол", "Омез", MedicationForm.CAPSULE, true, GROUP_GI, "безрецептурный"),
        item("Лоперамид", "Имодиум", MedicationForm.CAPSULE, true, GROUP_GI, "безрецептурный"),
        item("Дротаверин", "Но-шпа", MedicationForm.TABLET, true, GROUP_GI, "безрецептурный"),
        item("Урсодезоксихолевая кислота", "Урсосан", MedicationForm.CAPSULE, true, GROUP_GI, "по рецепту"),

        item("Амоксициллин + клавулановая кислота", "Аугментин", MedicationForm.TABLET, true, GROUP_ABX, "по рецепту"),
        item("Азитромицин", "Сумамед", MedicationForm.CAPSULE, true, GROUP_ABX, "по рецепту"),
        item("Цефиксим", "Супракс", MedicationForm.CAPSULE, true, GROUP_ABX, "по рецепту"),

        item("Сальбутамол", "Вентолин", MedicationForm.OTHER, true, GROUP_RESP, "ингалятор"),
        item("Будесонид + формотерол", "Симбикорт Турбухалер", MedicationForm.OTHER, true, GROUP_RESP, "ингалятор, по рецепту"),
        item("Цетиризин", "Зодак", MedicationForm.DROPS, true, GROUP_RESP, "безрецептурный"),
        item("Лоратадин", "Кларитин", MedicationForm.TABLET, true, GROUP_RESP, "безрецептурный"),

        item("Колекальциферол (вит. D3)", "Аквадетрим", MedicationForm.DROPS, true, GROUP_VIT, "безрецептурный"),
        item("Мелатонин", "Мелаксен", MedicationForm.TABLET, false, GROUP_VIT, "БАД, не во всех аптеках РК"),
    )

    private val byInn: Map<String, MedicationCatalogItem> =
        items.associateBy { it.inn.lowercase(Locale.ROOT) }

    override fun all(): List<MedicationCatalogItem> = items

    override fun search(query: String): List<MedicationCatalogItem> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return items
        return items.filter {
            it.inn.lowercase(Locale.ROOT).contains(q) ||
                it.tradeName.lowercase(Locale.ROOT).contains(q)
        }
    }

    override fun byInn(inn: String): MedicationCatalogItem? =
        byInn[inn.lowercase(Locale.ROOT)]

    private fun item(
        inn: String,
        trade: String,
        form: MedicationForm,
        registered: Boolean,
        group: String,
        note: String? = null,
    ) = MedicationCatalogItem(
        inn = inn,
        tradeName = trade,
        form = form,
        registeredInKz = registered,
        group = group,
        note = note,
    )

    private companion object {
        const val GROUP_CVD = "Сердечно-сосудистые"
        const val GROUP_BLOOD = "Кровь и липиды"
        const val GROUP_ENDO = "Эндокринология"
        const val GROUP_PAIN = "Обезболивающие и НПВС"
        const val GROUP_GI = "Желудочно-кишечный тракт"
        const val GROUP_ABX = "Антибиотики"
        const val GROUP_RESP = "Респираторные и аллергия"
        const val GROUP_VIT = "Витамины и БАД"
    }
}
