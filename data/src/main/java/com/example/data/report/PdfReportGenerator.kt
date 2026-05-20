package com.example.data.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.domain.model.HealthReportData
import com.example.domain.model.MedicationForm
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.domain.model.screening.ScreeningStatus
import java.io.File
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Генерирует PDF с самочувствием пользователя за период.
 * Возвращает content:// URI через FileProvider, чтобы файл можно было сразу
 * передать в Intent.ACTION_SEND и отправить врачу.
 */
class PdfReportGenerator(private val context: Context) {

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val zone: ZoneId = ZoneId.systemDefault()

    fun generate(data: HealthReportData): android.net.Uri {
        val pdf = PdfDocument()
        val title = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val h2 = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val body = Paint().apply { textSize = 11f }
        val muted = Paint().apply { textSize = 10f; color = 0xFF666666.toInt() }

        val r = Renderer(pdf)
        r.drawText("Health Report", title); r.advance(28f)
        r.drawText("Пациент: ${data.profile.name}", body); r.advance(16f)
        r.drawText(
            "Период: ${data.period.start.format(dateFmt)} – ${data.period.endInclusive.format(dateFmt)}",
            body,
        ); r.advance(16f)
        data.bmi?.let { r.drawText("ИМТ: ${"%.1f".format(it)}", body); r.advance(16f) }
        r.advance(12f)

        // ----- Шаги -----
        r.section("Активность", h2)
        r.drawText("Средние шаги в день: ${data.avgDailySteps}", body); r.advance(14f)
        r.drawText("Дней с записями: ${data.stepRecords.size}", muted); r.advance(22f)

        // ----- Вес -----
        r.section("Вес", h2)
        val first = data.weightRecords.minByOrNull { it.date }
        val last = data.weightRecords.maxByOrNull { it.date }
        if (first != null && last != null) {
            r.drawText(
                "С ${"%.1f".format(first.weightKg)} кг до ${"%.1f".format(last.weightKg)} кг",
                body,
            ); r.advance(14f)
            data.weightChange?.let {
                r.drawText("Изменение: ${"%+.1f".format(it)} кг", body); r.advance(14f)
            }
        } else {
            r.drawText("Нет записей.", muted); r.advance(14f)
        }
        r.advance(8f)

        // ----- Давление -----
        r.section("Артериальное давление", h2)
        data.bloodPressureStats?.let { stats ->
            r.drawText(
                "Среднее: ${stats.avgSystolic.toInt()}/${stats.avgDiastolic.toInt()} мм рт.ст., " +
                    "пульс ${stats.avgPulse.toInt()}",
                body,
            ); r.advance(14f)
            r.drawText(
                "Диапазон сист.: ${stats.minSystolic}–${stats.maxSystolic}, " +
                    "диаст.: ${stats.minDiastolic}–${stats.maxDiastolic}",
                body,
            ); r.advance(14f)
            r.drawText("Классификация: ${stats.classification.name}", body); r.advance(14f)
            r.drawText("Записей: ${stats.recordCount}", muted); r.advance(18f)
        } ?: run {
            r.drawText("Нет записей.", muted); r.advance(18f)
        }

        // ----- Самочувствие -----
        r.section("Общее самочувствие", h2)
        data.avgFeeling?.let {
            r.drawText("Средний уровень (1-5): ${"%.1f".format(it)}", body); r.advance(14f)
        }
        data.bloodSugarAvg?.let {
            r.drawText("Средний сахар: ${"%.1f".format(it)} ммоль/л", body); r.advance(14f)
        }
        data.temperatureAvg?.let {
            r.drawText("Средняя температура: ${"%.1f".format(it)} °C", body); r.advance(14f)
        }
        r.advance(12f)

        // ----- Журнал давления (последние 10) -----
        if (data.bloodPressureRecords.isNotEmpty()) {
            r.section("Последние измерения давления", h2)
            data.bloodPressureRecords.take(10).forEach { bp ->
                r.drawText(
                    "${bp.date.format(dateTimeFmt)}  —  ${bp.systolicPressure}/${bp.diastolicPressure}, пульс ${bp.pulse}",
                    body,
                ); r.advance(13f)
            }
            r.advance(6f)
        }

        // ----- История приёмов лекарств -----
        r.section("История приёмов лекарств", h2)
        if (data.medicationIntakes.isEmpty()) {
            r.drawText("Нет записей за период.", muted); r.advance(18f)
        } else {
            r.drawText("Всего записей: ${data.medicationIntakes.size}", muted); r.advance(14f)
            data.medicationIntakes.take(30).forEach { intake ->
                val med = data.medicationsById[intake.medicationId]
                val medLabel = med?.name ?: "Препарат #${intake.medicationId}"
                val dose = intake.dose ?: med?.dose
                val when_ = intake.takenAt.atZone(zone).toLocalDateTime().format(dateTimeFmt)
                val line = buildString {
                    append(when_)
                    append("  —  ")
                    append(medLabel)
                    if (!dose.isNullOrBlank()) append(", $dose")
                }
                r.drawText(line, body); r.advance(13f)
            }
            if (data.medicationIntakes.size > 30) {
                r.drawText(
                    "… и ещё ${data.medicationIntakes.size - 30} записей в приложении.",
                    muted,
                ); r.advance(14f)
            }
            r.advance(6f)
        }

        // ----- Активные напоминания -----
        r.section("Активные напоминания", h2)
        if (data.activeReminders.isEmpty()) {
            r.drawText("Нет включённых напоминаний.", muted); r.advance(18f)
        } else {
            data.activeReminders.forEach { reminder ->
                r.drawText(reminderLine(reminder, data), body); r.advance(13f)
            }
            r.advance(6f)
        }

        // ----- Скрининги РК -----
        r.section("Скрининги по программе РК", h2)
        if (data.eligibleScreenings.isEmpty()) {
            r.drawText("Подходящих скринингов по возрасту/полу нет.", muted); r.advance(18f)
        } else {
            data.eligibleScreenings.forEach { eligibility ->
                val statusLabel = when (eligibility.status) {
                    ScreeningStatus.DUE_NOW -> "пройти сейчас"
                    ScreeningStatus.UPCOMING -> "запланировано"
                    ScreeningStatus.NOT_ELIGIBLE -> "—"
                }
                r.drawText(
                    "${eligibility.screening.name}  —  $statusLabel",
                    body,
                ); r.advance(13f)
                val detail = buildString {
                    eligibility.lastDoneOn?.let { append("последнее: ${it.format(dateFmt)}") }
                    eligibility.nextDueOn?.let {
                        if (isNotEmpty()) append(", ")
                        append("следующее: ${it.format(dateFmt)}")
                    }
                }
                if (detail.isNotEmpty()) {
                    r.drawText("  $detail", muted); r.advance(13f)
                }
            }
            r.advance(6f)
        }

        r.finish()
        val file = File(
            File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() },
            "health_report_${System.currentTimeMillis()}.pdf",
        )
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun reminderLine(reminder: Reminder, data: HealthReportData): String {
        val timeStr = reminder.timeOfDay.format(timeFmt)
        val days = formatDays(reminder.daysOfWeek)
        val typeLabel = when (reminder.type) {
            ReminderType.MEDICATION -> {
                val med = reminder.medicationId?.let { data.medicationsById[it] }
                val name = med?.name ?: reminder.title
                val dose = reminder.doseOverride ?: med?.dose
                val formLabel = med?.form?.let { formLabel(it) }
                buildString {
                    append("Препарат: ")
                    append(name)
                    if (!dose.isNullOrBlank()) append(", $dose")
                    if (!formLabel.isNullOrBlank()) append(" ($formLabel)")
                }
            }
            ReminderType.BLOOD_PRESSURE -> "Замер давления"
            ReminderType.WEIGHT -> "Взвешивание"
            ReminderType.FEELING -> "Запись самочувствия"
            ReminderType.WATER -> "Стакан воды"
        }
        return "$timeStr, $days  —  $typeLabel"
    }

    private fun formatDays(days: Set<DayOfWeek>): String {
        if (days.isEmpty() || days.size == 7) return "ежедневно"
        return DayOfWeek.entries
            .filter { it in days }
            .joinToString(" ") {
                it.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"))
            }
    }

    private fun formLabel(form: MedicationForm): String = when (form) {
        MedicationForm.TABLET -> "таблетка"
        MedicationForm.CAPSULE -> "капсула"
        MedicationForm.DROPS -> "капли"
        MedicationForm.INJECTION -> "инъекция"
        MedicationForm.OINTMENT -> "мазь"
        MedicationForm.OTHER -> "форма не указана"
    }

    /**
     * Хелпер для отрисовки многостраничного PDF. Создаёт новые страницы по мере
     * наполнения, чтобы блоки не обрезались. A4 595×842, поля 40/50.
     */
    private inner class Renderer(private val pdf: PdfDocument) {
        private val pageWidth = 595
        private val pageHeight = 842
        private val left = 40f
        private val top = 50f
        private val bottom = 800f

        private var pageNumber = 0
        private var page: PdfDocument.Page = startNewPage()
        private var canvas: Canvas = page.canvas
        private var y = top

        fun drawText(text: String, paint: Paint) {
            ensureSpace(paint.textSize + 4f)
            canvas.drawText(text, left, y, paint)
        }

        fun advance(dy: Float) {
            y += dy
            ensureSpace(0f)
        }

        fun section(title: String, paint: Paint) {
            ensureSpace(paint.textSize + 24f)
            canvas.drawText(title, left, y, paint)
            y += 18f
        }

        fun finish() {
            pdf.finishPage(page)
        }

        private fun ensureSpace(needed: Float) {
            if (y + needed <= bottom) return
            pdf.finishPage(page)
            page = startNewPage()
            canvas = page.canvas
            y = top
        }

        private fun startNewPage(): PdfDocument.Page {
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            return pdf.startPage(info)
        }
    }
}
