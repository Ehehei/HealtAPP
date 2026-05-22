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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.health.ui.components.LabelXsText
import com.example.health.ui.components.SectionCard
import com.example.health.ui.components.StatusPill
import com.example.health.ui.theme.SosBannerBg
import com.example.health.ui.theme.SosBannerBorder
import com.example.health.ui.theme.SosCardBg
import com.example.health.ui.theme.SosRed
import com.example.health.ui.theme.SosRedRing
import com.example.health.ui.theme.SosTextDeep
import com.example.health.ui.theme.SosTextStrong
import com.example.health.ui.theme.SurfaceCard
import com.example.health.ui.theme.TextPrimary
import com.example.health.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun SosScreen(modifier: Modifier = Modifier, vm: SosViewModel = koinViewModel()) {
    val profile by vm.profile.collectAsState()
    val age by vm.age.collectAsState()
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
            .verticalScroll(rememberScrollState()),
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .background(SosBannerBg, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Emergency,
                contentDescription = null,
                tint = SosTextStrong,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                "Экстренная помощь",
                color = SosTextDeep,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .padding(vertical = 8.dp)
                    .size(160.dp)
                    .border(width = 6.dp, color = SosRedRing, shape = CircleShape)
                    .padding(6.dp)
                    .background(SosRed, CircleShape)
                    .clickable { dialEmergency(context, "103") },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        tint = SurfaceCard,
                        modifier = Modifier.size(38.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        "Вызвать скорую",
                        color = SurfaceCard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("103", color = SosTextStrong, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(" · откроет набор номера", color = TextSecondary, fontSize = 13.sp)
        }

        val phone = profile?.emergencyContactPhone.orEmpty()
        val phoneFilled = phone.isNotBlank()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .height(50.dp)
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .border(1.dp, SosBannerBorder, RoundedCornerShape(12.dp))
                .clickable {
                    if (!phoneFilled) {
                        Toast.makeText(
                            context,
                            "Сначала укажи доверенный контакт в Профиле",
                            Toast.LENGTH_LONG,
                        ).show()
                        return@clickable
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
                            ),
                        )
                    } else {
                        sendSosSms(context, phone, useLocation = true)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = SosTextStrong,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                if (phoneFilled) "Отправить SMS с координатами"
                else "Указать доверенный контакт",
                color = SosTextStrong,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }

        SectionCard(background = SurfaceCard) {
            LabelXsText("Медкарта")
            Spacer(Modifier.size(6.dp))
            profile?.let { p ->
                Text(
                    text = listOfNotNull(
                        p.name.ifBlank { null },
                        age?.let { "$it ${ageNoun(it)}" },
                    ).joinToString(", "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Spacer(Modifier.size(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        text = p.bloodType.label,
                        background = SosBannerBg,
                        contentColor = SosTextDeep,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("группа крови", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.size(6.dp))
                Text(
                    "Аллергии: ${p.allergies.ifBlank { "—" }}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                Text(
                    "Хронические: ${p.chronicConditions.ifBlank { "—" }}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            } ?: Text(
                "Заполни медкарту в профиле — её увидит тот, кто окажет помощь.",
                color = TextSecondary,
            )
        }

        SectionCard(background = SurfaceCard) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LabelXsText("Контакт")
                    val name = profile?.emergencyContactName.orEmpty()
                    val ph = profile?.emergencyContactPhone.orEmpty()
                    Spacer(Modifier.size(4.dp))
                    if (name.isBlank() && ph.isBlank()) {
                        Text(
                            "Не указан. Добавь в Профиле.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                    } else {
                        if (name.isNotBlank()) {
                            Text(
                                name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                        }
                        if (ph.isNotBlank()) {
                            Text(ph, color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
                val ph = profile?.emergencyContactPhone.orEmpty()
                if (ph.isNotBlank()) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(SosRed, CircleShape)
                            .clickable { dialEmergency(context, ph) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Позвонить",
                            tint = SurfaceCard,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        SectionCard(background = SosCardBg) {
            Text(
                "Звонок 103 откроет системный диалер с уже набранным номером — нажми зелёную кнопку, чтобы соединиться. " +
                    "SMS отправляется напрямую через SmsManager после согласия на разрешения.",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.size(8.dp))
    }
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

private fun ageNoun(age: Int): String {
    val mod10 = age % 10
    val mod100 = age % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "год"
        mod10 in 2..4 && mod100 !in 12..14 -> "года"
        else -> "лет"
    }
}
