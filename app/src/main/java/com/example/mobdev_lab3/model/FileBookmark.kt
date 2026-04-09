package com.example.mobdev_lab3.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class FileBookmark(
    val id: Long,
    val name: String,
    val path: String,
    val description: String,
    val color: BookmarkColor,
    val createdDate: Long,
    val isDirectory: Boolean
) : Serializable {
    
    fun getFormattedDate(): String {
        val date = Date(createdDate)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    fun getShortPath(): String {
        return if (path.length > 30) {
            "..." + path.substring(path.length - 27)
        } else {
            path
        }
    }
    
    companion object {
        private var currentId = 1L
        
        fun createSampleBookmarks(): MutableList<FileBookmark> {
            // Возвращаем пустой список - пользователь создаст свои закладки
            return mutableListOf()
        }
        
        fun getNextId(): Long = currentId++
    }
}

enum class BookmarkColor(val colorResId: Int, val displayName: String) {
    RED(android.R.color.holo_red_light, "Важное"),
    BLUE(android.R.color.holo_blue_light, "Работа"),
    GREEN(android.R.color.holo_green_light, "Проекты"),
    ORANGE(android.R.color.holo_orange_light, "Документы"),
    PURPLE(android.R.color.holo_purple, "Медиа")
}
