package com.example.mobdev_lab3.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Модель заметки для демонстрации работы с различными типами хранилищ
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Форматированная дата и время для отображения
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    // Преобразование в строку для TXT файла (формат: id|title|content|timestamp)
    fun toTxtFormat(): String {
        // Экранируем символ разделителя в данных
        val escapedTitle = title.replace("|", "\\|")
        val escapedContent = content.replace("|", "\\|")
        return "$id|$escapedTitle|$escapedContent|$timestamp"
    }
    
    // Преобразование в строку для CSV файла
    fun toCsvFormat(): String {
        // Экранируем запятые и кавычки для CSV
        val escapedTitle = escapeCsvField(title)
        val escapedContent = escapeCsvField(content)
        return "$id,$escapedTitle,$escapedContent,$timestamp"
    }
    
    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
    
    companion object {
        // Создание заметки из строки TXT формата
        fun fromTxtFormat(line: String): Note? {
            return try {
                val parts = line.split("|")
                if (parts.size != 4) return null
                
                Note(
                    id = parts[0],
                    title = parts[1].replace("\\|", "|"),
                    content = parts[2].replace("\\|", "|"),
                    timestamp = parts[3].toLong()
                )
            } catch (e: Exception) {
                null
            }
        }
        
        // Создание заметки из строки CSV формата
        fun fromCsvFormat(line: String): Note? {
            return try {
                val parts = parseCsvLine(line)
                if (parts.size != 4) return null
                
                Note(
                    id = parts[0],
                    title = parts[1],
                    content = parts[2],
                    timestamp = parts[3].toLong()
                )
            } catch (e: Exception) {
                null
            }
        }
        
        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            var current = StringBuilder()
            var inQuotes = false
            var i = 0
            
            while (i < line.length) {
                val char = line[i]
                
                when {
                    char == '"' && (i + 1 < line.length && line[i + 1] == '"') -> {
                        // Escaped quote
                        current.append('"')
                        i++
                    }
                    char == '"' -> {
                        inQuotes = !inQuotes
                    }
                    char == ',' && !inQuotes -> {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                    else -> {
                        current.append(char)
                    }
                }
                i++
            }
            result.add(current.toString())
            return result
        }
    }
}
