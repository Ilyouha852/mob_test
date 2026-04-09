package com.example.mobdev_lab3.repository

import android.content.Context
import com.example.mobdev_lab3.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Реализация хранилища заметок с использованием TXT файла
 * в app-specific storage (internal storage)
 * Формат: каждая заметка на отдельной строке в формате id|title|content|timestamp
 */
class TxtFileStorage(private val context: Context) : NoteStorage {
    
    companion object {
        private const val FILE_NAME = "notes.txt"
    }
    
    private val file: File
        get() = File(context.filesDir, FILE_NAME)
    
    /**
     * Синхронизированное чтение всех заметок из файла
     */
    @Synchronized
    private fun readAllNotes(): MutableList<Note> {
        if (!file.exists()) {
            return mutableListOf()
        }
        
        return file.readLines()
            .mapNotNull { line -> Note.fromTxtFormat(line) }
            .toMutableList()
    }
    
    /**
     * Синхронизированная запись всех заметок в файл
     */
    @Synchronized
    private fun writeAllNotes(notes: List<Note>) {
        file.writeText(notes.joinToString("\n") { it.toTxtFormat() })
    }
    
    override suspend fun create(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val notes = readAllNotes()
            notes.add(note)
            writeAllNotes(notes)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun read(id: String): Result<Note?> = withContext(Dispatchers.IO) {
        try {
            val notes = readAllNotes()
            val note = notes.find { it.id == id }
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun update(note: Note): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val notes = readAllNotes()
            val index = notes.indexOfFirst { it.id == note.id }
            
            if (index == -1) {
                Result.failure(Exception("Заметка с ID ${note.id} не найдена"))
            } else {
                notes[index] = note
                writeAllNotes(notes)
                Result.success(note)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val notes = readAllNotes()
            val removed = notes.removeIf { it.id == id }
            
            if (removed) {
                writeAllNotes(notes)
            }
            
            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAll(): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val notes = readAllNotes()
            Result.success(notes.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getStorageInfo(): String {
        return "Тип: TXT файл (App-specific storage)\nПуть: ${file.absolutePath}"
    }
}
