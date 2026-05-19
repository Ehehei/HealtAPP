package com.example.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.domain.model.Medication
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderType

class ReminderNotifier(private val context: Context) {

    init { ensureChannel() }

    fun show(reminder: Reminder, medication: Medication?) {
        val title = when (reminder.type) {
            ReminderType.MEDICATION -> medication?.name?.let { "Время принять: $it" } ?: reminder.title
            ReminderType.BLOOD_PRESSURE -> "Измерь давление"
            ReminderType.WEIGHT -> "Измерь вес"
            ReminderType.FEELING -> "Отметь самочувствие"
            ReminderType.WATER -> "Пора выпить воды"
        }
        val text = buildText(reminder, medication)

        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val contentPi = openAppIntent?.let {
            PendingIntent.getActivity(
                context,
                reminder.id.toInt(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 1).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val takenPi = if (reminder.type == ReminderType.MEDICATION && reminder.medicationId != null) {
            val takenIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_TAKEN
                putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            }
            PendingIntent.getBroadcast(
                context,
                (reminder.id * 10 + 2).toInt(),
                takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        } else null

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .also { b -> takenPi?.let { b.addAction(0, "Принял", it) } }
            .addAction(0, "Отложить 15 мин", snoozePi)

        contentPi?.let { builder.setContentIntent(it) }

        runCatching {
            NotificationManagerCompat.from(context).notify(reminder.id.toInt(), builder.build())
        }
    }

    fun cancel(reminderId: Long) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(reminderId.toInt())
        }
    }

    private fun buildText(reminder: Reminder, medication: Medication?): String {
        if (reminder.type == ReminderType.MEDICATION) {
            val dose = reminder.doseOverride ?: medication?.dose
            return listOfNotNull(dose, medication?.instructions).joinToString(" · ")
                .ifBlank { reminder.title }
        }
        return reminder.title
    }

    private fun ensureChannel() {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о здоровье",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Лекарства, измерения давления и веса, самочувствие"
        }
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "health_reminders"
    }
}
