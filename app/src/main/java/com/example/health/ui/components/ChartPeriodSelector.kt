package com.example.health.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ChartPeriod(val days: Int, val label: String) {
    WEEK(7, "7д"),
    MONTH(30, "30д"),
    QUARTER(90, "90д"),
}

@Composable
fun ChartPeriodSelector(
    selected: ChartPeriod,
    onChange: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ChartPeriod.entries.forEach { p ->
            FilterChip(
                selected = selected == p,
                onClick = { onChange(p) },
                label = { Text(p.label) },
            )
        }
    }
}
