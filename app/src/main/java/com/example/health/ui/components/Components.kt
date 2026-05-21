package com.example.health.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.ui.theme.BorderHairline
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.BrandGreenSoftBg
import com.example.health.ui.theme.BrandGreenSoftBorder
import com.example.health.ui.theme.CardShadowColor
import com.example.health.ui.theme.DisplayM
import com.example.health.ui.theme.LabelXs
import com.example.health.ui.theme.NeutralDark
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.SurfaceMuted
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary

// --- Карточка с мягкой тенью 0 2 12 rgba(15,20,25,0.06) ---
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    background: Color = SurfaceCard,
    borderColor: Color? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = CardShadowColor,
                spotColor = CardShadowColor,
            )
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp))
                } else Modifier,
            )
            .padding(contentPadding),
    ) {
        Column(content = { content() })
    }
}

// --- Большой заголовок экрана ---
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = DisplayM,
        color = TextPrimary,
    )
}

// --- Uppercase label с letter-spacing 0.04em ---
@Composable
fun LabelXsText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextSecondary,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = LabelXs,
        color = color,
    )
}

// --- Цветная точка-акцент для строк метрик ---
@Composable
fun AccentDot(color: Color, size: Int = 8, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size.dp)
            .background(color, CircleShape),
    )
}

// --- Lock-бейдж «Все данные только на этом устройстве» (всегда видим) ---
@Composable
fun LockBadge(
    text: String = "Все данные только на этом устройстве",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceMuted)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp),
        )
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}

// --- Мини-карточка метрики 2×2 на главной (давление/вес/пульс/самочувствие) ---
@Composable
fun MetricMiniCard(
    accent: Color,
    label: String,
    valueSlot: @Composable () -> Unit,
    caption: String,
    captionColor: Color = TextSecondary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = CardShadowColor, spotColor = CardShadowColor)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AccentDot(color = accent)
                LabelXsText(label)
            }
            Spacer(Modifier.height(8.dp))
            valueSlot()
            Spacer(Modifier.height(2.dp))
            Text(caption, color = captionColor, fontSize = 12.sp)
        }
    }
}

// --- 7-дневный мини-бар (вертикальные столбики) ---
@Composable
fun MiniWeekBars(
    heights: List<Float>,                // 0..1, 7 значений
    color: Color = BrandGreen,
    activeIndex: Int = 6,                // сегодня — последний
    height: androidx.compose.ui.unit.Dp = 32.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        heights.forEachIndexed { i, h ->
            val faded = if (i == activeIndex) color else color.copy(alpha = 0.4f)
            Box(
                Modifier
                    .weight(1f)
                    .height((h.coerceIn(0.08f, 1f) * height.value).dp)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(faded),
            )
        }
    }
}

// --- Кольцевой прогресс для шагов (SVG-стиль через Canvas) ---
@Composable
fun RingProgress(
    percent: Float,
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = 140.dp,
    strokeDp: androidx.compose.ui.unit.Dp = 12.dp,
    trackColor: Color = BrandGreenSoftBorder,
    color: Color = BrandGreen,
    centerLabel: String? = "${(percent.coerceIn(0f, 1f) * 100).toInt()}%",
) {
    Box(
        modifier.size(sizeDp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(sizeDp)) {
            val stroke = strokeDp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * percent.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        centerLabel?.let {
            Text(it, color = color, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
        }
    }
}

// --- «Chip-таб» — используется в Метриках (Шаги/Вес/Давление), в Фото (Тело/Анализы), в PDF (период) ---
@Composable
fun ChipTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = BrandGreen,
    selectedTextColor: Color = SurfaceCard,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) selectedColor else SurfaceMuted,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = if (selected) selectedTextColor else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

// --- Тёмный (NeutralDark) chip-таб — для PDF «30 дней» и Фото «Фото тела» ---
@Composable
fun DarkChipTab(text: String, selected: Boolean, onClick: () -> Unit) {
    ChipTab(
        text = text,
        selected = selected,
        onClick = onClick,
        selectedColor = NeutralDark,
        selectedTextColor = SurfaceCard,
    )
}

// --- Pill-чип статуса (для inline-чипов в Профиле и медкарте SOS) ---
@Composable
fun StatusPill(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// --- Тонкий divider ---
@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderHairline),
    )
}

// --- Универсальный пустой filler в карточке (для placeholder-плашек) ---
@Composable
fun BoxScope.PlaceholderHint(text: String) {
    Text(text, color = TextDisabled, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
}
