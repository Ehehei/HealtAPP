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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.model.Medication
import com.example.domain.model.MedicationCatalogItem
import com.example.domain.model.MedicationForm
import com.example.domain.model.MedicationIntakeRecord
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType
import com.example.health.ui.components.Divider
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.LockBadge
import com.example.health.ui.components.SectionCard
import com.example.health.ui.components.StatusPill
import com.example.health.ui.theme.AmberSoftBg
import com.example.health.ui.theme.AmberSoftText
import com.example.health.ui.theme.BlueSoftBg
import com.example.health.ui.theme.BlueSoftText
import com.example.health.ui.theme.BrandGreen
import com.example.health.ui.theme.BrandGreenSoftBg
import com.example.health.ui.theme.BrandGreenSoftBorder
import com.example.health.ui.theme.BrandGreenText
import com.example.health.ui.theme.SosBannerBg
import com.example.health.ui.theme.SosBannerBorder
import com.example.health.ui.theme.SosTextDeep
import com.example.health.ui.theme.SosTextStrong
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.SurfaceField
import com.example.health.ui.theme.SurfaceMuted
import com.example.health.ui.theme.TextDisabled
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale: Locale = Locale.forLanguageTag("ru")

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
    var showAddMedication by remember { mutableStateOf(false) }
    val exactAlarmAllowed = rememberExactAlarmAllowed()

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHeader(title = "Напоминания", onBack = onBack)

        if (!exactAlarmAllowed) {
            ExactAlarmBanner()
        }

        // Мои препараты
        SectionCard {
            SectionHeader(
                icon = Icons.Filled.Medication,
                label = "Мои препараты",
                actionLabel = if (showAddMedication) "Скрыть" else "+ Добавить",
                onAction = { showAddMedication = !showAddMedication },
            )
            Spacer(Modifier.height(8.dp))

            if (medications.isEmpty() && !showAddMedication) {
                EmptyHint("Пока ничего не добавлено")
            } else {
                medications.forEachIndexed { index, med ->
                    if (index > 0) Divider()
                    MedicationRow(
                        med = med,
                        onAddReminder = { addReminderForMed = med },
                        onRemove = { vm.removeMedication(med.id) },
                    )
                }
            }

            if (showAddMedication) {
                if (medications.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Divider()
                }
                Spacer(Modifier.height(12.dp))
                MedicationInput(
                    onAdd = { name, dose, form, instructions, registered ->
                        vm.addMedication(name, dose, form, instructions, registered)
                        showAddMedication = false
                    },
                    suggest = vm::suggest,
                    sourceLabel = vm.catalogSourceLabel,
                    sourceUpdatedOn = vm.catalogSourceUpdatedOn,
                )
            }
        }

        // Общие напоминания (быстрая кнопка)
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Column(Modifier.weight(1f)) {
                    LabelXsText("Общие напоминания")
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Давление, вес, самочувствие, вода — без привязки к препарату",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.size(8.dp))
                IconButton(
                    icon = Icons.Filled.Add,
                    onClick = { showGeneralDialog = true },
                )
            }
        }

        // Активные напоминания
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                LabelXsText("Активные напоминания · ${reminders.size}")
            }
            Spacer(Modifier.height(10.dp))
            if (reminders.isEmpty()) {
                EmptyHint("Нет настроенных напоминаний")
            } else {
                reminders.forEachIndexed { index, r ->
                    if (index > 0) Divider()
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

        // История приёмов
        if (intakes.isNotEmpty()) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    LabelXsText("История приёмов")
                }
                Spacer(Modifier.height(10.dp))
                intakes.take(20).forEachIndexed { index, intake ->
                    if (index > 0) Divider()
                    IntakeRow(
                        intake = intake,
                        medicationName = medications
                            .firstOrNull { it.id == intake.medicationId }?.name
                            ?: "Препарат №${intake.medicationId}",
                    )
                }
            }
        }

        LockBadge("Напоминания работают локально через AlarmManager")
        Spacer(Modifier.height(12.dp))
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
private fun MedicationRow(
    med: Medication,
    onAddReminder: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                med.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary,
            )
            Text(
                med.dose,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(6.dp))
            RegistrationBadge(med.registeredInKz)
            med.instructions?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            IconButton(icon = Icons.Filled.Alarm, onClick = onAddReminder)
            Spacer(Modifier.height(4.dp))
            IconButton(icon = Icons.Filled.Delete, onClick = onRemove, tint = TextDisabled)
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                medicationName?.let { "Принять $it" } ?: reminder.title,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatTime(reminder.timeOfDay),
                    color = if (reminder.enabled) BrandGreenText else TextDisabled,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                Text(
                    " · " + formatDays(reminder.daysOfWeek),
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Switch(
            checked = reminder.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = BrandGreen,
                checkedThumbColor = SurfaceCard,
            ),
        )
        Spacer(Modifier.size(4.dp))
        IconButton(icon = Icons.Filled.Edit, onClick = onEdit)
        IconButton(icon = Icons.Filled.Delete, onClick = onDelete, tint = TextDisabled)
    }
}

@Composable
private fun IntakeRow(intake: MedicationIntakeRecord, medicationName: String) {
    val dateTime = LocalDateTime.ofInstant(intake.takenAt, ZoneId.systemDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(BrandGreenSoftBg, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", color = BrandGreenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(medicationName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Text(
                listOfNotNull(dateTime.format(INTAKE_DATE_FORMAT), intake.dose).joinToString(" · "),
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

private val INTAKE_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM HH:mm")

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        LabelXsText(label, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = BrandGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = TextDisabled, fontSize = 13.sp)
    }
}

@Composable
private fun IconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = TextSecondary,
) {
    Box(
        Modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

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
        if (picked != null && name.equals(picked!!.tradeName, ignoreCase = true)) emptyList()
        else suggest(name)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = SurfaceField,
        focusedContainerColor = SurfaceField,
    )

    Column {
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
                placeholder = { Text("например: парацетамол", color = TextSecondary) },
                supportingText = {
                    Text(
                        "Источник: $sourceLabel · $sourceUpdatedOn",
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )
                },
                singleLine = true,
                colors = fieldColors,
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
                                    color = TextPrimary,
                                )
                                Text(
                                    "${item.inn} · ${formLabel(item.form)} · ${item.group}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                )
                                if (!item.registeredInKz) {
                                    Text(
                                        "⚠ Нет в реестре РК",
                                        color = AmberSoftText,
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
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                modifier = Modifier.weight(1f),
                label = { Text("Дозировка") },
                placeholder = { Text("10 мг", color = TextSecondary) },
                singleLine = true,
                colors = fieldColors,
            )
            Spacer(Modifier.size(8.dp))
            Box(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formLabel(form),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Форма") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formExpanded = true },
                    colors = fieldColors,
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
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Инструкция (опц.)") },
            placeholder = { Text("после еды, запивая водой", color = TextSecondary) },
            colors = fieldColors,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(BrandGreen, RoundedCornerShape(12.dp))
                .clickable(enabled = name.isNotBlank() && dose.isNotBlank()) {
                    val registered = picked?.registeredInKz ?: false
                    onAdd(name, dose, form, instructions.ifBlank { null }, registered)
                    name = ""; dose = ""; instructions = ""; picked = null
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("Добавить препарат", color = SurfaceCard, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RegistrationBadge(registeredInKz: Boolean) {
    if (registeredInKz) {
        StatusPill(
            text = "✓ Зарегистрирован в РК",
            background = BrandGreenSoftBg,
            contentColor = BrandGreenText,
        )
    } else {
        StatusPill(
            text = "⚠ Нет в реестре РК",
            background = AmberSoftBg,
            contentColor = AmberSoftText,
        )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = true },
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
                Spacer(Modifier.height(8.dp))
                TimePicker(state = timeState)
                Spacer(Modifier.height(8.dp))
                DaysPicker(days = days, onChange = { days = it; daysError = null })
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
                Spacer(Modifier.height(8.dp))
                DaysPicker(days = days, onChange = { days = it; daysError = null })
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
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = doseOverride,
                    onValueChange = { doseOverride = it },
                    label = { Text("Доза на приём (опц.)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                DaysPicker(days = days, onChange = { days = it; daysError = null })
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
private fun DaysPicker(days: Set<DayOfWeek>, onChange: (Set<DayOfWeek>) -> Unit) {
    Column {
        LabelXsText("Дни недели")
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DayOfWeek.entries.forEach { d ->
                FilterChip(
                    selected = d in days,
                    onClick = {
                        onChange(if (d in days) days - d else days + d)
                    },
                    label = {
                        Text(
                            d.getDisplayName(java.time.format.TextStyle.SHORT, ruLocale).take(2),
                            fontSize = 11.sp,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun rememberExactAlarmAllowed(): Boolean {
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(SosBannerBg, RoundedCornerShape(12.dp))
            .border(1.dp, SosBannerBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = SosTextStrong,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Точные напоминания отключены",
                    fontWeight = FontWeight.SemiBold,
                    color = SosTextDeep,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Без этого разрешения Android может задерживать срабатывание в режиме энергосбережения. " +
                    "Включи в системных настройках для надёжности приёма лекарств.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    }
                    .background(SosTextDeep, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Открыть настройки", color = SurfaceCard, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* tolerate denial */ }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

@Composable
private fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

private fun formLabel(form: MedicationForm): String = when (form) {
    MedicationForm.TABLET -> "Таблетка"
    MedicationForm.CAPSULE -> "Капсула"
    MedicationForm.DROPS -> "Капли"
    MedicationForm.INJECTION -> "Инъекция"
    MedicationForm.OINTMENT -> "Мазь"
    MedicationForm.OTHER -> "Другое"
}

private fun formatTime(time: LocalTime): String =
    "%02d:%02d".format(time.hour, time.minute)

private fun formatDays(days: Set<DayOfWeek>): String {
    if (days.size == 7) return "Каждый день"
    val ordered = DayOfWeek.entries.filter { it in days }
    return ordered.joinToString(", ") {
        it.getDisplayName(java.time.format.TextStyle.SHORT, ruLocale).take(2)
    }
}
