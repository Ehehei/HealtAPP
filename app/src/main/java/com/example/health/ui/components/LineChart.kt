package com.example.health.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ChartPoint(val xEpochDay: Long, val y: Float)

data class ChartSeries(
    val label: String,
    val color: Color,
    val points: List<ChartPoint>,
)

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")

@Composable
fun LineChart(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    yLabelFormat: (Float) -> String = { "%.0f".format(it) },
) {
    val allPoints = series.flatMap { it.points }
    if (allPoints.size < 2) {
        Box(
            modifier
                .fillMaxWidth()
                .height(height)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Недостаточно данных для графика",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        return
    }

    val xMin = allPoints.minOf { it.xEpochDay }
    val xMax = allPoints.maxOf { it.xEpochDay }
    val xRange = (xMax - xMin).coerceAtLeast(1L)

    val rawYMin = allPoints.minOf { it.y }
    val rawYMax = allPoints.maxOf { it.y }
    val yPadding = ((rawYMax - rawYMin) * 0.1f).coerceAtLeast(1f)
    val yMin = rawYMin - yPadding
    val yMax = rawYMax + yPadding
    val yRange = (yMax - yMin).coerceAtLeast(0.0001f)

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val axisColor = MaterialTheme.colorScheme.outline
    val gridColor = axisColor.copy(alpha = 0.25f)
    val labelStyle = TextStyle(fontSize = 10.sp, color = TextSecondary)

    val padLeftPx = with(density) { 36.dp.toPx() }
    val padRightPx = with(density) { 8.dp.toPx() }
    val padTopPx = with(density) { 8.dp.toPx() }
    val padBottomPx = with(density) { 22.dp.toPx() }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        val w = size.width
        val h = size.height
        val plotLeft = padLeftPx
        val plotTop = padTopPx
        val plotRight = w - padRightPx
        val plotBottom = h - padBottomPx
        val plotW = plotRight - plotLeft
        val plotH = plotBottom - plotTop

        fun xOf(epoch: Long): Float =
            plotLeft + (epoch - xMin).toFloat() / xRange * plotW

        fun yOf(value: Float): Float =
            plotBottom - (value - yMin) / yRange * plotH

        // grid (3 lines: top/middle/bottom)
        listOf(0f, 0.5f, 1f).forEach { frac ->
            val y = plotTop + plotH * frac
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }

        // Y-labels (max top, mid, min bottom)
        listOf(yMax, (yMin + yMax) / 2f, yMin).forEachIndexed { idx, value ->
            val frac = idx * 0.5f
            val y = plotTop + plotH * frac
            val layout = measurer.measure(
                text = yLabelFormat(value),
                style = labelStyle,
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = plotLeft - layout.size.width - 4f,
                    y = y - layout.size.height / 2f,
                ),
            )
        }

        // X-labels: даты по краям
        val startDate = LocalDate.ofEpochDay(xMin).format(dateFmt)
        val endDate = LocalDate.ofEpochDay(xMax).format(dateFmt)
        val startLayout = measurer.measure(startDate, labelStyle)
        val endLayout = measurer.measure(endDate, labelStyle)
        drawText(
            textLayoutResult = startLayout,
            topLeft = Offset(plotLeft, plotBottom + 4f),
        )
        drawText(
            textLayoutResult = endLayout,
            topLeft = Offset(plotRight - endLayout.size.width, plotBottom + 4f),
        )

        // series
        series.forEach { s ->
            val sorted = s.points.sortedBy { it.xEpochDay }
            if (sorted.size < 2) return@forEach
            val path = Path()
            sorted.forEachIndexed { idx, p ->
                val x = xOf(p.xEpochDay)
                val y = yOf(p.y)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = s.color,
                style = Stroke(width = 3f),
            )
            sorted.forEach { p ->
                drawCircle(
                    color = s.color,
                    radius = 3.5f,
                    center = Offset(xOf(p.xEpochDay), yOf(p.y)),
                )
            }
        }
    }
}

@Composable
fun ChartLegend(series: List<ChartSeries>, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        series.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .padding(end = 4.dp)
                        .size(10.dp)
                        .background(s.color, CircleShape),
                )
                Text(s.label, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
