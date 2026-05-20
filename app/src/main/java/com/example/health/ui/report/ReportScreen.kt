package com.example.health.ui.report

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel

private val PERIOD_OPTIONS = listOf(7, 30, 90)

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

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTitle("Отчёт для врача")
        SectionCard {
            Text(
                "Соберём твоё самочувствие за период (давление, шаги, вес, " +
                    "сахар, температура, заметки, приёмы лекарств, скрининги и " +
                    "активные напоминания) в один PDF — поделимся через любое " +
                    "приложение: мессенджер, почту или AirDrop.",
                color = OnSurfaceMuted,
                fontSize = 13.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PERIOD_OPTIONS.forEach { days ->
                    FilterChip(
                        selected = period == days,
                        onClick = { vm.setPeriod(days) },
                        label = { Text("$days дней") },
                    )
                }
            }
            Button(
                onClick = { vm.generate() },
                enabled = !generating,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text(if (generating) "Готовим PDF…" else "Сформировать PDF за $period дней") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
