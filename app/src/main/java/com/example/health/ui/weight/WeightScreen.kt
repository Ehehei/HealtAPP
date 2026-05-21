package com.example.health.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.ChartPeriod
import com.example.health.ui.components.ChartPeriodSelector
import com.example.health.ui.components.ChartPoint
import com.example.health.ui.components.ChartSeries
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LineChart
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentBlue
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.DisplayL
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun WeightScreen(vm: WeightViewModel = koinViewModel()) {
    val history by vm.history.collectAsState()
    val progress by vm.progress.collectAsState()
    val error by vm.error.collectAsState()

    var input by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(ChartPeriod.MONTH) }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero
        SectionCard {
            LabelXsText("Текущий вес")
            Spacer(Modifier.height(4.dp))
            progress?.let { p ->
                Text("%.1f кг".format(p.currentWeightKg), style = DisplayL, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                val sign = if (p.changeKg < 0) "↘" else if (p.changeKg > 0) "↗" else "→"
                Text(
                    "$sign ${"%+.1f".format(p.changeKg)} кг (${"%+.1f".format(p.changePercent)}%) с начала",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                Text(
                    "Тренд: ${trendLabel(p.trend.name)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            } ?: Text("Добавь первое измерение", color = TextSecondary)
        }

        // Динамика
        SectionCard {
            LabelXsText("Динамика")
            Spacer(Modifier.height(6.dp))
            ChartPeriodSelector(period, { period = it })
            val cutoff = LocalDate.now().minusDays(period.days.toLong()).toEpochDay()
            val series = listOf(
                ChartSeries(
                    label = "Вес",
                    color = AccentBlue,
                    points = history
                        .filter { it.date.toEpochDay() >= cutoff }
                        .map { ChartPoint(it.date.toEpochDay(), it.weightKg) },
                ),
            )
            LineChart(series = series, yLabelFormat = { "%.1f".format(it) })
        }

        // Новое измерение
        SectionCard {
            LabelXsText("Новое измерение")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("кг") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    input.toFloatOrNull()?.let { vm.add(it); input = "" }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        // История
        SectionCard {
            LabelXsText("История")
            Spacer(Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text("Нет записей", color = TextSecondary, fontSize = 13.sp)
            } else {
                history.takeLast(10).reversed().forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(r.date.toString(), color = TextSecondary, fontSize = 13.sp)
                        Text("%.1f кг".format(r.weightKg), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun trendLabel(name: String): String = when (name) {
    "LOSING" -> "снижается"
    "GAINING" -> "растёт"
    "STABLE" -> "стабильный"
    else -> "—"
}
