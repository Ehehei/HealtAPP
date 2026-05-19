package com.example.health.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.health.ui.dashboard.DashboardScreen
import com.example.health.ui.health.StateOfHealthScreen
import com.example.health.ui.photos.PhotosScreen
import com.example.health.ui.pressure.PressureScreen
import com.example.health.ui.profile.ProfileScreen
import com.example.health.ui.profile.ProfileTarget
import com.example.health.ui.report.ReportScreen
import com.example.health.ui.sos.SosScreen
import com.example.health.ui.steps.StepsScreen
import com.example.health.ui.theme.AccentCalories
import com.example.health.ui.theme.AccentHeart
import com.example.health.ui.theme.AccentMinutes
import com.example.health.ui.theme.AccentSleep
import com.example.health.ui.theme.AccentSteps
import com.example.health.ui.theme.AccentWater
import com.example.health.ui.weight.WeightScreen

enum class NavTab(val title: String, val color: Color) {
    DASHBOARD("Главная", AccentSteps),
    METRICS("Метрики", AccentMinutes),
    HEALTH("Здоровье", AccentHeart),
    PHOTOS("Фото", AccentSleep),
    REPORT("PDF", AccentWater),
    SOS("SOS", Color(0xFFD32F2F)),
    PROFILE("Профиль", AccentCalories),
}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var metricsSub by remember { mutableStateOf(MetricsTab.STEPS) }
    var profileTarget by remember { mutableStateOf<ProfileTarget?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavTab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(t.color, CircleShape),
                            )
                        },
                        label = { Text(t.title, maxLines = 1) },
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        val mod = Modifier.fillMaxSize().padding(padding)
        when (tab) {
            NavTab.DASHBOARD -> DashboardScreen(
                modifier = mod,
                onOpenMetrics = { tab = NavTab.METRICS },
                onOpenHealth = { tab = NavTab.HEALTH },
                onOpenPhotos = { tab = NavTab.PHOTOS },
                onOpenReport = { tab = NavTab.REPORT },
                onOpenScreenings = {
                    profileTarget = ProfileTarget.SCREENINGS
                    tab = NavTab.PROFILE
                },
                onOpenReminders = {
                    profileTarget = ProfileTarget.REMINDERS
                    tab = NavTab.PROFILE
                },
            )
            NavTab.METRICS -> MetricsHost(modifier = mod, current = metricsSub, onChange = { metricsSub = it })
            NavTab.HEALTH -> StateOfHealthScreen(modifier = mod)
            NavTab.PHOTOS -> PhotosScreen(modifier = mod)
            NavTab.REPORT -> ReportScreen(modifier = mod)
            NavTab.SOS -> SosScreen(modifier = mod)
            NavTab.PROFILE -> ProfileScreen(
                modifier = mod,
                initialTarget = profileTarget,
                onTargetConsumed = { profileTarget = null },
            )
        }
    }
}

enum class MetricsTab(val title: String) { STEPS("Шаги"), WEIGHT("Вес"), PRESSURE("Давление") }

@Composable
fun MetricsHost(modifier: Modifier, current: MetricsTab, onChange: (MetricsTab) -> Unit) {
    androidx.compose.foundation.layout.Column(modifier) {
        PrimaryTabRow(selectedTabIndex = current.ordinal) {
            MetricsTab.entries.forEach { t ->
                Tab(
                    selected = current == t,
                    onClick = { onChange(t) },
                    text = { Text(t.title) },
                )
            }
        }
        when (current) {
            MetricsTab.STEPS -> StepsScreen()
            MetricsTab.WEIGHT -> WeightScreen()
            MetricsTab.PRESSURE -> PressureScreen()
        }
    }
}
