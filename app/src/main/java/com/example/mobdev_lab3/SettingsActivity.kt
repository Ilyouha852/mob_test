package com.example.mobdev_lab3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobdev_lab3.model.SortMode
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var radioGroupSort: RadioGroup
    private lateinit var radioByName: RadioButton
    private lateinit var radioByType: RadioButton
    private lateinit var radioByDate: RadioButton
    private lateinit var checkBoxShowHidden: CheckBox
    private lateinit var checkBoxShowSystemFiles: CheckBox
    private lateinit var buttonSave: Button
    private lateinit var buttonBack: Button
    private lateinit var buttonReset: Button
    
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        const val PREFS_NAME = "file_manager_prefs"
        const val KEY_SORT_MODE = "sort_mode"
        const val KEY_SHOW_HIDDEN = "show_hidden"
        const val KEY_SHOW_SYSTEM_FILES = "show_system_files"
        
        const val EXTRA_SORT_MODE = "sort_mode"
        const val EXTRA_SHOW_HIDDEN = "show_hidden"
        const val EXTRA_SHOW_SYSTEM_FILES = "show_system_files"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initViews()
        setupClickListeners()
        loadSettings()
    }
    
    private fun initViews() {
        radioGroupSort = findViewById(R.id.radioGroupSort)
        radioByName = findViewById(R.id.radioByName)
        radioByType = findViewById(R.id.radioByType)
        radioByDate = findViewById(R.id.radioByDate)
        checkBoxShowHidden = findViewById(R.id.checkBoxShowHidden)
        checkBoxShowSystemFiles = findViewById(R.id.checkBoxShowSystemFiles)
        buttonSave = findViewById(R.id.buttonSave)
        buttonBack = findViewById(R.id.buttonBack)
        buttonReset = findViewById(R.id.buttonReset)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }
    
    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
        
        buttonSave.setOnClickListener {
            saveSettings()
        }
        
        buttonReset.setOnClickListener {
            resetToDefaults()
        }
    }
    
    private fun loadSettings() {
        val sortMode = sharedPreferences.getString(KEY_SORT_MODE, SortMode.BY_NAME.name) ?: SortMode.BY_NAME.name
        val showHidden = sharedPreferences.getBoolean(KEY_SHOW_HIDDEN, false)
        val showSystemFiles = sharedPreferences.getBoolean(KEY_SHOW_SYSTEM_FILES, false)
        
        when (sortMode) {
            SortMode.BY_NAME.name -> radioByName.isChecked = true
            SortMode.BY_TYPE.name -> radioByType.isChecked = true
            SortMode.BY_DATE.name -> radioByDate.isChecked = true
        }
        
        checkBoxShowHidden.isChecked = showHidden
        checkBoxShowSystemFiles.isChecked = showSystemFiles
    }
    
    private fun saveSettings() {
        val sortMode = when {
            radioByName.isChecked -> SortMode.BY_NAME.name
            radioByType.isChecked -> SortMode.BY_TYPE.name
            radioByDate.isChecked -> SortMode.BY_DATE.name
            else -> SortMode.BY_NAME.name
        }
        
        val showHidden = checkBoxShowHidden.isChecked
        val showSystemFiles = checkBoxShowSystemFiles.isChecked
        
        sharedPreferences.edit {
            putString(KEY_SORT_MODE, sortMode)
                .putBoolean(KEY_SHOW_HIDDEN, showHidden)
                .putBoolean(KEY_SHOW_SYSTEM_FILES, showSystemFiles)
        }
        
        // Возвращаем результат в MainActivity
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_SORT_MODE, sortMode)
        resultIntent.putExtra(EXTRA_SHOW_HIDDEN, showHidden)
        resultIntent.putExtra(EXTRA_SHOW_SYSTEM_FILES, showSystemFiles)
        setResult(RESULT_OK, resultIntent)
        
        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun resetToDefaults() {
        AlertDialog.Builder(this)
            .setTitle("Сброс настроек")
            .setMessage("Вы уверены, что хотите сбросить настройки к значениям по умолчанию?")
            .setPositiveButton("Сбросить") { _, _ ->
                radioByName.isChecked = true
                checkBoxShowHidden.isChecked = false
                checkBoxShowSystemFiles.isChecked = false
                
                sharedPreferences.edit { clear() }
                
                Toast.makeText(this, "Настройки сброшены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
