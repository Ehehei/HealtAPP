package com.example.health.ui.screenings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Gender
import com.example.domain.model.screening.ScreeningEligibility
import com.example.domain.model.screening.ScreeningStatus
import com.example.health.ui.components.Divider
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LockBadge
import com.example.health.ui.components.SectionCard
import com.example.health.ui.components.StatusPill
import com.example.health.ui.theme.AmberSoftBg
import com.example.health.ui.theme.AmberSoftText
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.BrandGreenSoftBg
import com.example.health.ui.theme.BrandGreenText
import com.example.health.ui.theme.NeutralDark
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.SurfaceMuted
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
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
        DetailHeader(title = "Скрининги РК", onBack = onBack)

        val (eligible, rest) = items.partition { it.status != ScreeningStatus.NOT_ELIGIBLE }
        val dueCount = eligible.count { it.status == ScreeningStatus.DUE_NOW }

        // Heads-up — сколько положено сейчас
        if (dueCount > 0) {
            SectionCard(background = AmberSoftBg) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = AmberSoftText,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Пора пройти $dueCount " + screeningWord(dueCount),
                            fontWeight = FontWeight.SemiBold,
                            color = AmberSoftText,
                            fontSize = 14.sp,
                        )
                        Text(
                            "По программе бесплатных скринингов РК",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        SourceBlock(vm.meta)

        if (eligible.isEmpty() && rest.isEmpty()) {
            SectionCard {
                Text(
                    "Заполни профиль (дату рождения и пол) — программа подбирает скрининги по этим данным.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        eligible.forEach { item ->
            ScreeningCard(
                item = item,
                onLogToday = { vm.logToday(item.screening.code) },
            )
        }

        if (rest.isNotEmpty()) {
            NotEligibleBlock(rest)
        }

        LockBadge("Перечень утверждается приказами МЗ РК — сверяйтесь в поликлинике")
        Spacer(Modifier.height(12.dp))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                LabelXsText("Откуда эти данные")
                Spacer(Modifier.height(2.dp))
                Text(
                    if (expanded) "Скрыть" else "Подробнее",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 10.dp)) {
                Divider()
                Spacer(Modifier.height(10.dp))
                Text(meta.sourceLabel, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    meta.sourceUpdatedOn,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    "Перечень и периодичность могут обновляться приказами МЗ РК. " +
                        "Это приложение не заменяет консультацию врача.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
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
    val isDue = item.status == ScreeningStatus.DUE_NOW
    SectionCard(
        background = if (isDue) BrandGreenSoftBg else SurfaceCard,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Filled.MedicalServices,
                contentDescription = null,
                tint = if (isDue) BrandGreenText else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.screening.name,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatTargeting(item),
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.size(8.dp))
            ScreeningStatusPill(item.status)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            item.screening.description,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Метод: ${item.screening.method}",
            color = TextSecondary,
            fontSize = 12.sp,
        )

        if (item.lastDoneOn != null || (item.status == ScreeningStatus.UPCOMING && item.nextDueOn != null)) {
            Spacer(Modifier.height(10.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            item.lastDoneOn?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelXsText("Последний раз", color = TextDisabled)
                    Spacer(Modifier.weight(1f))
                    Text(it.format(dateFmt), fontSize = 12.sp, color = TextPrimary)
                }
            }
            if (item.status == ScreeningStatus.UPCOMING) {
                item.nextDueOn?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LabelXsText("Следующий", color = TextDisabled)
                        Spacer(Modifier.weight(1f))
                        Text(it.format(dateFmt), fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PrimaryActionButton(
            text = if (isDue) "Я прошёл сегодня" else "Отметить прохождение",
            background = if (isDue) BrandGreen else NeutralDark,
            onClick = onLogToday,
        )
    }
}

@Composable
private fun ScreeningStatusPill(status: ScreeningStatus) {
    when (status) {
        ScreeningStatus.DUE_NOW -> StatusPill(
            text = "Пора пройти",
            background = AmberSoftBg,
            contentColor = AmberSoftText,
        )
        ScreeningStatus.UPCOMING -> StatusPill(
            text = "По графику",
            background = BrandGreenSoftBg,
            contentColor = BrandGreenText,
        )
        ScreeningStatus.NOT_ELIGIBLE -> StatusPill(
            text = "Не показан",
            background = SurfaceMuted,
            contentColor = TextSecondary,
        )
    }
}

@Composable
private fun NotEligibleBlock(rest: List<ScreeningEligibility>) {
    var expanded by remember { mutableStateOf(false) }
    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                LabelXsText("Не положены по возрасту или полу")
                Spacer(Modifier.height(2.dp))
                Text(
                    "${rest.size} скринингов",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 10.dp)) {
                Divider()
                Spacer(Modifier.height(8.dp))
                rest.forEachIndexed { index, it ->
                    if (index > 0) Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("·", color = TextDisabled, fontSize = 14.sp)
                        Spacer(Modifier.size(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                it.screening.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                            )
                            Text(
                                formatTargeting(it),
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    background: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(background, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = SurfaceCard,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text,
                color = SurfaceCard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
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

private fun screeningWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "скрининг"
        mod10 in 2..4 && mod100 !in 12..14 -> "скрининга"
        else -> "скринингов"
    }
}
