package com.example.health.ui.sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.model.UserProfile
import com.example.health.ui.components.ScreenTitle
import com.example.health.ui.components.SectionCard
import com.example.health.ui.theme.OnSurfaceMuted
import org.koin.androidx.compose.koinViewModel

private val DangerRed = Color(0xFFC62828)

@Composable
fun SosScreen(modifier: Modifier = Modifier, vm: SosViewModel = koinViewModel()) {
    val profile by vm.profile.collectAsState()
    val context = LocalContext.current

    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val phone = profile?.emergencyContactPhone.orEmpty()
        val smsGranted = results[Manifest.permission.SEND_SMS] == true
        val locGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (!smsGranted) {
            Toast.makeText(context, "Без разрешения SMS отправить нельзя", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        sendSosSms(context, phone, useLocation = locGranted)
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("SOS")

        SectionCard {
            Text(
                "Экстренная связь",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { dialEmergency(context, "103") },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) { Text("Вызвать скорую 103", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))

            val phone = profile?.emergencyContactPhone.orEmpty()
            val phoneFilled = phone.isNotBlank()
            OutlinedButton(
                onClick = {
                    if (!phoneFilled) {
                        Toast.makeText(
                            context,
                            "Сначала укажи доверенный контакт в Профиле",
                            Toast.LENGTH_LONG,
                        ).show()
                        return@OutlinedButton
                    }
                    val needSms = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.SEND_SMS,
                    ) != PackageManager.PERMISSION_GRANTED
                    val needLoc = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                    if (needSms || needLoc) {
                        smsLauncher.launch(
                            arrayOf(
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    } else {
                        sendSosSms(context, phone, useLocation = true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    if (phoneFilled) "Отправить SMS близкому с координатами"
                    else "Указать доверенный контакт",
                )
            }
        }

        SectionCard {
            Text("Медкарта", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            profile?.let { MedicalCardView(it) } ?: Text(
                "Заполни медкарту в профиле — её увидит тот, кто окажет помощь.",
                color = OnSurfaceMuted,
            )
        }

        SectionCard {
            Text("Доверенный контакт", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            val name = profile?.emergencyContactName.orEmpty()
            val phone = profile?.emergencyContactPhone.orEmpty()
            if (name.isBlank() && phone.isBlank()) {
                Text(
                    "Не указан. Добавь в Профиле — на этот номер уйдёт SMS.",
                    color = OnSurfaceMuted,
                )
            } else {
                if (name.isNotBlank()) Text(name)
                if (phone.isNotBlank()) {
                    Text(phone, color = OnSurfaceMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { dialEmergency(context, phone) }) {
                            Text("Позвонить")
                        }
                    }
                }
            }
        }

        SectionCard {
            Text(
                "Звонок 103 открывает системный диалер с уже набранным номером — нажми зелёную кнопку, чтобы соединиться. " +
                    "SMS отправляется напрямую через SmsManager, после согласия на разрешения.",
                fontSize = 12.sp,
                color = OnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun MedicalCardView(profile: UserProfile) {
    Text("Имя: ${profile.name.ifBlank { "—" }}", color = OnSurfaceMuted)
    Text("Группа крови: ${profile.bloodType.label}", color = OnSurfaceMuted)
    Text(
        "Аллергии: ${profile.allergies.ifBlank { "—" }}",
        color = OnSurfaceMuted,
    )
    Text(
        "Хронические: ${profile.chronicConditions.ifBlank { "—" }}",
        color = OnSurfaceMuted,
    )
}

private fun dialEmergency(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "Не удалось открыть диалер", Toast.LENGTH_SHORT).show()
        }
}

private fun sendSosSms(context: Context, phone: String, useLocation: Boolean) {
    val location: Location? = if (useLocation) tryGetLastKnownLocation(context) else null
    val text = buildString {
        append("SOS! Срочно нужна помощь.")
        if (location != null) {
            append(" Я здесь: https://maps.google.com/?q=")
            append(location.latitude)
            append(',')
            append(location.longitude)
        } else {
            append(" Координаты недоступны.")
        }
    }
    val sms: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
    }
    runCatching {
        sms.sendTextMessage(phone, null, text, null, null)
        Toast.makeText(context, "SOS-SMS отправлено", Toast.LENGTH_LONG).show()
    }.onFailure {
        Toast.makeText(context, "Не удалось отправить SMS: ${it.message}", Toast.LENGTH_LONG).show()
    }
}

private fun tryGetLastKnownLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return runCatching {
        val gps = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
        val net = if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null
        listOfNotNull(gps, net).maxByOrNull { it.time }
    }.getOrNull()
}
