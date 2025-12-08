package com.josias.pesagempaginainicial

import android.app.Activity

object ThemeManager {
    fun applyTheme(activity: Activity) {
        val theme = ThemePreferences.getCurrentTheme(activity)
        activity.setTheme(theme.styleRes)
    }
}