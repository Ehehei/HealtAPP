package com.example.health.ui.theme

import androidx.compose.ui.graphics.Color

// Базовые поверхности — off-white фон, чистый белый для карточек.
val SurfaceBg = Color(0xFFFAFAFB)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF3F4F6)      // светло-серый «pill»
val SurfaceField = Color(0xFFF9FAFB)      // поле ввода

// Текст
val TextPrimary = Color(0xFF0F1419)
val TextSecondary = Color(0xFF6B7280)
val TextDisabled = Color(0xFF9CA3AF)

// Бордеры
val BorderHairline = Color(0xFFE5E7EB)

// Бренд: зелёный — основное действие/успех/активность.
val BrandGreen = Color(0xFF2EB872)
val BrandGreenSoftBg = Color(0xFFF0FDF4)
val BrandGreenSoftBorder = Color(0xFFBBF7D0)
val BrandGreenText = Color(0xFF15803D)

// Акценты метрик (точки на 2×2 сетке)
val AccentRed = Color(0xFFE5484D)       // давление, пульс
val AccentBlue = Color(0xFF3B82F6)      // вес
val AccentAmber = Color(0xFFF59E0B)     // самочувствие
val AccentTeal = Color(0xFF06B6D4)      // вода

// Янтарный набор (самочувствие, скрининги «положен»)
val AmberSoftBg = Color(0xFFFEF3C7)
val AmberSoftText = Color(0xFF92400E)

// Синий набор (профиль avatar, статус-чипы)
val BlueSoftBg = Color(0xFFDBEAFE)
val BlueSoftText = Color(0xFF1D4ED8)

// SOS / опасность — изолированная палитра, используется только на SosScreen и шифрование AES.
val SosRed = Color(0xFFD32F2F)
val SosRedRing = Color(0xFFFFCDD2)
val SosScreenBg = Color(0xFFFFF5F5)
val SosBannerBg = Color(0xFFFEE2E2)
val SosBannerBorder = Color(0xFFFECACA)
val SosTextDeep = Color(0xFF7F1D1D)
val SosTextStrong = Color(0xFFDC2626)
val SosCardBg = Color(0xFFFEF2F2)

// Нейтральный тёмный (кнопка «Сравнить прогресс», активный «30 дней»)
val NeutralDark = Color(0xFF0F1419)

// Тень карточки 0 2 12 rgba(15,20,25,0.06) — alpha ≈ 0.06
val CardShadowColor = Color(0x0F0F1419)
