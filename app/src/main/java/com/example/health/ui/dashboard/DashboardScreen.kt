package com.example.health.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.MetricRow
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentCalories
import com.example.health.ui.theme.AccentHeart
import com.example.health.ui.theme.AccentMinutes
import com.example.health.ui.theme.AccentSleep
import com.example.health.ui.theme.AccentSteps
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onOpenMetrics: () -> Unit = {},
    onOpenHealth: () -> Unit = {},
    onOpenPhotos: () -> Unit = {},
    onOpenReport: () -> Unit = {},
    onOpenScreenings: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    vm: DashboardViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()
    val pendingScreenings by vm.pendingScreenings.collectAsState()
    val todayReminders by vm.todayReminders.collectAsState()

    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle(text = "Привет, ${state?.userName ?: "друг"}")

        SectionCard {
            Text("Сегодня", color = OnSurfaceMuted, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${state?.todaySteps ?: 0}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("шагов", color = OnSurfaceMuted, fontSize = 13.sp)
                }
                ProgressRing(percent = state?.stepGoalPercent ?: 0f)
            }
            LinearProgressIndicator(
                progress = { (state?.stepGoalPercent ?: 0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = AccentSteps,
            )
        }

        SectionCard {
            Text("Метрики", color = OnSurfaceMuted, fontSize = 12.sp)
            MetricRow(
                color = AccentMinutes,
                label = "Вес",
                value = state?.latestWeight?.let { "%.1f кг".format(it) } ?: "—",
            )
            MetricRow(
                color = AccentCalories,
                label = "ИМТ",
                value = state?.bmi?.let { "%.1f".format(it) } ?: "—",
            )
            MetricRow(
                color = AccentHeart,
                label = "Давление",
                value = state?.latestBloodPressure?.let {
                    "${it.systolicPressure}/${it.diastolicPressure}"
                } ?: "—",
            )
            MetricRow(
                color = AccentSleep,
                label = "Самочувствие",
                value = state?.todayFeeling?.name ?: "—",
            )
        }

        if (pendingScreenings > 0 || todayReminders > 0) {
            SectionCard {
                Text("Запланировано", color = OnSurfaceMuted, fontSize = 12.sp)
                if (pendingScreenings > 0) {
                    WidgetRow(
                        accent = AccentHeart,
                        title = "Скрининги к прохождению",
                        value = pendingScreenings.toString(),
                        onClick = onOpenScreenings,
                    )
                }
                if (todayReminders > 0) {
                    WidgetRow(
                        accent = AccentMinutes,
                        title = "Напоминания на сегодня",
                        value = todayReminders.toString(),
                        onClick = onOpenReminders,
                    )
                }
            }
        }

        QuickRow(
            onMetrics = onOpenMetrics,
            onHealth = onOpenHealth,
            onPhotos = onOpenPhotos,
            onReport = onOpenReport,
        )
    }
}

@Composable
private fun WidgetRow(
    accent: Color,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(value, fontWeight = FontWeight.Bold, color = accent)
                }
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Text("›", color = OnSurfaceMuted, fontSize = 22.sp)
        }
    }
}

@Composable
private fun ProgressRing(percent: Float) {
    Box(
        modifier = Modifier.size(80.dp).clip(CircleShape).background(AccentSteps.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text("${(percent * 100).toInt()}%", fontWeight = FontWeight.Bold, color = AccentSteps)
    }
}

@Composable
private fun QuickRow(
    onMetrics: () -> Unit,
    onHealth: () -> Unit,
    onPhotos: () -> Unit,
    onReport: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        QuickAction("Метрики", AccentSteps, onMetrics)
        QuickAction("Здоровье", AccentHeart, onHealth)
        QuickAction("Фото", AccentMinutes, onPhotos)
        QuickAction("PDF", AccentSleep, onReport)
    }
}

@Composable
private fun QuickAction(label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = color.copy(alpha = 0.18f),
            modifier = Modifier.size(58.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(20.dp).background(color, CircleShape))
            }
        }
        Text(label, fontSize = 11.sp, color = OnSurfaceMuted, modifier = Modifier.padding(top = 6.dp))
    }
}
