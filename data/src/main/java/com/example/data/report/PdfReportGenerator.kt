package com.example.data.report

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.domain.model.HealthReportData
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Генерирует PDF с самочувствием пользователя за период.
 * Возвращает content:// URI через FileProvider, чтобы файл можно было сразу
 * передать в Intent.ACTION_SEND и отправить врачу.
 */
class PdfReportGenerator(private val context: Context) {

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")

    fun generate(data: HealthReportData): android.net.Uri {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val title = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val h2 = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val body = Paint().apply { textSize = 11f }
        val muted = Paint().apply { textSize = 10f; color = 0xFF666666.toInt() }

        var y = 50f
        val left = 40f

        canvas.drawText("Health Report", left, y, title); y += 28f
        canvas.drawText("Пациент: ${data.profile.name}", left, y, body); y += 16f
        canvas.drawText(
            "Период: ${data.period.start.format(dateFmt)} – ${data.period.endInclusive.format(dateFmt)}",
            left, y, body
        ); y += 16f
        data.bmi?.let { canvas.drawText("ИМТ: ${"%.1f".format(it)}", left, y, body); y += 16f }
        y += 12f

        // ----- Шаги -----
        canvas.drawText("Активность", left, y, h2); y += 18f
        canvas.drawText("Средние шаги в день: ${data.avgDailySteps}", left, y, body); y += 14f
        canvas.drawText("Дней с записями: ${data.stepRecords.size}", left, y, muted); y += 22f

        // ----- Вес -----
        canvas.drawText("Вес", left, y, h2); y += 18f
        val first = data.weightRecords.minByOrNull { it.date }
        val last = data.weightRecords.maxByOrNull { it.date }
        if (first != null && last != null) {
            canvas.drawText(
                "С ${"%.1f".format(first.weightKg)} кг до ${"%.1f".format(last.weightKg)} кг",
                left, y, body
            ); y += 14f
            data.weightChange?.let {
                canvas.drawText("Изменение: ${"%+.1f".format(it)} кг", left, y, body); y += 14f
            }
        } else {
            canvas.drawText("Нет записей.", left, y, muted); y += 14f
        }
        y += 8f

        // ----- Давление -----
        canvas.drawText("Артериальное давление", left, y, h2); y += 18f
        data.bloodPressureStats?.let { stats ->
            canvas.drawText(
                "Среднее: ${stats.avgSystolic.toInt()}/${stats.avgDiastolic.toInt()} мм рт.ст., " +
                    "пульс ${stats.avgPulse.toInt()}",
                left, y, body
            ); y += 14f
            canvas.drawText(
                "Диапазон сист.: ${stats.minSystolic}–${stats.maxSystolic}, " +
                    "диаст.: ${stats.minDiastolic}–${stats.maxDiastolic}",
                left, y, body
            ); y += 14f
            canvas.drawText("Классификация: ${stats.classification.name}", left, y, body); y += 14f
            canvas.drawText("Записей: ${stats.recordCount}", left, y, muted); y += 18f
        } ?: run {
            canvas.drawText("Нет записей.", left, y, muted); y += 18f
        }

        // ----- Самочувствие -----
        canvas.drawText("Общее самочувствие", left, y, h2); y += 18f
        data.avgFeeling?.let {
            canvas.drawText("Средний уровень (1-5): ${"%.1f".format(it)}", left, y, body); y += 14f
        }
        data.bloodSugarAvg?.let {
            canvas.drawText("Средний сахар: ${"%.1f".format(it)} ммоль/л", left, y, body); y += 14f
        }
        data.temperatureAvg?.let {
            canvas.drawText("Средняя температура: ${"%.1f".format(it)} °C", left, y, body); y += 14f
        }
        y += 12f

        // ----- Журнал давления (последние 10) -----
        if (data.bloodPressureRecords.isNotEmpty()) {
            canvas.drawText("Последние измерения давления", left, y, h2); y += 18f
            data.bloodPressureRecords.take(10).forEach { bp ->
                canvas.drawText(
                    "${bp.date.format(dateTimeFmt)}  —  ${bp.systolicPressure}/${bp.diastolicPressure}, пульс ${bp.pulse}",
                    left, y, body
                ); y += 13f
                if (y > 800) return@forEach
            }
        }

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "health_report_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}
