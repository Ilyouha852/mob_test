package com.example.mobdev_lab3.ui.storage

import com.example.mobdev_lab3.repository.NoteStorage
import com.example.mobdev_lab3.repository.SharedPreferencesStorage

// Фрагмент для работы с SharedPreferences хранилищем
class SharedPreferencesFragment : BaseStorageFragment() {
    
    override val storage: NoteStorage by lazy {
        SharedPreferencesStorage(requireContext())
    }
}
