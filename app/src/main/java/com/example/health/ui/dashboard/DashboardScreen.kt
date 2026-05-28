package com.example.health.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FeelingLevel
import com.example.health.ui.components.AccentDot
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LockBadge
import com.example.health.ui.components.MetricMiniCard
import com.example.health.ui.components.MiniWeekBars
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentAmber
import com.example.health.ui.theme.AccentBlue
import com.example.health.ui.theme.AccentRed
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.BrandGreenSoftBg
import com.example.health.ui.theme.BrandGreenSoftBorder
import com.example.health.ui.theme.BrandGreenText
import com.example.health.ui.theme.DisplayL
import com.example.health.ui.theme.PressureMono
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale: Locale = Locale.forLanguageTag("ru")
private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ruLocale)

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
    val bars by vm.weekBars.collectAsState()
    val pendingScreenings by vm.pendingScreenings.collectAsState()
    val todayReminders by vm.todayReminders.collectAsState()

    // ViewModel живёт в scope Activity и переживает переключение вкладок, поэтому
    // init { refresh() } срабатывает лишь однажды. При каждом возврате на «Главную»
    // composable пересоздаётся — перечитываем свежие метрики (шаги, вес, давление, самочувствие).
    LaunchedEffect(Unit) { vm.refresh() }

    val today = LocalDate.now()
    val greeting = greetingFor(LocalTime.now().hour)
    val userName = state?.userName?.takeIf { it.isNotBlank() } ?: "друг"

    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {

        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 4.dp)) {
            Text(
                text = dateFmt.format(today).replaceFirstChar { it.titlecase(ruLocale) },
                color = TextSecondary,
                fontSize = 13.sp,
            )
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        HeroSteps(
            steps = state?.todaySteps ?: 0,
            goal = 10_000,
            percent = state?.stepGoalPercent ?: 0f,
            weekBars = bars,
        )

        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.weight(1f)) {
                MetricMiniCard(
                    accent = AccentRed,
                    label = "Давление",
                    valueSlot = {
                        Text(
                            text = state?.latestBloodPressure?.let {
                                "${it.systolicPressure}/${it.diastolicPressure}"
                            } ?: "—",
                            style = PressureMono,
                            color = TextPrimary,
                        )
                    },
                    caption = state?.bpClassification?.let { classificationLabel(it.name) } ?: "—",
                    captionColor = BrandGreenText,
                )
            }
            Box(Modifier.weight(1f)) {
                MetricMiniCard(
                    accent = AccentBlue,
                    label = "Вес",
                    valueSlot = {
                        Text(
                            text = state?.latestWeight?.let { "%.1f".format(it) } ?: "—",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                    },
                    caption = state?.weightChangeSinceStart?.let {
                        val sign = if (it < 0) "↘" else if (it > 0) "↗" else "→"
                        "$sign ${"%+.1f".format(it)} кг с начала"
                    } ?: "кг",
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.weight(1f)) {
                MetricMiniCard(
                    accent = AccentRed,
                    label = "Пульс",
                    valueSlot = {
                        Text(
                            text = state?.latestBloodPressure?.pulse?.toString() ?: "—",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                    },
                    caption = "уд/мин",
                )
            }
            Box(Modifier.weight(1f)) {
                MetricMiniCard(
                    accent = AccentAmber,
                    label = "Самочувствие",
                    valueSlot = {
                        Text(
                            text = state?.todayFeeling?.let { emojiForFeeling(it) } ?: "—",
                            fontSize = 22.sp,
                        )
                    },
                    caption = state?.todayFeeling?.let { feelingLabel(it) } ?: "не отмечено",
                )
            }
        }

        if (pendingScreenings > 0 || todayReminders > 0) {
            SectionCard {
                LabelXsText("Запланировано")
                Spacer(Modifier.height(8.dp))
                if (todayReminders > 0) {
                    PlannedRow(
                        accent = AccentBlue,
                        title = "Напоминания на сегодня",
                        count = todayReminders,
                        onClick = onOpenReminders,
                    )
                }
                if (pendingScreenings > 0) {
                    if (todayReminders > 0) Spacer(Modifier.height(6.dp))
                    PlannedRow(
                        accent = AccentRed,
                        title = "Скрининги к прохождению",
                        count = pendingScreenings,
                        onClick = onOpenScreenings,
                    )
                }
            }
        }

        SectionCard {
            LabelXsText("Быстрые действия")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QuickAction("Метрики", BrandGreen, onOpenMetrics)
                QuickAction("Здоровье", AccentRed, onOpenHealth)
                QuickAction("Фото", AccentBlue, onOpenPhotos)
                QuickAction("PDF", AccentAmber, onOpenReport)
            }
        }

        LockBadge()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeroSteps(
    steps: Int,
    goal: Int,
    percent: Float,
    weekBars: List<Float>,
) {
    SectionCard(
        background = BrandGreenSoftBg,
        borderColor = BrandGreenSoftBorder,
    ) {
        LabelXsText("Шаги сегодня", color = BrandGreenText)
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatThousand(steps),
            style = DisplayL,
            color = BrandGreen,
        )
        Text(
            text = "из ${formatThousand(goal)} шагов",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { percent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = BrandGreen,
            trackColor = BrandGreenSoftBorder,
        )
        Spacer(Modifier.height(12.dp))
        MiniWeekBars(heights = weekBars, color = BrandGreen, activeIndex = 6, height = 36.dp)
    }
}

@Composable
private fun PlannedRow(
    accent: Color,
    title: String,
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(accent.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                count.toString(),
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            fontSize = 14.sp,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun QuickAction(label: String, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AccentDot(color = color, size = 14)
        }
        Text(label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Доброе утро"
    in 12..17 -> "Добрый день"
    in 18..22 -> "Добрый вечер"
    else -> "Доброй ночи"
}

private fun formatThousand(value: Int): String =
    if (value < 1000) value.toString()
    else "%,d".format(ruLocale, value).replace(',', ' ')

private fun emojiForFeeling(level: FeelingLevel): String = when (level) {
    FeelingLevel.EXCELLENT -> "😊"
    FeelingLevel.GOOD -> "🙂"
    FeelingLevel.FAIR -> "😌"
    FeelingLevel.POOR -> "😕"
    FeelingLevel.BAD -> "😟"
}

private fun feelingLabel(level: FeelingLevel): String = when (level) {
    FeelingLevel.EXCELLENT -> "отличное"
    FeelingLevel.GOOD -> "хорошее"
    FeelingLevel.FAIR -> "среднее"
    FeelingLevel.POOR -> "плохое"
    FeelingLevel.BAD -> "тяжёлое"
}

private fun classificationLabel(name: String): String = when (name) {
    "NORMAL" -> "Норма"
    "ELEVATED" -> "Повышено"
    "HYPERTENSION_STAGE_1" -> "Гипертония I"
    "HYPERTENSION_STAGE_2" -> "Гипертония II"
    "HYPERTENSIVE_CRISIS" -> "Криз"
    else -> "—"
}
