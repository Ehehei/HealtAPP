package com.example.health.ui.report

import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.DarkChipTab
import com.example.health.ui.components.Divider
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LockBadge
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.NeutralDark
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

private val PERIOD_OPTIONS = listOf(7, 30, 90)
private val INCLUDED_BLOCKS = listOf(
    "Артериальное давление",
    "Динамика веса",
    "Самочувствие и симптомы",
    "Приёмы лекарств",
    "Активные напоминания",
    "Скрининги по программе РК",
)

@Composable
fun ReportScreen(modifier: Modifier = Modifier, vm: ReportViewModel = koinViewModel()) {
    val uri by vm.pdfUri.collectAsState()
    val error by vm.error.collectAsState()
    val period by vm.periodDays.collectAsState()
    val generating by vm.generating.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uri) {
        uri?.let { u ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, u)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Отправить отчёт врачу"))
            vm.consume()
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {

        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.size(10.dp))
            ScreenTitle("Отчёт для врача", modifier = Modifier.padding(0.dp))
        }

        SectionCard {
            Text(
                "Сгенерирует PDF с давлением, весом, самочувствием, приёмами лекарств, " +
                    "напоминаниями и скринингами по программе РК за выбранный период.",
                color = TextSecondary,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(14.dp))
            LabelXsText("Период")
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PERIOD_OPTIONS.forEach { days ->
                    DarkChipTab(
                        text = "$days дней",
                        selected = period == days,
                        onClick = { vm.setPeriod(days) },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        color = if (generating) NeutralDark.copy(alpha = 0.55f) else NeutralDark,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = !generating) { vm.generate() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (generating) "Готовим PDF…" else "Сформировать PDF за $period дней",
                    color = SurfaceCard,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            error?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(Modifier.height(14.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            LabelXsText("Будет включено")
            Spacer(Modifier.height(6.dp))
            INCLUDED_BLOCKS.forEach { block ->
                Row(
                    Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(block, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        LockBadge("Файл создаётся локально, не отправляется в сеть")
        Spacer(Modifier.height(12.dp))
    }
}
