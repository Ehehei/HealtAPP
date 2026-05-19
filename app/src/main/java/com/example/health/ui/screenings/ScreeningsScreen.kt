package com.example.health.ui.screenings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.Gender
import com.example.domain.model.screening.ScreeningEligibility
import com.example.domain.model.screening.ScreeningStatus
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
fun ScreeningsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ScreeningsViewModel = koinViewModel(),
) {
    val items by vm.items.collectAsState()
    val error by vm.error.collectAsState()

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Скрининги по программе РК")

        SourceBlock(vm.meta)

        val (eligible, rest) = items.partition { it.status != ScreeningStatus.NOT_ELIGIBLE }

        if (eligible.isEmpty() && rest.isEmpty()) {
            SectionCard {
                Text(
                    "Заполни профиль (дату рождения и пол) — программа подбирает скрининги по этим данным.",
                    color = OnSurfaceMuted,
                )
            }
        }

        eligible.forEach { item ->
            ScreeningCard(item = item, onLogToday = { vm.logToday(item.screening.code) })
        }

        if (rest.isNotEmpty()) {
            SectionCard {
                Text("Не положены по возрасту или полу", color = OnSurfaceMuted, fontSize = 12.sp)
                rest.forEach {
                    Text(
                        "• ${it.screening.name} (${formatTargeting(it)})",
                        color = OnSurfaceMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        SectionCard {
            TextButton(onClick = onBack) { Text("← Назад") }
        }
    }

    error?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            confirmButton = { TextButton(onClick = vm::clearError) { Text("OK") } },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun SourceBlock(meta: CatalogMeta) {
    var expanded by remember { mutableStateOf(false) }
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Откуда эти данные", color = OnSurfaceMuted, fontSize = 12.sp)
                Text(
                    if (expanded) "Скрыть" else "Подробнее",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "▲" else "▼")
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp)) {
                Text(meta.sourceLabel, fontSize = 13.sp)
                Text(meta.sourceUpdatedOn, color = OnSurfaceMuted, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Перечень и периодичность скрининга могут обновляться приказами МЗ РК. " +
                        "Сверяйтесь в поликлинике прикрепления — это приложение не заменяет консультацию врача.",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ScreeningCard(
    item: ScreeningEligibility,
    onLogToday: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.screening.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            StatusChip(item.status)
        }
        Text(item.screening.description, color = OnSurfaceMuted, fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp))
        Text("Метод: ${item.screening.method}", color = OnSurfaceMuted, fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp))
        Text(formatTargeting(item), color = OnSurfaceMuted, fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp))

        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                item.lastDoneOn?.let {
                    Text("Последний раз: ${it.format(dateFmt)}", fontSize = 12.sp)
                }
                if (item.status == ScreeningStatus.UPCOMING) {
                    item.nextDueOn?.let {
                        Text("Следующий: ${it.format(dateFmt)}", fontSize = 12.sp, color = OnSurfaceMuted)
                    }
                }
            }
            Button(
                onClick = onLogToday,
                colors = if (item.status == ScreeningStatus.DUE_NOW)
                    ButtonDefaults.buttonColors()
                else
                    ButtonDefaults.outlinedButtonColors(),
            ) { Text("Я прошёл сегодня") }
        }
    }
}

@Composable
private fun StatusChip(status: ScreeningStatus) {
    val (label, color) = when (status) {
        ScreeningStatus.DUE_NOW -> "Пора пройти" to Color(0xFFE53935)
        ScreeningStatus.UPCOMING -> "По графику" to Color(0xFF43A047)
        ScreeningStatus.NOT_ELIGIBLE -> "Не показан" to OnSurfaceMuted
    }
    AssistChip(
        onClick = {},
        label = { Text(label, fontSize = 11.sp) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color,
        ),
    )
}

private fun formatTargeting(item: ScreeningEligibility): String {
    val ageText = "${item.screening.ageRange.first}–${item.screening.ageRange.last} лет"
    val genderText = when (item.screening.eligibleGender) {
        Gender.MALE -> "мужчины"
        Gender.FEMALE -> "женщины"
        null -> "оба пола"
    }
    val interval = item.screening.intervalMonths.let { months ->
        if (months <= 0) "однократно/по показаниям"
        else "каждые ${months / 12} лет".replace("каждые 1 лет", "ежегодно")
    }
    return "$ageText · $genderText · $interval"
}
