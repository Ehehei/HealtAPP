package com.example.health.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BloodType
import com.example.domain.model.Gender
import com.example.health.ui.catalog.MedicationCatalogScreen
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.reminders.RemindersScreen
import com.example.health.ui.screenings.ScreeningsScreen
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

enum class ProfileTarget { REMINDERS, SCREENINGS }

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

    val profile by vm.profile.collectAsState()
    val bmi by vm.bmi.collectAsState()

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf("") } // yyyy-MM-dd
    var gender by remember { mutableStateOf(Gender.MALE) }
    var bloodType by remember { mutableStateOf(BloodType.UNKNOWN) }
    var allergies by remember { mutableStateOf("") }
    var chronic by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

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
        ScreenTitle("Профиль")

        SectionCard {
            profile?.let {
                Text(it.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Возраст: ${vm.age() ?: "—"}", color = OnSurfaceMuted)
                bmi?.let { v -> Text("ИМТ: ${"%.1f".format(v)}", color = OnSurfaceMuted) }
                Text(
                    "Аккаунт локальный. Фото никогда не покидают устройство.",
                    fontSize = 11.sp,
                    color = OnSurfaceMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            } ?: Text("Заполни данные ниже", color = OnSurfaceMuted)
        }

        SectionCard {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Имя") })
            OutlinedTextField(
                height, { height = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Рост, см") },
            )
            OutlinedTextField(
                weight, { weight = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Стартовый вес, кг") },
            )
            OutlinedTextField(
                birth, { birth = it },
                Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Дата рождения yyyy-MM-dd") },
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g.name) },
                    )
                }
            }
        }

        SectionCard {
            Text("Медкарта (SOS)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Эти данные показываются на экране SOS и помогают врачу скорой.",
                fontSize = 11.sp,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            BloodTypeDropdown(
                value = bloodType,
                onChange = { bloodType = it },
            )
            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Аллергии") },
                placeholder = { Text("например: пенициллин, орехи") },
                minLines = 2,
            )
            OutlinedTextField(
                value = chronic,
                onValueChange = { chronic = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Хронические заболевания") },
                placeholder = { Text("например: гипертония, диабет 2 типа") },
                minLines = 2,
            )
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Доверенный контакт — имя") },
            )
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it.filter { c -> c.isDigit() || c == '+' } },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                label = { Text("Доверенный контакт — телефон") },
                placeholder = { Text("+7…") },
            )
        }

        SectionCard {
            Button(
                onClick = {
                    val h = height.toFloatOrNull() ?: return@Button
                    val w = weight.toFloatOrNull() ?: return@Button
                    val d = runCatching { LocalDate.parse(birth) }.getOrNull() ?: return@Button
                    vm.save(
                        name = name,
                        heightCm = h,
                        initialWeightKg = w,
                        birthDate = d,
                        gender = gender,
                        bloodType = bloodType,
                        allergies = allergies.trim(),
                        chronicConditions = chronic.trim(),
                        emergencyContactName = contactName.trim(),
                        emergencyContactPhone = contactPhone.trim(),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сохранить") }
        }

        SectionCard {
            Button(
                onClick = { showReminders = true },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Мои напоминания") }
            Button(
                onClick = { showScreenings = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Скрининги по программе РК") }
            Button(
                onClick = { showCatalog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Справочник лекарств РК") }
        }
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
