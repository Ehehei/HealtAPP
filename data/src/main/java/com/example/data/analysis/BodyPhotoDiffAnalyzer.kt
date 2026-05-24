package com.example.data.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.data.storage.PhotoStorage
import com.example.domain.model.BodyPhoto
import com.example.domain.model.WeightRecord
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

class BodyPhotoDiffAnalyzer(
    private val storage: PhotoStorage,
    private val minIntervalDays: Int = 14,
    private val visualNoiseThreshold: Float = 0.08f,
    private val weightNoiseKg: Float = 0.7f,
) {

    data class Verdict(
        val verdict: ProgressVerdict,
        val visualChange: Float,
        val weightDeltaKg: Float?,
        val daysBetween: Int,
        val message: String,
    )

    enum class ProgressVerdict {
        TOO_EARLY,
        NOISE,
        SUBTLE_PROGRESS,
        CLEAR_PROGRESS,
        REGRESSION,
    }

    fun analyze(
        before: BodyPhoto,
        after: BodyPhoto,
        beforeWeight: WeightRecord? = null,
        afterWeight: WeightRecord? = null,
        enforceMinInterval: Boolean = true,
    ): Verdict {
        val days = ChronoUnit.DAYS.between(before.date, after.date).toInt()

        if (enforceMinInterval && days < minIntervalDays) {
            return Verdict(
                ProgressVerdict.TOO_EARLY,
                visualChange = 0f,
                weightDeltaKg = afterWeight?.let { a -> beforeWeight?.let { b -> a.weightKg - b.weightKg } },
                daysBetween = days,
                message = "Слишком рано судить о прогрессе. Дай телу минимум $minIntervalDays дней — " +
                    "ежедневные колебания связаны с водой, едой и позой, а не реальным изменением.",
            )
        }

        val visualRaw = perceptualDistance(before.filePath, after.filePath)
        val visual = if (visualRaw < visualNoiseThreshold) 0f else (visualRaw - visualNoiseThreshold)
            .coerceIn(0f, 1f)

        val weightDelta = afterWeight?.let { a -> beforeWeight?.let { b -> a.weightKg - b.weightKg } }
        val significantWeight = weightDelta != null && abs(weightDelta) >= weightNoiseKg

        val verdict = when {
            weightDelta != null && weightDelta >= weightNoiseKg -> ProgressVerdict.REGRESSION
            visual == 0f && !significantWeight -> ProgressVerdict.NOISE
            visual >= 0.18f || (weightDelta != null && weightDelta <= -2f) -> ProgressVerdict.CLEAR_PROGRESS
            else -> ProgressVerdict.SUBTLE_PROGRESS
        }

        val msg = when (verdict) {
            ProgressVerdict.NOISE ->
                "Видимых изменений пока нет — это нормально. Не сравнивай себя ежедневно, " +
                    "следи за трендом за месяц."
            ProgressVerdict.SUBTLE_PROGRESS ->
                "Лёгкий, но реальный сдвиг" + (if (days > 0) " за $days дн" else "") +
                    ". Не торопи результат — стабильность важнее скорости."
            ProgressVerdict.CLEAR_PROGRESS ->
                "Чёткий прогресс за $days дн" +
                    (weightDelta?.let { " · вес ${formatDelta(it)} кг" } ?: "") + ". Продолжай."
            ProgressVerdict.REGRESSION ->
                "За $days дн вес вырос на ${formatDelta(weightDelta!!)} кг. Это не катастрофа — " +
                    "пересмотри питание и сон, без самобичевания."
            ProgressVerdict.TOO_EARLY -> ""
        }

        return Verdict(verdict, visual, weightDelta, days, msg)
    }

    private fun formatDelta(v: Float): String {
        val s = ((v * 10).roundToInt() / 10f)
        return if (s > 0) "+$s" else "$s"
    }

    private fun perceptualDistance(pathA: String, pathB: String): Float {
        val a = downscaleGray(pathA) ?: return 0f
        val b = downscaleGray(pathB) ?: return 0f
        if (a.size != b.size) return 0f
        val avgA = a.average()
        val avgB = b.average()
        var diffBits = 0
        for (i in a.indices) {
            val ba = if (a[i] > avgA) 1 else 0
            val bb = if (b[i] > avgB) 1 else 0
            if (ba != bb) diffBits++
        }
        return diffBits.toFloat() / a.size
    }

    private fun downscaleGray(path: String): IntArray? {
        val bytes = storage.readBytes(path) ?: return null
        val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
        val scaled = Bitmap.createScaledBitmap(bmp, SIZE, SIZE, true)
        val out = IntArray(SIZE * SIZE)
        val px = IntArray(SIZE * SIZE)
        scaled.getPixels(px, 0, SIZE, 0, 0, SIZE, SIZE)
        for (i in px.indices) {
            val c = px[i]

            out[i] = (Color.red(c) * 30 + Color.green(c) * 59 + Color.blue(c) * 11) / 100
        }
        bmp.recycle()
        scaled.recycle()
        return out
    }

    private companion object {
        const val SIZE = 16
    }
}
