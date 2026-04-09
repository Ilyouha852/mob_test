package com.example.mobdev_lab3

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class AboutActivity : AppCompatActivity() {
    
    private lateinit var textViewAppName: TextView
    private lateinit var textViewDeveloper: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var buttonBack: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        initViews()
        setupClickListeners()
        loadAppInfo()
    }
    
    private fun initViews() {
        textViewAppName = findViewById(R.id.textViewAppName)
        textViewDeveloper = findViewById(R.id.textViewDeveloper)
        textViewDescription = findViewById(R.id.textViewDescription)
        buttonBack = findViewById(R.id.buttonBack)
    }
    
    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun loadAppInfo() {
        try {
            
            textViewAppName.text = "Файловый менеджер"
            textViewDeveloper.text = "Разработчик: Иванов Илья, ИСТб-23-1"
            
            textViewDescription.text = """
                Это многооконное приложение файлового менеджера для Android.
                
                Возможности:
                • Просмотр файлов и папок
                • Сортировка по имени, типу и дате
                • Детальный просмотр файлов
                • Создание, переименование и удаление файлов
                • Настройки отображения
                • Навигация по файловой системе
                
                Приложение разработано в рамках лабораторной работы №4
                по дисциплине "Мобильная разработка".
            """.trimIndent()
            
        } catch (_: PackageManager.NameNotFoundException) {
            textViewAppName.text = "Файловый менеджер"
            textViewDeveloper.text = "Разработчик: Иванов Илья, ИСТб-23-1"
            textViewDescription.text = "Информация о приложении недоступна"
        }
    }
}
