package com.example.health.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.example.health.ui.components.LineChart
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
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
        ScreenTitle("Вес")

        SectionCard {
            progress?.let { p ->
                Text("Текущий", color = OnSurfaceMuted, fontSize = 12.sp)
                Text("%.1f кг".format(p.currentWeightKg), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(
                    "С начала: ${"%+.1f".format(p.changeKg)} кг (${"%+.1f".format(p.changePercent)}%)",
                    color = OnSurfaceMuted,
                    fontSize = 13.sp,
                )
                Text("Тренд: ${p.trend.name}", color = OnSurfaceMuted, fontSize = 12.sp)
            } ?: Text("Добавь первое измерение", color = OnSurfaceMuted)
        }

        SectionCard {
            Text("Динамика", color = OnSurfaceMuted, fontSize = 12.sp)
            ChartPeriodSelector(period, { period = it })
            val cutoff = LocalDate.now().minusDays(period.days.toLong()).toEpochDay()
            val color = MaterialTheme.colorScheme.primary
            val series = listOf(
                ChartSeries(
                    label = "Вес",
                    color = color,
                    points = history
                        .filter { it.date.toEpochDay() >= cutoff }
                        .map { ChartPoint(it.date.toEpochDay(), it.weightKg) },
                ),
            )
            LineChart(
                series = series,
                yLabelFormat = { "%.1f".format(it) },
            )
        }

        SectionCard {
            Text("Новое измерение", color = OnSurfaceMuted, fontSize = 12.sp)
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("кг") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Button(
                onClick = {
                    input.toFloatOrNull()?.let { vm.add(it); input = "" }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Сохранить") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        SectionCard {
            Text("История", color = OnSurfaceMuted, fontSize = 12.sp)
            history.takeLast(10).reversed().forEach { r ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(r.date.toString())
                    Text("%.1f кг".format(r.weightKg), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
