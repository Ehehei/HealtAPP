package com.example.health.ui.pressure

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.ChartLegend
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

private val SystolicColor = Color(0xFF1976D2)
private val DiastolicColor = Color(0xFF388E3C)

@Composable
fun PressureScreen(vm: PressureViewModel = koinViewModel()) {
    val history by vm.history.collectAsState()
    val stats by vm.stats.collectAsState()
    val error by vm.error.collectAsState()

    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(ChartPeriod.MONTH) }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Давление")

        SectionCard {
            stats?.let { s ->
                Text("Среднее за 30 дн", color = OnSurfaceMuted, fontSize = 12.sp)
                Text(
                    "${s.avgSystolic.toInt()}/${s.avgDiastolic.toInt()}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("Пульс ~${s.avgPulse.toInt()} · ${s.classification.name}", color = OnSurfaceMuted)
            } ?: Text("Нет данных за 30 дней", color = OnSurfaceMuted)
        }

        SectionCard {
            Text("Динамика", color = OnSurfaceMuted, fontSize = 12.sp)
            ChartPeriodSelector(period, { period = it })
            val cutoff = LocalDate.now().minusDays(period.days.toLong()).toEpochDay()
            val filtered = history.filter { it.date.toLocalDate().toEpochDay() >= cutoff }
            val series = listOf(
                ChartSeries(
                    label = "САД",
                    color = SystolicColor,
                    points = filtered.map {
                        ChartPoint(it.date.toLocalDate().toEpochDay(), it.systolicPressure.toFloat())
                    },
                ),
                ChartSeries(
                    label = "ДАД",
                    color = DiastolicColor,
                    points = filtered.map {
                        ChartPoint(it.date.toLocalDate().toEpochDay(), it.diastolicPressure.toFloat())
                    },
                ),
            )
            LineChart(series = series, yLabelFormat = { "%.0f".format(it) })
            ChartLegend(series = series, modifier = Modifier.padding(top = 4.dp))
        }

        SectionCard {
            Text("Новое измерение", color = OnSurfaceMuted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(
                    sys, { sys = it.filter(Char::isDigit) },
                    Modifier.weight(1f), label = { Text("сист.") },
                )
                OutlinedTextField(
                    dia, { dia = it.filter(Char::isDigit) },
                    Modifier.weight(1f).padding(start = 6.dp), label = { Text("диаст.") },
                )
                OutlinedTextField(
                    pulse, { pulse = it.filter(Char::isDigit) },
                    Modifier.weight(1f).padding(start = 6.dp), label = { Text("пульс") },
                )
            }
            Button(
                onClick = {
                    val s = sys.toIntOrNull() ?: return@Button
                    val d = dia.toIntOrNull() ?: return@Button
                    val p = pulse.toIntOrNull() ?: return@Button
                    vm.add(s, d, p)
                    sys = ""; dia = ""; pulse = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Сохранить") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        SectionCard {
            Text("История", color = OnSurfaceMuted, fontSize = 12.sp)
            history.take(15).forEach { r ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(r.date.toLocalDate().toString())
                    Text("${r.systolicPressure}/${r.diastolicPressure}  ♥${r.pulse}", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
