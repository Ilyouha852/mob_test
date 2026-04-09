package com.example.mobdev_lab3.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.mobdev_lab3.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Реализация хранилища заметок с использованием CSV файла
 * в shared storage (Downloads)
 * Использует MediaStore API для Android 10+ и прямой доступ для более старых версий
 */
class CsvFileStorage(private val context: Context) : NoteStorage {
    
    companion object {
        private const val FILE_NAME = "notes.csv"
        private const val CSV_HEADER = "id,title,content,timestamp"
    }
    
    /**
     * Получить путь к файлу в Downloads
     */
    private fun getDownloadsPath(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Downloads/$FILE_NAME"
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, FILE_NAME).absolutePath
        }
    }
    
    /**
     * Чтение всех заметок из CSV файла
     */
    private suspend fun readAllNotes(): MutableList<Note> = withContext(Dispatchers.IO) {
        val notes = mutableListOf<Note>()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - используем MediaStore
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val projection = arrayOf(
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME
                )
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(FILE_NAME)
                
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val id = cursor.getLong(idColumn)
                        val uri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY, id)
                        
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val lines = inputStream.bufferedReader().readLines()
                            // Пропускаем заголовок
                            lines.drop(1).forEach { line ->
                                Note.fromCsvFormat(line)?.let { notes.add(it) }
                            }
                        }
                    }
                }
            } else {
                // Android 9 и ниже - прямой доступ к файлу
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, FILE_NAME)
                
                if (file.exists()) {
                    val lines = file.readLines()
                    // Пропускаем заголовок
                    lines.drop(1).forEach { line ->
                        Note.fromCsvFormat(line)?.let { notes.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            // Файл не существует или ошибка чтения - возвращаем пустой список
        }
        
        notes
    }
    
    /**
     * Запись всех заметок в CSV файл
     */
    private suspend fun writeAllNotes(notes: List<Note>) = withContext(Dispatchers.IO) {
        val csvContent = buildString {
            appendLine(CSV_HEADER)
            notes.forEach { note ->
                appendLine(note.toCsvFormat())
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - используем MediaStore
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            
            // Сначала пытаемся найти существующий файл
            val projection = arrayOf(MediaStore.Downloads._ID)
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(FILE_NAME)
            
            var uri = context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    val id = cursor.getLong(idColumn)
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY, id)
                } else {
                    null
                }
            }
            
            // Если файл не найден, создаем новый
            if (uri == null) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                uri = context.contentResolver.insert(collection, contentValues)
            }
            
            uri?.let {
                context.contentResolver.openOutputStream(it, "wt")?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                
                // Убираем флаг IS_PENDING
                val updateValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                context.contentResolver.update(it, updateValues, null, null)
            }
        } else {
            // Android 9 и ниже - прямой доступ к файлу
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, FILE_NAME)
            file.writeText(csvContent)
        }
    }
    
    override suspend fun create(note: Note): Result<Note> {
        return try {
            val notes = readAllNotes()
            notes.add(note)
            writeAllNotes(notes)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun read(id: String): Result<Note?> {
        return try {
            val notes = readAllNotes()
            val note = notes.find { it.id == id }
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun update(note: Note): Result<Note> {
        return try {
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
    
    override suspend fun delete(id: String): Result<Boolean> {
        return try {
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
    
    override suspend fun getAll(): Result<List<Note>> {
        return try {
            val notes = readAllNotes()
            Result.success(notes.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getStorageInfo(): String {
        return "Тип: CSV файл (Shared storage)\nПуть: ${getDownloadsPath()}"
    }
}
