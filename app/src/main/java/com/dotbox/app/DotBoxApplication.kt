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
        val channels = listOf(
            NotificationChannel(
                CHANNEL_POMODORO,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications when a Pomodoro session finishes"
            },
            NotificationChannel(
                CHANNEL_COUNTDOWN,
                "Countdown Timer",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications when a countdown event arrives"
            },
            NotificationChannel(
                CHANNEL_WORKOUT,
                "Workout Timer",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Workout phase transitions and completion"
            },
            NotificationChannel(
                CHANNEL_STOPWATCH,
                "Stopwatch Timer",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notification when a timer countdown reaches zero"
            },
            NotificationChannel(
                CHANNEL_WATER_INTAKE,
                "Water Intake Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Periodic reminders to drink water"
            },
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(channels)
    }

    companion object {
        const val CHANNEL_POMODORO = "pomodoro_timer"
        const val CHANNEL_COUNTDOWN = "countdown_timer"
        const val CHANNEL_WORKOUT = "workout_timer"
        const val CHANNEL_STOPWATCH = "stopwatch_timer"
        const val CHANNEL_WATER_INTAKE = "water_intake"
    }
}
