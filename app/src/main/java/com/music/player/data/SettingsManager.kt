package com.music.player.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val videoToAudioKey = booleanPreferencesKey("video_to_audio")
    private val lastSongIdKey = stringPreferencesKey("last_song_id")
    private val lastPositionKey = androidx.datastore.preferences.core.longPreferencesKey("last_position")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name)
    }

    val videoToAudioEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[videoToAudioKey] ?: false
    }

    val lastSongId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[lastSongIdKey]?.toLongOrNull()
    }

    val lastPosition: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[lastPositionKey] ?: 0L
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }

    suspend fun setVideoToAudioEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[videoToAudioKey] = enabled
        }
    }

    suspend fun saveLastPlaybackState(songId: Long, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[lastSongIdKey] = songId.toString()
            preferences[lastPositionKey] = position
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}
