package com.example.mobdev_lab3.presentation.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.core.content.edit
import com.example.mobdev_lab3.model.SortMode

data class AppSettings(
    val sortMode: SortMode = SortMode.BY_NAME,
    val showHidden: Boolean = false,
    val showSystemFiles: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val PREFS_NAME = "file_manager_prefs"
        const val KEY_SORT_MODE = "sort_mode"
        const val KEY_SHOW_HIDDEN = "show_hidden"
        const val KEY_SHOW_SYSTEM_FILES = "show_system_files"
    }

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableLiveData(loadFromPrefs())
    val settings: LiveData<AppSettings> = _settings

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private fun loadFromPrefs(): AppSettings {
        val sortName = prefs.getString(KEY_SORT_MODE, SortMode.BY_NAME.name) ?: SortMode.BY_NAME.name
        return AppSettings(
            sortMode = SortMode.valueOf(sortName),
            showHidden = prefs.getBoolean(KEY_SHOW_HIDDEN, false),
            showSystemFiles = prefs.getBoolean(KEY_SHOW_SYSTEM_FILES, false)
        )
    }

    fun reload() {
        _settings.value = loadFromPrefs()
    }

    fun save(settings: AppSettings) {
        prefs.edit {
            putString(KEY_SORT_MODE, settings.sortMode.name)
            putBoolean(KEY_SHOW_HIDDEN, settings.showHidden)
            putBoolean(KEY_SHOW_SYSTEM_FILES, settings.showSystemFiles)
        }
        _settings.value = settings
        _saved.value = true
        _saved.value = false
    }

    fun reset() {
        prefs.edit { clear() }
        _settings.value = AppSettings()
    }
}
