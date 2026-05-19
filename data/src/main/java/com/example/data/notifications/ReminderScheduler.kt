package com.example.data.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.model.Reminder
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Планирует точные алармы на ближайшее срабатывание Reminder через AlarmManager.
 * После срабатывания [ReminderReceiver] перепланирует следующее.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        val nextAt = nextTriggerEpochMillis(reminder) ?: return
        val pi = pendingIntentFor(reminder.id, create = true) ?: return
        setExact(nextAt, pi)
    }

    fun cancel(reminderId: Long) {
        pendingIntentFor(reminderId, create = false)?.let { alarmManager.cancel(it) }
    }

    fun snooze(reminderId: Long, minutes: Int) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val pi = pendingIntentFor(reminderId, create = true) ?: return
        setExact(triggerAt, pi)
    }

    private fun setExact(triggerAtMillis: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun pendingIntentFor(reminderId: Long, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or
            if (create) 0 else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, reminderId.toInt(), intent, flags)
    }

    private fun nextTriggerEpochMillis(reminder: Reminder): Long? {
        if (reminder.daysOfWeek.isEmpty()) return null
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val candidates = reminder.daysOfWeek.map { dow ->
            var candidate = now
                .with(reminder.timeOfDay)
                .truncatedTo(ChronoUnit.MINUTES)
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(dow))
            if (!candidate.isAfter(now)) {
                candidate = candidate.with(java.time.temporal.TemporalAdjusters.next(dow))
            }
            candidate
        }
        return candidates.min().toInstant().toEpochMilli()
    }
}
