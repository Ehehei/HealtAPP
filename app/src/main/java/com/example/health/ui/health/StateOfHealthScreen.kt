package com.example.health.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FeelingLevel
import com.example.health.ui.components.ChartPeriod
import com.example.health.ui.components.ChartPeriodSelector
import com.example.health.ui.components.ChartPoint
import com.example.health.ui.components.ChartSeries
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LineChart
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentAmber
import com.example.health.ui.theme.AmberSoftBg
import com.example.health.ui.theme.AmberSoftText
import com.example.health.ui.theme.BorderHairline
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale: Locale = Locale.forLanguageTag("ru")
private val subFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", ruLocale)
private val histFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", ruLocale)

@Composable
fun StateOfHealthScreen(modifier: Modifier = Modifier, vm: StateOfHealthViewModel = koinViewModel()) {
    val trend by vm.trend.collectAsState()
    val history by vm.history.collectAsState()
    val error by vm.error.collectAsState()

    var feeling by remember { mutableStateOf(FeelingLevel.FAIR) }
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
        Text(
            subFmt.format(LocalDate.now()).replaceFirstChar { it.titlecase(ruLocale) },
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )

        SectionCard {
            Text("Как вы себя чувствуете?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
            EmojiSelector(selected = feeling, onChange = { feeling = it })

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    Column {
                        LabelXsText("Сахар (ммоль/л)")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sugar,
                            onValueChange = { sugar = it.filter { c -> c.isDigit() || c == '.' } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("5.4", color = TextSecondary) },
                            singleLine = true,
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    Column {
                        LabelXsText("Темп. (°C)")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = temp,
                            onValueChange = { temp = it.filter { c -> c.isDigit() || c == '.' } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("36.6", color = TextSecondary) },
                            singleLine = true,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            LabelXsText("Заметки")
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Лёгкая усталость к вечеру", color = TextSecondary) },
                minLines = 2,
            )

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    vm.add(feeling, sugar.toFloatOrNull(), temp.toFloatOrNull(), notes.ifBlank { null })
                    sugar = ""; temp = ""; notes = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
            ) { Text("Сохранить запись", fontWeight = FontWeight.SemiBold) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }

        SectionCard {
            LabelXsText("Динамика самочувствия (1–5)")
            Spacer(Modifier.height(6.dp))
            ChartPeriodSelector(period, { period = it })
            val cutoff = LocalDate.now().minusDays(period.days.toLong()).toEpochDay()
            val points = history
                .filter { it.date.toEpochDay() >= cutoff }
                .groupBy { it.date }
                .map { (date, list) ->
                    val avg = list.map { it.feelingLevel.value }.average().toFloat()
                    ChartPoint(date.toEpochDay(), avg)
                }
                .sortedBy { it.xEpochDay }
            LineChart(
                series = listOf(ChartSeries("Самочувствие", AccentAmber, points)),
                yLabelFormat = { "%.1f".format(it) },
            )
        }

        SectionCard {
            LabelXsText("Тренд за 30 дней")
            Spacer(Modifier.height(6.dp))
            Text(
                "Среднее: ${"%.1f".format(trend?.averageFeeling ?: 0f)} / 5",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            trend?.bloodSugarAvg?.let {
                Text("Сахар (ср): ${"%.1f".format(it)}", color = TextSecondary, fontSize = 13.sp)
            }
            trend?.temperatureAvg?.let {
                Text("Темп. (ср): ${"%.1f".format(it)}", color = TextSecondary, fontSize = 13.sp)
            }
        }

        SectionCard {
            LabelXsText("История")
            Spacer(Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text("Нет записей", color = TextSecondary, fontSize = 13.sp)
            } else {
                history.takeLast(8).reversed().forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(emojiFor(entry.feelingLevel), fontSize = 22.sp)
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                histFmt.format(entry.date),
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                            )
                            Text(
                                entry.notes?.takeIf { it.isNotBlank() } ?: feelingLabel(entry.feelingLevel),
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun EmojiSelector(selected: FeelingLevel, onChange: (FeelingLevel) -> Unit) {
    val order = listOf(
        FeelingLevel.BAD,
        FeelingLevel.POOR,
        FeelingLevel.FAIR,
        FeelingLevel.GOOD,
        FeelingLevel.EXCELLENT,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        order.forEach { f ->
            val isSelected = f == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) AmberSoftBg else SurfaceCard,
                        shape = CircleShape,
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) AccentAmber else BorderHairline,
                        shape = CircleShape,
                    )
                    .clickable { onChange(f) },
                contentAlignment = Alignment.Center,
            ) {
                Text(emojiFor(f), fontSize = 22.sp)
            }
        }
    }
}

private fun emojiFor(level: FeelingLevel): String = when (level) {
    FeelingLevel.BAD -> "😟"
    FeelingLevel.POOR -> "😕"
    FeelingLevel.FAIR -> "😌"
    FeelingLevel.GOOD -> "🙂"
    FeelingLevel.EXCELLENT -> "😊"
}

private fun feelingLabel(level: FeelingLevel): String = when (level) {
    FeelingLevel.BAD -> "Тяжёлое самочувствие"
    FeelingLevel.POOR -> "Плохое самочувствие"
    FeelingLevel.FAIR -> "Среднее самочувствие"
    FeelingLevel.GOOD -> "Хорошее самочувствие"
    FeelingLevel.EXCELLENT -> "Отличное самочувствие"
}
