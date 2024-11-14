package com.vladrip.ifchat

import android.app.Application
import com.vladrip.ifchat.data.local.DatabaseModule
import com.vladrip.ifchat.data.network.ApiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*

class IFChat : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@IFChat)
            androidLogger()

            modules(
                defaultModule,
                DatabaseModule().module,
                ApiModule().module,
            )
        }
    }
}