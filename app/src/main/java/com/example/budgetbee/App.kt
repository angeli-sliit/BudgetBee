package com.example.budgetbee

import android.app.Application
import com.example.budgetbee.utils.NotificationHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}