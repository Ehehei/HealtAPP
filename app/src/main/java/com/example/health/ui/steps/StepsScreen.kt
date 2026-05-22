package com.example.health.ui.steps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.RingProgress
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.BrandGreenSoftBg
import com.example.health.ui.theme.BrandGreenSoftBorder
import com.example.health.ui.theme.BrandGreenText
import com.example.health.ui.theme.DisplayL
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ruLocale: Locale = Locale.forLanguageTag("ru")
private val weekOrder: List<DayOfWeek> = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

@Composable
fun StepsScreen(vm: StepsViewModel = koinViewModel()) {
    val scope = rememberCoroutineScope()
    val hcPermissions = remember { vm.healthConnectPermissions }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(hcPermissions)) {
            vm.syncFromHealthConnect()
        } else {
            vm.setMessage("Доступ к шагам не выдан в Health Connect")
        }
    }
    val onImportClick: () -> Unit = {
        scope.launch {
            when (vm.checkHealthConnectState()) {
                HealthConnectState.NOT_INSTALLED ->
                    vm.setMessage("Health Connect не установлен. Установи приложение из Google Play и повтори.")
                HealthConnectState.NEEDS_PERMISSION ->
                    permissionLauncher.launch(hcPermissions)
                HealthConnectState.READY ->
                    vm.syncFromHealthConnect()
            }
        }
    }

    val today by vm.today.collectAsState()
    val week by vm.week.collectAsState()
    val sync by vm.syncMessage.collectAsState()

    val days = week?.dailySteps.orEmpty()
    val maxSteps = (days.maxOfOrNull { it.steps } ?: 1).coerceAtLeast(1)
    val total = week?.totalSteps ?: 0
    val avg = week?.averageSteps ?: 0
    val best = days.maxOfOrNull { it.steps } ?: 0
    val percent = today?.percentOfGoal ?: 0f

    val todayDow = LocalDate.now().dayOfWeek

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {

        SectionCard(background = BrandGreenSoftBg, borderColor = BrandGreenSoftBorder) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                LabelXsText("Шаги сегодня", color = BrandGreenText)
                Spacer(Modifier.height(4.dp))
                Text(formatThousand(today?.steps ?: 0), style = DisplayL, color = BrandGreen)
                Text(
                    "из ${formatThousand(today?.goalSteps ?: 10000)}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(16.dp))
                RingProgress(
                    percent = percent.coerceIn(0f, 1f),
                    sizeDp = 160.dp,
                    strokeDp = 14.dp,
                )
            }
        }

        SectionCard {
            LabelXsText("Неделя")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                weekOrder.forEach { dow ->
                    val steps = days.firstOrNull { it.date.dayOfWeek == dow }?.steps ?: 0
                    val h = (steps.toFloat() / maxSteps * 86f).coerceAtLeast(4f)
                    val isActive = dow == todayDow
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(20.dp)
                                .height(h.dp)
                                .background(
                                    if (isActive) BrandGreen else BrandGreen.copy(alpha = 0.5f),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                ),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                weekOrder.forEach { dow ->
                    val isActive = dow == todayDow
                    Text(
                        text = dow.getDisplayName(TextStyle.SHORT, ruLocale).take(2),
                        color = if (isActive) BrandGreen else TextSecondary,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StatBox(label = "Среднее", value = formatThousand(avg), color = BrandGreen, modifier = Modifier.weight(1f))
            StatBox(label = "Лучший", value = formatThousand(best), color = TextPrimary, modifier = Modifier.weight(1f))
            StatBox(label = "Всего", value = formatThousand(total), color = TextPrimary, modifier = Modifier.weight(1f))
        }

        SectionCard {
            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) { Text("Импорт шагов из Health Connect") }
            sync?.let {
                Text(
                    it,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            LabelXsText(label, color = TextDisabled)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

private fun formatThousand(value: Int): String =
    if (value < 1000) value.toString()
    else "%,d".format(ruLocale, value).replace(',', ' ')
