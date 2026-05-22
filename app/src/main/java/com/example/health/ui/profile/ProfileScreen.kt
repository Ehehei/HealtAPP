package com.example.health.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BloodType
import com.example.domain.model.Gender
import com.example.health.ui.catalog.MedicationCatalogScreen
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LockBadge
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.components.StatusPill
import com.example.health.ui.reminders.RemindersScreen
import com.example.health.ui.screenings.ScreeningsScreen
import com.example.health.ui.theme.AmberSoftBg
import com.example.health.ui.theme.AmberSoftText
import com.example.health.ui.theme.BlueSoftBg
import com.example.health.ui.theme.BlueSoftText
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.SosBannerBg
import com.example.health.ui.theme.SosTextDeep
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ProfileTarget { REMINDERS, SCREENINGS }

private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    initialTarget: ProfileTarget? = null,
    onTargetConsumed: () -> Unit = {},
    vm: ProfileViewModel = koinViewModel(),
) {
    var showReminders by remember { mutableStateOf(false) }
    var showScreenings by remember { mutableStateOf(false) }
    var showCatalog by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    LaunchedEffect(initialTarget) {
        when (initialTarget) {
            ProfileTarget.REMINDERS -> showReminders = true
            ProfileTarget.SCREENINGS -> showScreenings = true
            null -> Unit
        }
        if (initialTarget != null) onTargetConsumed()
    }

    if (showReminders) {
        RemindersScreen(onBack = { showReminders = false }, modifier = modifier)
        return
    }
    if (showScreenings) {
        ScreeningsScreen(onBack = { showScreenings = false }, modifier = modifier)
        return
    }
    if (showCatalog) {
        MedicationCatalogScreen(onBack = { showCatalog = false }, modifier = modifier)
        return
    }
    if (showEdit) {
        ProfileEditScreen(onBack = { showEdit = false }, modifier = modifier, vm = vm)
        return
    }

    val profile by vm.profile.collectAsState()
    val nextMed by vm.nextMedReminder.collectAsState()
    val screeningsDue by vm.screeningsPending.collectAsState()

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Профиль")

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = profile?.name.orEmpty())
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        profile?.name?.takeIf { it.isNotBlank() } ?: "Без имени",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = buildString {
                            vm.age()?.let { append("$it ${ageNoun(it)}") }
                            profile?.height?.takeIf { it > 0 }?.let {
                                if (isNotEmpty()) append(" · ")
                                append("${it.toInt()} см")
                            }
                            profile?.initialWeightKg?.takeIf { it > 0 }?.let {
                                if (isNotEmpty()) append(" · ")
                                append("%.1f кг".format(it))
                            }
                        },
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Редактировать",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { showEdit = true },
                )
            }
        }

        SectionCard {
            LabelXsText("Медкарта (SOS)")
            Spacer(Modifier.size(8.dp))
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Группа крови", color = TextSecondary, fontSize = 13.sp)
                StatusPill(
                    text = profile?.bloodType?.label ?: BloodType.UNKNOWN.label,
                    background = SosBannerBg,
                    contentColor = SosTextDeep,
                )
            }
            Spacer(Modifier.size(4.dp))
            ProfileTextRow("Аллергии", profile?.allergies?.ifBlank { "—" } ?: "—")
            ProfileTextRow("Хронические", profile?.chronicConditions?.ifBlank { "—" } ?: "—")
        }

        SectionCard {
            LabelXsText("Сервисы")
            Spacer(Modifier.size(8.dp))
            ServiceRow(
                icon = Icons.Filled.Notifications,
                title = "Мои напоминания",
                trailing = {
                    nextMed?.let { n ->
                        StatusPill(
                            text = "${n.medicationName} ${n.timeOfDay.format(timeFmt)}",
                            background = BlueSoftBg,
                            contentColor = BlueSoftText,
                        )
                    }
                },
                onClick = { showReminders = true },
            )
            Spacer(Modifier.size(8.dp))
            ServiceRow(
                icon = Icons.Filled.MedicalServices,
                title = "Скрининги по программе РК",
                trailing = {
                    if (screeningsDue) {
                        StatusPill(
                            text = "Положен",
                            background = AmberSoftBg,
                            contentColor = AmberSoftText,
                        )
                    }
                },
                onClick = { showScreenings = true },
            )
            Spacer(Modifier.size(8.dp))
            ServiceRow(
                icon = Icons.Filled.Medication,
                title = "Справочник лекарств РК",
                trailing = {},
                onClick = { showCatalog = true },
            )
        }

        LockBadge()
        Spacer(Modifier.size(12.dp))
    }
}

@Composable
private fun Avatar(name: String) {
    val initials = name
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
    Box(
        Modifier
            .size(44.dp)
            .background(BlueSoftBg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = BlueSoftText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun ProfileTextRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun ServiceRow(
    icon: ImageVector,
    title: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        trailing()
        Spacer(Modifier.size(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ProfileEditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ProfileViewModel,
) {
    val profile by vm.profile.collectAsState()

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var bloodType by remember { mutableStateOf(BloodType.UNKNOWN) }
    var allergies by remember { mutableStateOf("") }
    var chronic by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile) {
        profile?.let {
            if (name.isEmpty()) name = it.name
            if (height.isEmpty()) height = it.height.toString()
            if (weight.isEmpty()) weight = it.initialWeightKg.toString()
            if (birth.isEmpty()) birth = it.birthDate.toString()
            gender = it.gender
            if (bloodType == BloodType.UNKNOWN) bloodType = it.bloodType
            if (allergies.isEmpty()) allergies = it.allergies
            if (chronic.isEmpty()) chronic = it.chronicConditions
            if (contactName.isEmpty()) contactName = it.emergencyContactName
            if (contactPhone.isEmpty()) contactPhone = it.emergencyContactPhone
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Редактировать профиль")

        SectionCard {
            LabelXsText("Основное")
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Имя") })
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                height, { height = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth(),
                label = { Text("Рост, см") },
            )
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                weight, { weight = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth(),
                label = { Text("Стартовый вес, кг") },
            )
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                birth, { birth = it; saveError = null },
                Modifier.fillMaxWidth(),
                label = { Text("Дата рождения") },
                placeholder = { Text("1990-05-15 или 15.05.1990", color = TextSecondary) },
                singleLine = true,
            )
            Spacer(Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(if (g == Gender.MALE) "Мужской" else "Женский") },
                    )
                }
            }
        }

        SectionCard {
            LabelXsText("Медкарта (SOS)")
            Spacer(Modifier.size(8.dp))
            Text(
                "Эти данные показываются на экране SOS и помогают врачу скорой.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            BloodTypeDropdown(value = bloodType, onChange = { bloodType = it })
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Аллергии") },
                placeholder = { Text("например: пенициллин, орехи") },
                minLines = 2,
            )
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                value = chronic,
                onValueChange = { chronic = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Хронические заболевания") },
                placeholder = { Text("например: гипертония, диабет 2 типа") },
                minLines = 2,
            )
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Доверенный контакт — имя") },
            )
            Spacer(Modifier.size(6.dp))
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it.filter { c -> c.isDigit() || c == '+' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Доверенный контакт — телефон") },
                placeholder = { Text("+7…") },
            )
        }

        SectionCard {
            Button(
                onClick = {
                    val problem = validateProfileInput(
                        name = name,
                        heightStr = height,
                        weightStr = weight,
                        birthStr = birth,
                    )
                    if (problem != null) {
                        saveError = problem
                        return@Button
                    }
                    vm.save(
                        name = name.trim(),
                        heightCm = height.toFloat(),
                        initialWeightKg = weight.toFloat(),
                        birthDate = parseBirthDate(birth)!!,
                        gender = gender,
                        bloodType = bloodType,
                        allergies = allergies.trim(),
                        chronicConditions = chronic.trim(),
                        emergencyContactName = contactName.trim(),
                        emergencyContactPhone = contactPhone.trim(),
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
            saveError?.let {
                Spacer(Modifier.size(8.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                )
            }
        }

        SectionCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBack)
                    .padding(vertical = 6.dp),
            ) {
                Text("← Назад", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.size(12.dp))
    }
}

@Composable
private fun BloodTypeDropdown(value: BloodType, onChange: (BloodType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Группа крови") },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            BloodType.entries.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.label) },
                    onClick = {
                        onChange(t)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun ageNoun(age: Int): String {
    val mod10 = age % 10
    val mod100 = age % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "год"
        mod10 in 2..4 && mod100 !in 12..14 -> "года"
        else -> "лет"
    }
}

private val birthFormatters: List<java.time.format.DateTimeFormatter> = listOf(
    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
)

private fun parseBirthDate(input: String): LocalDate? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null
    for (fmt in birthFormatters) {
        runCatching { return LocalDate.parse(trimmed, fmt) }
    }
    return null
}

private fun validateProfileInput(
    name: String,
    heightStr: String,
    weightStr: String,
    birthStr: String,
): String? {
    if (name.isBlank()) return "Укажи имя"
    val h = heightStr.toFloatOrNull()
    if (h == null || h !in 50f..250f) {
        return "Рост должен быть числом от 50 до 250 см"
    }
    val w = weightStr.toFloatOrNull()
    if (w == null || w !in 20f..400f) {
        return "Вес должен быть числом от 20 до 400 кг"
    }
    val date = parseBirthDate(birthStr)
        ?: return "Дата рождения должна быть в формате 1990-05-15 или 15.05.1990"
    val today = LocalDate.now()
    if (date.isAfter(today)) return "Дата рождения не может быть в будущем"
    if (date.isBefore(today.minusYears(120))) return "Проверь дату рождения"
    return null
}
