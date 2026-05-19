package com.example.health.ui.reminders

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.domain.model.Medication
import com.example.domain.model.MedicationCatalogItem
import com.example.domain.model.MedicationForm
import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: RemindersViewModel = koinViewModel(),
) {
    val medications by vm.medications.collectAsState()
    val reminders by vm.reminders.collectAsState()
    val intakes by vm.intakes.collectAsState()
    val error by vm.error.collectAsState()

    RequestNotificationPermission()

    var addReminderForMed by remember { mutableStateOf<Medication?>(null) }
    var editReminder by remember { mutableStateOf<Reminder?>(null) }
    var showGeneralDialog by remember { mutableStateOf(false) }
    val exactAlarmAllowed = rememberExactAlarmAllowed()

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Напоминания")

        if (!exactAlarmAllowed) {
            ExactAlarmBanner()
        }

        SectionCard {
            Text("Мои препараты", color = OnSurfaceMuted, fontSize = 12.sp)
            MedicationInput(
                onAdd = vm::addMedication,
                suggest = vm::suggest,
                sourceLabel = vm.catalogSourceLabel,
                sourceUpdatedOn = vm.catalogSourceUpdatedOn,
            )
            if (medications.isEmpty()) {
                Text("Пока ничего не добавлено", color = OnSurfaceMuted, fontSize = 13.sp)
            } else {
                medications.forEach { med ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${med.name} · ${med.dose}", fontWeight = FontWeight.SemiBold)
                            RegistrationBadge(med.registeredInKz)
                            med.instructions?.let { Text(it, color = OnSurfaceMuted, fontSize = 12.sp) }
                        }
                        TextButton(onClick = { addReminderForMed = med }) { Text("Напомнить") }
                        TextButton(onClick = { vm.removeMedication(med.id) }) { Text("Удалить") }
                    }
                }
            }
        }

        SectionCard {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Общие напоминания", color = OnSurfaceMuted, fontSize = 12.sp)
                TextButton(onClick = { showGeneralDialog = true }) { Text("+ Добавить") }
            }
            Text(
                "Давление, вес, самочувствие, вода — без привязки к препарату.",
                color = OnSurfaceMuted,
                fontSize = 11.sp,
            )
        }

        SectionCard {
            Text("Активные напоминания", color = OnSurfaceMuted, fontSize = 12.sp)
            if (reminders.isEmpty()) {
                Text("Нет настроенных напоминаний", color = OnSurfaceMuted, fontSize = 13.sp)
            } else {
                reminders.forEach { r ->
                    ReminderRow(
                        reminder = r,
                        medicationName = medications.firstOrNull { it.id == r.medicationId }?.name,
                        onToggle = { vm.toggle(r.id, it) },
                        onEdit = { editReminder = r },
                        onDelete = { vm.remove(r.id) },
                    )
                }
            }
        }

        if (intakes.isNotEmpty()) {
            SectionCard {
                Text("История приёмов", color = OnSurfaceMuted, fontSize = 12.sp)
                intakes.forEach { intake ->
                    IntakeRow(
                        intake = intake,
                        medicationName = medications
                            .firstOrNull { it.id == intake.medicationId }?.name
                            ?: "Препарат №${intake.medicationId}",
                    )
                }
            }
        }

        SectionCard {
            TextButton(onClick = onBack) { Text("← Назад") }
        }
    }

    addReminderForMed?.let { med ->
        AddReminderDialog(
            medication = med,
            onDismiss = { addReminderForMed = null },
            onConfirm = { time, days, dose ->
                vm.addMedicationReminder(med.id, time, days, dose)
                addReminderForMed = null
            },
        )
    }

    if (showGeneralDialog) {
        GeneralReminderDialog(
            onDismiss = { showGeneralDialog = false },
            onConfirm = { type, time, days ->
                vm.addGeneralReminder(type, time, days)
                showGeneralDialog = false
            },
        )
    }

    editReminder?.let { r ->
        EditReminderDialog(
            reminder = r,
            medicationName = medications.firstOrNull { it.id == r.medicationId }?.name,
            onDismiss = { editReminder = null },
            onConfirm = { time, days ->
                vm.updateReminderSchedule(r, time, days)
                editReminder = null
            },
        )
    }

    error?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            confirmButton = { TextButton(onClick = vm::clearError) { Text("OK") } },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    medicationName: String?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                medicationName?.let { "Принять $it" } ?: reminder.title,
                fontWeight = FontWeight.SemiBold,
            )
            val time = reminder.timeOfDay.toString().padStart(5, '0')
            val days = formatDays(reminder.daysOfWeek)
            Text("$time · $days", color = OnSurfaceMuted, fontSize = 12.sp)
        }
        Switch(checked = reminder.enabled, onCheckedChange = onToggle)
        TextButton(onClick = onEdit) { Text("✎") }
        TextButton(onClick = onDelete) { Text("✕") }
    }
}

@Composable
private fun IntakeRow(intake: MedicationIntakeRecord, medicationName: String) {
    val dateTime = LocalDateTime.ofInstant(intake.takenAt, ZoneId.systemDefault())
    val formatted = dateTime.format(INTAKE_DATE_FORMAT)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(medicationName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                listOfNotNull(formatted, intake.dose).joinToString(" · "),
                color = OnSurfaceMuted,
                fontSize = 12.sp,
            )
        }
        Text("✓", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
    }
}

private val INTAKE_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM HH:mm")

@Composable
private fun MedicationInput(
    onAdd: (String, String, MedicationForm, String?, Boolean) -> Unit,
    suggest: (String) -> List<MedicationCatalogItem>,
    sourceLabel: String,
    sourceUpdatedOn: String,
) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var form by remember { mutableStateOf(MedicationForm.TABLET) }
    var formExpanded by remember { mutableStateOf(false) }
    var instructions by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf<MedicationCatalogItem?>(null) }
    var suggestionsOpen by remember { mutableStateOf(false) }
    val suggestions = remember(name, picked) {
        // Если введённое имя совпадает с выбранным элементом — список не нужен.
        if (picked != null && name.equals(picked!!.tradeName, ignoreCase = true)) emptyList()
        else suggest(name)
    }

    Column(Modifier.padding(top = 8.dp)) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    picked = null
                    suggestionsOpen = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название препарата") },
                supportingText = {
                    Text(
                        "Источник: $sourceLabel · $sourceUpdatedOn",
                        color = OnSurfaceMuted,
                        fontSize = 11.sp,
                    )
                },
            )
            DropdownMenu(
                expanded = suggestionsOpen && suggestions.isNotEmpty(),
                onDismissRequest = { suggestionsOpen = false },
                modifier = Modifier.heightIn(max = 240.dp),
            ) {
                suggestions.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    item.tradeName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    "${item.inn} · ${formLabel(item.form)} · ${item.group}",
                                    color = OnSurfaceMuted,
                                    fontSize = 11.sp,
                                )
                                if (!item.registeredInKz) {
                                    Text(
                                        "⚠ Нет в реестре РК",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        },
                        onClick = {
                            name = item.tradeName
                            form = item.form
                            picked = item
                            suggestionsOpen = false
                        },
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            OutlinedTextField(
                dose, { dose = it },
                Modifier.weight(1f),
                label = { Text("Дозировка (например, 10 мг)") },
            )
            Box(Modifier.weight(1f).padding(start = 6.dp)) {
                OutlinedTextField(
                    value = formLabel(form),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Форма") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formExpanded = true },
                )
                DropdownMenu(
                    expanded = formExpanded,
                    onDismissRequest = { formExpanded = false },
                ) {
                    MedicationForm.entries.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(formLabel(f)) },
                            onClick = { form = f; formExpanded = false },
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            instructions, { instructions = it },
            Modifier.fillMaxWidth().padding(top = 6.dp),
            label = { Text("Инструкция (опц.)") },
        )
        Button(
            onClick = {
                if (name.isNotBlank() && dose.isNotBlank()) {
                    val registered = picked?.registeredInKz ?: false
                    onAdd(name, dose, form, instructions.ifBlank { null }, registered)
                    name = ""; dose = ""; instructions = ""; picked = null
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text("Добавить препарат") }
    }
}

@Composable
private fun RegistrationBadge(registeredInKz: Boolean) {
    val (text, bg, fg) = if (registeredInKz) {
        Triple(
            "✓ Зарегистрирован в РК",
            Color(0xFFE6F4EA),
            Color(0xFF1B5E20),
        )
    } else {
        Triple(
            "⚠ Нет в реестре РК",
            Color(0xFFFFF4E5),
            Color(0xFFB26500),
        )
    }
    Box(
        Modifier
            .padding(top = 2.dp)
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text, color = fg, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneralReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReminderType, LocalTime, Set<DayOfWeek>) -> Unit,
) {
    val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    var type by remember { mutableStateOf(ReminderType.BLOOD_PRESSURE) }
    var typeExpanded by remember { mutableStateOf(false) }
    var days by remember { mutableStateOf<Set<DayOfWeek>>(DayOfWeek.entries.toSet()) }
    var daysError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое напоминание") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = typeLabel(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип") },
                        modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        ReminderType.entries
                            .filter { it != ReminderType.MEDICATION }
                            .forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(typeLabel(t)) },
                                    onClick = { type = t; typeExpanded = false },
                                )
                            }
                    }
                }
                TimePicker(state = timeState)
                Text(
                    "Дни недели",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DayOfWeek.entries.forEach { d ->
                        FilterChip(
                            selected = d in days,
                            onClick = {
                                days = if (d in days) days - d else days + d
                                daysError = null
                            },
                            label = { Text(d.name.take(2)) },
                        )
                    }
                }
                daysError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (days.isEmpty()) {
                    daysError = "Выбери хотя бы один день"
                    return@TextButton
                }
                onConfirm(type, LocalTime.of(timeState.hour, timeState.minute), days)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReminderDialog(
    reminder: Reminder,
    medicationName: String?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, Set<DayOfWeek>) -> Unit,
) {
    val timeState = rememberTimePickerState(
        initialHour = reminder.timeOfDay.hour,
        initialMinute = reminder.timeOfDay.minute,
        is24Hour = true,
    )
    var days by remember { mutableStateOf(reminder.daysOfWeek.ifEmpty { DayOfWeek.entries.toSet() }) }
    var daysError by remember { mutableStateOf<String?>(null) }
    val titleText = medicationName?.let { "Принять $it" } ?: reminder.title

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить · $titleText") },
        text = {
            Column {
                TimePicker(state = timeState)
                Text(
                    "Дни недели",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DayOfWeek.entries.forEach { d ->
                        FilterChip(
                            selected = d in days,
                            onClick = {
                                days = if (d in days) days - d else days + d
                                daysError = null
                            },
                            label = { Text(d.name.take(2)) },
                        )
                    }
                }
                daysError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (days.isEmpty()) {
                    daysError = "Выбери хотя бы один день"
                    return@TextButton
                }
                onConfirm(LocalTime.of(timeState.hour, timeState.minute), days)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun typeLabel(type: ReminderType): String = when (type) {
    ReminderType.MEDICATION -> "Лекарство"
    ReminderType.BLOOD_PRESSURE -> "Давление"
    ReminderType.WEIGHT -> "Вес"
    ReminderType.FEELING -> "Самочувствие"
    ReminderType.WATER -> "Вода"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, Set<DayOfWeek>, String?) -> Unit,
) {
    val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    var doseOverride by remember { mutableStateOf("") }
    var days by remember { mutableStateOf<Set<DayOfWeek>>(DayOfWeek.entries.toSet()) }
    var daysError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Напоминание · ${medication.name}") },
        text = {
            Column {
                TimePicker(state = timeState)
                OutlinedTextField(
                    doseOverride, { doseOverride = it },
                    label = { Text("Доза на приём (опц.)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
                Text("Дни недели", color = OnSurfaceMuted, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DayOfWeek.entries.forEach { d ->
                        FilterChip(
                            selected = d in days,
                            onClick = {
                                days = if (d in days) days - d else days + d
                                daysError = null
                            },
                            label = { Text(d.name.take(2)) },
                        )
                    }
                }
                daysError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (days.isEmpty()) {
                    daysError = "Выбери хотя бы один день"
                    return@TextButton
                }
                val picked = LocalTime.of(timeState.hour, timeState.minute)
                onConfirm(picked, days, doseOverride.ifBlank { null })
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun rememberExactAlarmAllowed(): Boolean {
    // До Android 12 точные алармы выдаются по разрешению из манифеста — всегда true.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val alarmManager = remember(context) {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    var allowed by remember { mutableStateOf(alarmManager.canScheduleExactAlarms()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = alarmManager.canScheduleExactAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return allowed
}

@Composable
private fun ExactAlarmBanner() {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFFFF4E5), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(
                "Точные напоминания отключены",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFB26500),
            )
            Text(
                "Без этого разрешения Android может задерживать срабатывание в режиме энергосбережения. " +
                    "Включи в системных настройках для надёжности приёма лекарств.",
                color = OnSurfaceMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            TextButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching { context.startActivity(intent) }
                    }
                },
                modifier = Modifier.padding(top = 4.dp),
            ) { Text("Открыть настройки") }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* tolerate denial — пользователь может включить позже из системы */ }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

private fun formLabel(form: MedicationForm): String = when (form) {
    MedicationForm.TABLET -> "Таблетка"
    MedicationForm.CAPSULE -> "Капсула"
    MedicationForm.DROPS -> "Капли"
    MedicationForm.INJECTION -> "Инъекция"
    MedicationForm.OINTMENT -> "Мазь"
    MedicationForm.OTHER -> "Другое"
}

private fun formatDays(days: Set<DayOfWeek>): String {
    if (days.size == 7) return "Каждый день"
    val ordered = DayOfWeek.entries.filter { it in days }
    return ordered.joinToString(", ") { it.name.take(2) }
}
