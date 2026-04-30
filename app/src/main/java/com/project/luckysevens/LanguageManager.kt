package com.project.luckysevens

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageManager {

    private const val PREF_NAME = "settings"
    private const val KEY_LANGUAGE = "language"

    private fun getDefaultLanguage(): String {
        val deviceLanguage = Locale.getDefault().language
        return if (deviceLanguage == "en") {
            "en"
        } else {
            "es"
        }
    }

    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun loadLanguage(context: Context): Context {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, null) ?: getDefaultLanguage()
        return updateContext(context, language)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null) ?: getDefaultLanguage()
    }

    private fun updateContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}