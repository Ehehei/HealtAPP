package com.example.health.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.AccentSteps
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel

@Composable
fun StepsScreen(vm: StepsViewModel = koinViewModel()) {
    val today by vm.today.collectAsState()
    val week by vm.week.collectAsState()
    val sync by vm.syncMessage.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTitle("Активность")

        SectionCard {
            Text("Сегодня", color = OnSurfaceMuted, fontSize = 12.sp)
            Text(
                "${today?.steps ?: 0} / ${today?.goalSteps ?: 10000}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${((today?.percentOfGoal ?: 0f) * 100).toInt()}% дневной цели",
                color = OnSurfaceMuted,
                fontSize = 13.sp,
            )
        }

        SectionCard {
            Text("Неделя", color = OnSurfaceMuted, fontSize = 12.sp)
            val days = week?.dailySteps.orEmpty()
            val maxSteps = (days.maxOfOrNull { it.steps } ?: 1).coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEach { rec ->
                    val h = (rec.steps.toFloat() / maxSteps * 100).coerceAtLeast(4f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(22.dp)
                                .height(h.dp)
                                .background(AccentSteps, RoundedCornerShape(6.dp)),
                        )
                        Text(
                            rec.date.dayOfWeek.name.take(2),
                            fontSize = 10.sp,
                            color = OnSurfaceMuted,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            Text(
                "Итого: ${week?.totalSteps ?: 0}, в среднем ${week?.averageSteps ?: 0}",
                modifier = Modifier.padding(top = 12.dp),
                color = OnSurfaceMuted,
                fontSize = 13.sp,
            )
        }

        SectionCard {
            Button(
                onClick = vm::syncFromHealthConnect,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Импорт шагов из Health Connect") }
            sync?.let {
                Text(it, color = OnSurfaceMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
