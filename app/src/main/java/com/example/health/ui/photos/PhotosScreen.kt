package com.example.health.ui.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.BodyPhoto
import com.example.domain.model.PhotoType
import com.example.health.ui.components.ChipTab
import com.example.health.ui.components.DarkChipTab
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.BorderHairline
import com.example.health.ui.theme.NeutralDark
import com.example.health.ui.theme.SosBannerBg
import com.example.health.ui.theme.SosBannerBorder
import com.example.health.ui.theme.SosTextStrong
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.SurfaceMuted
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val photoDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))

@Composable
fun PhotosScreen(modifier: Modifier = Modifier, vm: PhotosViewModel = koinViewModel()) {
    val type by vm.type.collectAsState()
    val body by vm.bodyPhotos.collectAsState()
    val analyses by vm.analysisPhotos.collectAsState()
    val pairs by vm.pairs.collectAsState()
    val verdict by vm.verdict.collectAsState()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { vm.addPhoto(it, type) } }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Фото")

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DarkChipTab(
                text = "Фото тела",
                selected = type == PhotoType.BODY,
                onClick = { vm.setType(PhotoType.BODY) },
            )
            ChipTab(
                text = "Анализы",
                selected = type == PhotoType.ANALYSIS,
                onClick = { vm.setType(PhotoType.ANALYSIS) },
                selectedColor = NeutralDark,
                selectedTextColor = SurfaceCard,
            )
        }

        val list = if (type == PhotoType.BODY) body else analyses

        SectionCard {
            LabelXsText("Галерея · ${list.size}")
            Spacer(Modifier.height(10.dp))
            val withAdd: List<BodyPhoto?> = list + null
            withAdd.chunked(2).forEach { rowItems ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { item ->
                        Box(Modifier.weight(1f)) {
                            if (item == null) {
                                AddPhotoTile(onClick = { picker.launch("image/*") })
                            } else {
                                PhotoTile(photo = item, vm = vm)
                            }
                        }
                    }

                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        if (type == PhotoType.BODY) {

            SectionCard(background = NeutralDark) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.refreshPairs() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = SurfaceCard,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Сравнить прогресс", color = SurfaceCard, fontWeight = FontWeight.SemiBold)
                }
            }

            verdict?.let { v ->
                SectionCard {
                    LabelXsText("Результат сравнения")
                    Spacer(Modifier.height(6.dp))
                    Text(verdictLabel(v.verdict.name), fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(v.message, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    if (pairs.isNotEmpty()) {
                        val pair = pairs.last()
                        val beforeBytes = remember(pair.before.filePath) { vm.bytesOf(pair.before.filePath) }
                        val afterBytes = remember(pair.after.filePath) { vm.bytesOf(pair.after.filePath) }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AsyncImage(
                                model = beforeBytes,
                                contentDescription = null,
                                modifier = Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(12.dp)),
                            )
                            AsyncImage(
                                model = afterBytes,
                                contentDescription = null,
                                modifier = Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SosBannerBg)
                .border(1.dp, SosBannerBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = SosTextStrong,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "ФОТО ЗАШИФРОВАНЫ ЛОКАЛЬНО · AES-256",
                        color = SosTextStrong,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    "Не покидают устройство. Никогда.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PhotoTile(photo: BodyPhoto, vm: PhotosViewModel) {
    val bytes = remember(photo.filePath) { vm.bytesOf(photo.filePath) }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceMuted),
    ) {
        AsyncImage(
            model = bytes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x99000000))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = photo.date.format(photoDateFmt),
                color = SurfaceCard,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.5.dp,
                color = BorderHairline,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Добавить фото",
            tint = TextDisabled,
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun verdictLabel(name: String): String = when (name) {
    "TOO_EARLY" -> "Ещё рано"
    "NOISE" -> "Шум, не зацикливайся"
    "SUBTLE_PROGRESS" -> "Лёгкий прогресс"
    "CLEAR_PROGRESS" -> "Чёткий прогресс"
    "REGRESSION" -> "Откат"
    else -> "—"
}
