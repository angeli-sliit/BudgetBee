package com.example.budgetbee

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.budgetbee.utils.NotificationHelper
import com.example.budgetbee.utils.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workManagerHelper: WorkManagerHelper

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeWorkers()
    }

    private fun initializeWorkers() {
        workManagerHelper.scheduleDailyReminder()
        workManagerHelper.scheduleBudgetAlert()
        workManagerHelper.scheduleDataBackup()
    }
}