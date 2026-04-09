package com.example.mobdev_lab3.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.mobdev_lab3.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация хранилища заметок с использованием SharedPreferences
 * Заметки сохраняются в JSON формате
 */
class SharedPreferencesStorage(context: Context) : NoteStorage {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "notes_storage"
        private const val KEY_NOTES = "notes"
    }
    
    /**
     * Получить все заметки из SharedPreferences
     */
    private fun getAllNotesMap(): MutableMap<String, Note> {
        val json = sharedPreferences.getString(KEY_NOTES, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Note>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    /**
     * Сохранить все заметки в SharedPreferences
     */
    private fun saveAllNotes(notes: Map<String, Note>) {
        val json = gson.toJson(notes)
        sharedPreferences.edit().putString(KEY_NOTES, json).apply()
    }
    
    override suspend fun create(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val notes = getAllNotesMap()
            notes[note.id] = note
            saveAllNotes(notes)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun read(id: String): Result<Note?> = withContext(Dispatchers.IO) {
        try {
            val notes = getAllNotesMap()
            Result.success(notes[id])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun update(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val notes = getAllNotesMap()
            if (!notes.containsKey(note.id)) {
                Result.failure(Exception("Заметка с ID ${note.id} не найдена"))
            } else {
                notes[note.id] = note
                saveAllNotes(notes)
                Result.success(note)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val notes = getAllNotesMap()
            val removed = notes.remove(id) != null
            if (removed) {
                saveAllNotes(notes)
            }
            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAll(): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val notes = getAllNotesMap()
            Result.success(notes.values.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getStorageInfo(): String {
        return "Тип: SharedPreferences\nФайл: shared_prefs/$PREFS_NAME.xml"
    }
}
