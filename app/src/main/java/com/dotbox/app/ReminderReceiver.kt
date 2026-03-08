package com.dotbox.app

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotbox.app.data.preferences.AppPreferences

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_COUNTDOWN_ALARM -> {
                val eventName = intent.getStringExtra(EXTRA_EVENT_NAME) ?: "Countdown"
                sendNotification(
                    context,
                    DotBoxApplication.CHANNEL_COUNTDOWN,
                    eventName,
                    "Your countdown has reached its target!",
                    NOTIFICATION_ID_COUNTDOWN_BASE + (intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)),
                )
            }
            ACTION_WATER_REMINDER -> {
                sendNotification(
                    context,
                    DotBoxApplication.CHANNEL_WATER_INTAKE,
                    "Time to Drink Water",
                    "Stay hydrated! Have a glass of water.",
                    NOTIFICATION_ID_WATER,
                )
                // Reschedule next reminder
                val prefs = AppPreferences.get(context)
                val enabled = prefs.getBoolean(AppPreferences.KEY_WATER_REMINDER_ENABLED, false)
                if (enabled) {
                    val intervalMinutes = prefs.getInt(AppPreferences.KEY_WATER_REMINDER_INTERVAL, 60)
                    scheduleWaterReminder(context, intervalMinutes)
                }
            }
        }
    }

    private fun sendNotification(
        context: Context,
        channel: String,
        title: String,
        body: String,
        id: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, tapIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val ACTION_COUNTDOWN_ALARM = "com.dotbox.app.COUNTDOWN_ALARM"
        const val ACTION_WATER_REMINDER = "com.dotbox.app.WATER_REMINDER"
        const val EXTRA_EVENT_NAME = "event_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_ID_COUNTDOWN_BASE = 2000
        const val NOTIFICATION_ID_WATER = 3000

        fun scheduleCountdownAlarm(
            context: Context,
            countdownId: String,
            eventName: String,
            targetMillis: Long,
        ) {
            if (targetMillis <= System.currentTimeMillis()) return

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_COUNTDOWN_ALARM
                putExtra(EXTRA_EVENT_NAME, eventName)
                putExtra(EXTRA_NOTIFICATION_ID, countdownId.hashCode())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                countdownId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent,
                )
            } catch (_: SecurityException) {
                // Fall back to inexact alarm if exact alarm permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent,
                )
            }
        }

        fun cancelCountdownAlarm(context: Context, countdownId: String) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_COUNTDOWN_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                countdownId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            )
            if (pendingIntent != null) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            }
        }

        fun scheduleWaterReminder(context: Context, intervalMinutes: Int) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_WATER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_WATER,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAtMillis = System.currentTimeMillis() + intervalMinutes * 60 * 1000L
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }

        fun cancelWaterReminder(context: Context) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_WATER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_WATER,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            )
            if (pendingIntent != null) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
