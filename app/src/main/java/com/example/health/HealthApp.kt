package com.example.health

import android.app.Application
import com.example.data.di.dataModule
import com.example.data.di.domainModule
import com.example.health.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HealthApp)
            modules(dataModule, domainModule, appModule)
        }
    }
}
