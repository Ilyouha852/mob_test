package com.example.mobdev_lab3.presentation.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobdev_lab3.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_about, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.textViewAppName).text = "Файловый менеджер"
        view.findViewById<TextView>(R.id.textViewDeveloper).text = "Разработчик: Иванов Илья, ИСТб-23-1"
        view.findViewById<TextView>(R.id.textViewDescription).text = """
            Это многооконное приложение файлового менеджера для Android.
            
            Возможности:
            • Просмотр файлов и папок (фрагмент)
            • Сортировка по имени, типу и дате (MVVM + LiveData)
            • Детальный просмотр файлов
            • Создание, переименование и удаление файлов
            • Закладки с цветовой маркировкой (фрагмент)
            • Работа с хранилищами: SharedPreferences, TXT, CSV (фрагмент)
            • Потоки и корутины (фрагмент + ViewModel)
            • Сетевые запросы HTTP/2 (фрагмент + Clean Architecture)
            • Метаданные файлов с тегами (greenDAO)
            • Настройки (фрагмент + ViewModel)
            
            Архитектура: MVVM + Clean Architecture (data / domain / presentation)
            Навигация: NavigationDrawer + Fragment-container
            
            Лабораторная работа №4 — Мобильная разработка.
        """.trimIndent()
    }
}
