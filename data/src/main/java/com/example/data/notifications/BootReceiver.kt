package com.example.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.domain.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * После перезагрузки устройства Android сбрасывает все алармы.
 * Этот receiver проходит по всем enabled-напоминаниям пользователя и переустанавливает их.
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderRepository: ReminderRepository by inject()
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderRepository.getEnabledForUser(USER_ID).forEach { scheduler.schedule(it) }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val USER_ID = "local-user"
    }
}
