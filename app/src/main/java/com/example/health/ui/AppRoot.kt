package com.example.health.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.dashboard.DashboardScreen
import com.example.health.ui.health.StateOfHealthScreen
import com.example.health.ui.photos.PhotosScreen
import com.example.health.ui.pressure.PressureScreen
import com.example.health.ui.profile.ProfileScreen
import com.example.health.ui.profile.ProfileTarget
import com.example.health.ui.report.ReportScreen
import com.example.health.ui.sos.SosScreen
import com.example.health.ui.steps.StepsScreen
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.SosRed
import com.example.health.ui.theme.SosScreenBg
import com.example.health.ui.theme.SurfaceBg
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.weight.WeightScreen

enum class NavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Главная", Icons.Filled.Home),
    METRICS("Метрики", Icons.AutoMirrored.Filled.ShowChart),
    HEALTH("Здоровье", Icons.Filled.MonitorHeart),
    PHOTOS("Фото", Icons.Filled.CameraAlt),
    REPORT("PDF", Icons.Filled.PictureAsPdf),
    SOS("SOS", Icons.Filled.Emergency),
    PROFILE("Профиль", Icons.Filled.Person),
}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var metricsSub by remember { mutableStateOf(MetricsTab.STEPS) }
    var profileTarget by remember { mutableStateOf<ProfileTarget?>(null) }

    val isSos = tab == NavTab.SOS
    val containerBg = if (isSos) SosScreenBg else SurfaceBg

    Scaffold(
        containerColor = containerBg,
        bottomBar = {
            NavigationBar(
                containerColor = if (isSos) SosScreenBg else SurfaceCard,
                tonalElevation = 0.dp,
            ) {
                NavTab.entries.forEach { t ->
                    val selected = tab == t
                    val isSosItem = t == NavTab.SOS
                    val selectedColor = if (isSosItem) SosRed else BrandGreen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                imageVector = t.icon,
                                contentDescription = t.title,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        label = {
                            Text(t.title, fontSize = 10.sp, maxLines = 1)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextDisabled,
                            unselectedTextColor = TextDisabled,
                        ),
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
    Column(modifier) {
        PrimaryTabRow(
            selectedTabIndex = current.ordinal,
            containerColor = SurfaceBg,
            contentColor = TextPrimary,
        ) {
            MetricsTab.entries.forEach { t ->
                Tab(
                    selected = current == t,
                    onClick = { onChange(t) },
                    text = { Text(t.title) },
                    selectedContentColor = BrandGreen,
                    unselectedContentColor = TextDisabled,
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
