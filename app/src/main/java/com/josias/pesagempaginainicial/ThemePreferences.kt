package com.josias.pesagempaginainicial

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "theme_base_name"

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentTheme(context: Context): AppTheme {
        val name = getPrefs(context).getString(KEY_THEME, null)
        return AppTheme.fromPrefName(name)
    }

    fun setCurrentTheme(context: Context, theme: AppTheme) {
        getPrefs(context).edit().putString(KEY_THEME, theme.prefName).apply()
    }
}