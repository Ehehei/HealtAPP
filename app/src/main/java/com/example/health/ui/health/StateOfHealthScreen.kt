package com.example.health.ui.health

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
import androidx.compose.material3.FilterChip
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
import com.example.domain.model.FeelingLevel
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
fun StateOfHealthScreen(modifier: Modifier = Modifier, vm: StateOfHealthViewModel = koinViewModel()) {
    val trend by vm.trend.collectAsState()
    val history by vm.history.collectAsState()
    val error by vm.error.collectAsState()

    var feeling by remember { mutableStateOf(FeelingLevel.GOOD) }
    var sugar by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(ChartPeriod.MONTH) }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Самочувствие")

        SectionCard {
            Text("Сегодня я чувствую себя", color = OnSurfaceMuted, fontSize = 12.sp)
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FeelingLevel.entries.forEach { f ->
                    FilterChip(
                        selected = feeling == f,
                        onClick = { feeling = f },
                        label = { Text(f.name) },
                    )
                }
            }
            OutlinedTextField(
                sugar, { sugar = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Сахар, ммоль/л (опц.)") },
            )
            OutlinedTextField(
                temp, { temp = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Температура °C (опц.)") },
            )
            OutlinedTextField(
                notes, { notes = it },
                Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Заметка") },
            )
            Button(
                onClick = {
                    vm.add(feeling, sugar.toFloatOrNull(), temp.toFloatOrNull(), notes.ifBlank { null })
                    sugar = ""; temp = ""; notes = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Сохранить") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        SectionCard {
            Text("Динамика самочувствия (1–5)", color = OnSurfaceMuted, fontSize = 12.sp)
            ChartPeriodSelector(period, { period = it })
            val cutoff = LocalDate.now().minusDays(period.days.toLong()).toEpochDay()
            val color = MaterialTheme.colorScheme.primary
            val points = history
                .filter { it.date.toEpochDay() >= cutoff }
                .groupBy { it.date }
                .map { (date, list) ->
                    val avg = list.map { it.feelingLevel.value }.average().toFloat()
                    ChartPoint(date.toEpochDay(), avg)
                }
                .sortedBy { it.xEpochDay }
            LineChart(
                series = listOf(ChartSeries("Самочувствие", color, points)),
                yLabelFormat = { "%.1f".format(it) },
            )
        }

        SectionCard {
            Text("Тренд за 30 дней", color = OnSurfaceMuted, fontSize = 12.sp)
            Text(
                "Среднее: ${"%.1f".format(trend?.averageFeeling ?: 0f)} / 5",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            trend?.bloodSugarAvg?.let {
                Text("Сахар (ср): ${"%.1f".format(it)}", color = OnSurfaceMuted)
            }
            trend?.temperatureAvg?.let {
                Text("Темп. (ср): ${"%.1f".format(it)}", color = OnSurfaceMuted)
            }
        }
    }
}
