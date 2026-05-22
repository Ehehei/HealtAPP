package com.example.health.ui.pressure

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.ChartLegend
import com.example.health.ui.components.ChartPeriod
import com.example.health.ui.components.ChartPeriodSelector
import com.example.health.ui.components.ChartPoint
import com.example.health.ui.components.ChartSeries
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LineChart
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentBlue
import com.example.health.ui.theme.AccentRed
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.PressureMono
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

private val SystolicColor = AccentRed
private val DiastolicColor = AccentBlue

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

        SectionCard {
            LabelXsText("Среднее за 30 дней")
            Spacer(Modifier.height(6.dp))
            stats?.let { s ->
                Text(
                    "${s.avgSystolic.toInt()}/${s.avgDiastolic.toInt()}",
                    style = PressureMono.copy(fontSize = 36.sp),
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Пульс ~${s.avgPulse.toInt()} · ${classificationLabel(s.classification.name)}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            } ?: Text("Нет данных за 30 дней", color = TextSecondary)
        }

        SectionCard {
            LabelXsText("Динамика")
            Spacer(Modifier.height(6.dp))
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
            Spacer(Modifier.height(4.dp))
            ChartLegend(series = series)
        }

        SectionCard {
            LabelXsText("Новое измерение")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
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
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val s = sys.toIntOrNull() ?: return@Button
                    val d = dia.toIntOrNull() ?: return@Button
                    val p = pulse.toIntOrNull() ?: return@Button
                    vm.add(s, d, p)
                    sys = ""; dia = ""; pulse = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        SectionCard {
            LabelXsText("История")
            Spacer(Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text("Нет записей", color = TextSecondary, fontSize = 13.sp)
            } else {
                history.take(15).forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(r.date.toLocalDate().toString(), color = TextSecondary, fontSize = 13.sp)
                        Text(
                            "${r.systolicPressure}/${r.diastolicPressure}  ♥${r.pulse}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun classificationLabel(name: String): String = when (name) {
    "NORMAL" -> "норма"
    "ELEVATED" -> "повышено"
    "HYPERTENSION_STAGE_1" -> "гипертония I"
    "HYPERTENSION_STAGE_2" -> "гипертония II"
    "HYPERTENSIVE_CRISIS" -> "криз"
    else -> "—"
}
