package com.example.health

/**
 * Локальная сессия. Аккаунт может быть привязан к Firebase в будущем,
 * но сейчас работаем с одним локальным пользователем — фото и метрики
 * никогда не уходят с устройства.
 */
object Session {
    const val USER_ID: String = "local-user"
}
