package com.example.mobdev_lab3.data.repository

import android.content.Context
import com.example.mobdev_lab3.domain.repository.IBookmarkRepository
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import org.json.JSONArray
import org.json.JSONObject

class BookmarkRepositoryImpl(private val context: Context) : IBookmarkRepository {

    companion object {
        private const val PREFS_NAME = "bookmarks_prefs"
        private const val KEY_BOOKMARKS = "bookmarks_data"
    }

    override fun loadBookmarks(): List<FileBookmark> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                FileBookmark(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    path = obj.getString("path"),
                    description = obj.getString("description"),
                    color = BookmarkColor.valueOf(obj.getString("color")),
                    createdDate = obj.getLong("createdDate"),
                    isDirectory = obj.getBoolean("isDirectory")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveBookmarks(bookmarks: List<FileBookmark>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        bookmarks.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("name", b.name)
            obj.put("path", b.path)
            obj.put("description", b.description)
            obj.put("color", b.color.name)
            obj.put("createdDate", b.createdDate)
            obj.put("isDirectory", b.isDirectory)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, jsonArray.toString()).apply()
    }
}
