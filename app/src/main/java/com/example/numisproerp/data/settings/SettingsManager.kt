package com.numisproerp.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Тема додатку. Користувач обирає в Налаштуваннях.
 *
 * - [DEFAULT] — класична iOS-Blue палітра, з якою додаток був до цього.
 * - [OLEG_SMILE] — фірмова чорно-золота тема із емблемою лева.
 */
enum class AppTheme {
    DEFAULT,
    OLEG_SMILE;

    companion object {
        fun fromKey(key: String?): AppTheme = when (key) {
            OLEG_SMILE.name -> OLEG_SMILE
            else -> DEFAULT
        }
    }
}

/**
 * Простий менеджер користувацьких налаштувань на основі SharedPreferences.
 * Тримає Compose-friendly реактивний стан через [MutableState], щоб тема
 * перемикалася миттєво без рестарту Activity.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _theme: MutableState<AppTheme> =
        mutableStateOf(AppTheme.fromKey(prefs.getString(KEY_THEME, null)))

    val themeState: MutableState<AppTheme>
        get() = _theme

    var theme: AppTheme
        get() = _theme.value
        set(value) {
            _theme.value = value
            prefs.edit().putString(KEY_THEME, value.name).apply()
        }

    companion object {
        private const val PREFS_NAME = "numispro_settings"
        private const val KEY_THEME = "app_theme"
    }
}
