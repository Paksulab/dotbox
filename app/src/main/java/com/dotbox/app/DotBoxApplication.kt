package com.dotbox.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.dotbox.app.data.local.AppDatabase
import com.dotbox.app.data.repository.ToolsRepository

class DotBoxApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val toolsRepository by lazy { ToolsRepository(database.favoriteDao(), this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val pomodoroChannel = NotificationChannel(
            CHANNEL_POMODORO,
            "Pomodoro Timer",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications when a Pomodoro session finishes"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(pomodoroChannel)
    }

    companion object {
        const val CHANNEL_POMODORO = "pomodoro_timer"
    }
}
