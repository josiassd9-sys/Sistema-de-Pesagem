package com.josias.pesagempaginainicial

import android.content.Context
import android.graphics.Color
import android.util.TypedValue

object ThemeOverrides {

    private const val PREFS_NAME = "theme_overrides"

    // Chaves de overrides (tokens principais)
    const val KEY_BACKGROUND = "override_background"
    const val KEY_FOREGROUND = "override_foreground"
    const val KEY_CARD = "override_card"
    const val KEY_CARD_FOREGROUND = "override_card_foreground"
    const val KEY_PRIMARY = "override_primary"
    const val KEY_PRIMARY_FOREGROUND = "override_primary_foreground"
    const val KEY_ACCENT_PRICE = "override_accent_price"
    const val KEY_SETTINGS_BUTTON_BG = "override_settings_button_bg"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setOverrideColor(context: Context, key: String, hex: String?) {
        val editor = prefs(context).edit()
        if (hex.isNullOrBlank()) {
            editor.remove(key)
        } else {
            // Garante que tenha '#'
            val normalized = if (hex.startsWith("#")) hex else "#$hex"
            editor.putString(key, normalized)
        }
        editor.apply()
    }

    fun getOverrideHex(context: Context, key: String): String? =
        prefs(context).getString(key, null)

    /**
     * Resolve uma cor considerando override + fallback do tema base.
     *
     * @param key chave de override (por ex. KEY_BACKGROUND)
     * @param fallbackAttr atributo de tema (por ex. R.attr.colorSurface)
     */
    fun resolveColor(context: Context, key: String, fallbackAttr: Int): Int {
        val hex = getOverrideHex(context, key)
        if (!hex.isNullOrBlank()) {
            return try {
                Color.parseColor(hex)
            } catch (_: IllegalArgumentException) {
                resolveThemeAttrColor(context, fallbackAttr)
            }
        }
        return resolveThemeAttrColor(context, fallbackAttr)
    }

    private fun resolveThemeAttrColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        val found = theme.resolveAttribute(attr, typedValue, true)
        return if (found) typedValue.data else Color.BLACK
    }
}