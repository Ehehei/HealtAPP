package com.example.health.ui.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.PhotoType
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel

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

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenTitle("Фотодневник")

        SectionCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == PhotoType.BODY,
                    onClick = { vm.setType(PhotoType.BODY) },
                    label = { Text("Тело") },
                )
                FilterChip(
                    selected = type == PhotoType.ANALYSIS,
                    onClick = { vm.setType(PhotoType.ANALYSIS) },
                    label = { Text("Анализы") },
                )
            }
            Text(
                "Все фото хранятся локально на устройстве — никуда не отправляются.",
                color = OnSurfaceMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Button(
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Добавить фото") }
        }

        val list = if (type == PhotoType.BODY) body else analyses
        SectionCard {
            Text("Галерея (${list.size})", color = OnSurfaceMuted, fontSize = 12.sp)
            LazyRow(
                Modifier.fillMaxWidth().padding(top = 8.dp).height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list) { photo ->
                    val bytes = remember(photo.filePath) { vm.bytesOf(photo.filePath) }
                    AsyncImage(
                        model = bytes,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }

        if (type == PhotoType.BODY) {
            SectionCard {
                Button(
                    onClick = { vm.refreshPairs() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сравнить прогресс по фото") }
                verdict?.let { v ->
                    Text(
                        v.verdict.name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(v.message, color = OnSurfaceMuted, fontSize = 13.sp)
                    if (pairs.isNotEmpty()) {
                        val pair = pairs.last()
                        val beforeBytes = remember(pair.before.filePath) { vm.bytesOf(pair.before.filePath) }
                        val afterBytes = remember(pair.after.filePath) { vm.bytesOf(pair.after.filePath) }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
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
    }
}

