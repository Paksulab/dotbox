package com.dotbox.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Single source of truth for SharedPreferences keys and access.
 * Eliminates scattered raw strings across the codebase.
 */
object AppPreferences {

    const val PREFS_NAME = "dotbox_settings"

    // Appearance
    const val KEY_THEME = "theme_mode"
    const val KEY_GRID_COLUMNS = "grid_columns"

    // Behaviour
    const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    const val KEY_ANIMATIONS = "animations_enabled"
    const val KEY_DEFAULT_CATEGORY = "default_category"
    const val KEY_TWO_PANE = "two_pane_layout"

    // Onboarding
    const val KEY_ONBOARDING = "has_seen_onboarding"

    // Widget
    const val KEY_WIDGET_FAVORITES = "widget_favorites"

    // Pomodoro
    const val KEY_POMODORO_AUTO_START = "pomodoro_auto_start"

    // Countdown
    const val KEY_SAVED_COUNTDOWNS = "saved_countdowns"

    // Workout Timer
    const val KEY_WORKOUT_NOTIFICATIONS = "workout_notifications"

    // Water Intake Reminders
    const val KEY_WATER_REMINDER_ENABLED = "water_reminder_enabled"
    const val KEY_WATER_REMINDER_INTERVAL = "water_reminder_interval"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
