package com.example.mobdev_lab3.presentation.storage

import com.example.mobdev_lab3.data.storage.NoteStorage
import com.example.mobdev_lab3.data.storage.SharedPreferencesStorage

// Фрагмент для работы с SharedPreferences хранилищем
class SharedPreferencesFragment : BaseStorageFragment() {
    
    override val storage: NoteStorage by lazy {
        SharedPreferencesStorage(requireContext())
    }
}
