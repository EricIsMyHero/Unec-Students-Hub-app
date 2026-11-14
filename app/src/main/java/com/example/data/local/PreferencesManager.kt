package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unec_prefs")

data class UserPrefsState(
    val lang: String = "az",
    val themeKey: String = "unec",
    val favoriteKeys: Set<String> = emptySet(),
    val savedCompassIds: Set<String> = emptySet(),
    val selectedMajor: String = "all",
    val selectedMaterialsYear: Int = 1,
    val selectedCurriculumMajor: String = "",
    val profileName: String = "",
    val profileFaculty: String = "",
    val profileMajor: String = "",
    val profileYear: Int = 0,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val lastLoginDate: String = "",
    val solvedTests: Int = 0,
    val pdfsOpened: Int = 0,
    val notesCount: Int = 0,
    val ratingsCount: Int = 0,
    val pomodoroTotalCompleted: Int = 0,
    val pomodoroTodayDate: String = "",
    val pomodoroTodaySessions: Int = 0,
    val pomodoroTodayMinutes: Int = 0,
    val unlockedBadges: Set<String> = emptySet(),
    val seenTgMaxId: Long = 0L
)

class PreferencesManager(private val context: Context) {
    private object Keys {
        val LANG = stringPreferencesKey("lang")
        val THEME = stringPreferencesKey("theme_key")
        val FAVORITES = stringSetPreferencesKey("favorite_keys")
        val COMPASS_SAVED = stringSetPreferencesKey("compass_saved")
        val SELECTED_MAJOR = stringPreferencesKey("selected_major")
        val SELECTED_YEAR = intPreferencesKey("selected_materials_year")
        val CURRICULUM_MAJOR = stringPreferencesKey("curriculum_major")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_FACULTY = stringPreferencesKey("profile_faculty")
        val PROFILE_MAJOR = stringPreferencesKey("profile_major")
        val PROFILE_YEAR = intPreferencesKey("profile_year")
        val XP = intPreferencesKey("xp")
        val STREAK = intPreferencesKey("streak")
        val LAST_ACTIVE = stringPreferencesKey("last_active")
        val LAST_LOGIN = stringPreferencesKey("last_login")
        val SOLVED_TESTS = intPreferencesKey("solved_tests")
        val PDFS_OPENED = intPreferencesKey("pdfs_opened")
        val NOTES_COUNT = intPreferencesKey("notes_count")
        val RATINGS_COUNT = intPreferencesKey("ratings_count")
        val POMO_TOTAL = intPreferencesKey("pomo_total")
        val POMO_DATE = stringPreferencesKey("pomo_date")
        val POMO_TODAY_SESSIONS = intPreferencesKey("pomo_today_sessions")
        val POMO_TODAY_MINS = intPreferencesKey("pomo_today_mins")
        val BADGES = stringSetPreferencesKey("unlocked_badges")
        val SEEN_TG_MAX = longPreferencesKey("seen_tg_max")
    }

    val state: Flow<UserPrefsState> = context.dataStore.data.map { p ->
        UserPrefsState(
            lang = p[Keys.LANG] ?: "az",
            themeKey = p[Keys.THEME] ?: "unec",
            favoriteKeys = p[Keys.FAVORITES] ?: emptySet(),
            savedCompassIds = p[Keys.COMPASS_SAVED] ?: emptySet(),
            selectedMajor = p[Keys.SELECTED_MAJOR] ?: "all",
            selectedMaterialsYear = p[Keys.SELECTED_YEAR] ?: 1,
            selectedCurriculumMajor = p[Keys.CURRICULUM_MAJOR] ?: "",
            profileName = p[Keys.PROFILE_NAME] ?: "",
            profileFaculty = p[Keys.PROFILE_FACULTY] ?: "",
            profileMajor = p[Keys.PROFILE_MAJOR] ?: "",
            profileYear = p[Keys.PROFILE_YEAR] ?: 0,
            xp = p[Keys.XP] ?: 0,
            streak = p[Keys.STREAK] ?: 0,
            lastActiveDate = p[Keys.LAST_ACTIVE] ?: "",
            lastLoginDate = p[Keys.LAST_LOGIN] ?: "",
            solvedTests = p[Keys.SOLVED_TESTS] ?: 0,
            pdfsOpened = p[Keys.PDFS_OPENED] ?: 0,
            notesCount = p[Keys.NOTES_COUNT] ?: 0,
            ratingsCount = p[Keys.RATINGS_COUNT] ?: 0,
            pomodoroTotalCompleted = p[Keys.POMO_TOTAL] ?: 0,
            pomodoroTodayDate = p[Keys.POMO_DATE] ?: "",
            pomodoroTodaySessions = p[Keys.POMO_TODAY_SESSIONS] ?: 0,
            pomodoroTodayMinutes = p[Keys.POMO_TODAY_MINS] ?: 0,
            unlockedBadges = p[Keys.BADGES] ?: emptySet(),
            seenTgMaxId = p[Keys.SEEN_TG_MAX] ?: 0L
        )
    }

    suspend fun setLang(lang: String) {
        context.dataStore.edit { it[Keys.LANG] = lang }
    }

    suspend fun setTheme(key: String) {
        context.dataStore.edit { it[Keys.THEME] = key }
    }

    suspend fun toggleFavorite(favKey: String): Boolean {
        var added = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            if (favKey in current) {
                prefs[Keys.FAVORITES] = current - favKey
                added = false
            } else {
                prefs[Keys.FAVORITES] = current + favKey
                added = true
            }
        }
        return added
    }

    suspend fun toggleSavedCompass(articleId: String): Boolean {
        var added = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COMPASS_SAVED] ?: emptySet()
            if (articleId in current) {
                prefs[Keys.COMPASS_SAVED] = current - articleId
                added = false
            } else {
                prefs[Keys.COMPASS_SAVED] = current + articleId
                added = true
            }
        }
        return added
    }

    suspend fun setSelectedMajor(major: String) {
        context.dataStore.edit { it[Keys.SELECTED_MAJOR] = major }
    }

    suspend fun setSelectedMaterialsYear(year: Int) {
        context.dataStore.edit { it[Keys.SELECTED_YEAR] = year.coerceIn(1, 4) }
    }

    suspend fun setSelectedCurriculumMajor(major: String) {
        context.dataStore.edit { it[Keys.CURRICULUM_MAJOR] = major }
    }

    suspend fun updateProfile(name: String, faculty: String, major: String, year: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROFILE_NAME] = name.trim()
            prefs[Keys.PROFILE_FACULTY] = faculty.trim()
            prefs[Keys.PROFILE_MAJOR] = major.trim()
            prefs[Keys.PROFILE_YEAR] = year.coerceIn(0, 4)
        }
    }

    suspend fun markSeenTgId(maxId: Long) {
        context.dataStore.edit { prefs ->
            val prev = prefs[Keys.SEEN_TG_MAX] ?: 0L
            if (maxId > prev) prefs[Keys.SEEN_TG_MAX] = maxId
        }
    }

    suspend fun editRaw(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    object RawKeys {
        val XP = Keys.XP
        val STREAK = Keys.STREAK
        val LAST_ACTIVE = Keys.LAST_ACTIVE
        val LAST_LOGIN = Keys.LAST_LOGIN
        val SOLVED_TESTS = Keys.SOLVED_TESTS
        val PDFS_OPENED = Keys.PDFS_OPENED
        val NOTES_COUNT = Keys.NOTES_COUNT
        val RATINGS_COUNT = Keys.RATINGS_COUNT
        val POMO_TOTAL = Keys.POMO_TOTAL
        val POMO_DATE = Keys.POMO_DATE
        val POMO_TODAY_SESSIONS = Keys.POMO_TODAY_SESSIONS
        val POMO_TODAY_MINS = Keys.POMO_TODAY_MINS
        val BADGES = Keys.BADGES
    }
}
