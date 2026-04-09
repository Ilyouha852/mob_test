package com.example.mobdev_lab3.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import org.json.JSONArray
import org.json.JSONObject

object BookmarksManager {
    private const val PREFS_NAME = "bookmarks_prefs"
    private const val KEY_BOOKMARKS = "bookmarks_data"

    fun getBookmarks(context: Context): MutableList<FileBookmark> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_BOOKMARKS, null) ?: return mutableListOf()
        
        val bookmarks = mutableListOf<FileBookmark>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val bookmark = FileBookmark(
                    id = jsonObject.getLong("id"),
                    name = jsonObject.getString("name"),
                    path = jsonObject.getString("path"),
                    description = jsonObject.getString("description"),
                    color = BookmarkColor.valueOf(jsonObject.getString("color")),
                    createdDate = jsonObject.getLong("createdDate"),
                    isDirectory = jsonObject.getBoolean("isDirectory")
                )
                bookmarks.add(bookmark)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bookmarks
    }

    fun saveBookmarks(context: Context, bookmarks: List<FileBookmark>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        val jsonArray = JSONArray()
        for (bookmark in bookmarks) {
            val jsonObject = JSONObject()
            jsonObject.put("id", bookmark.id)
            jsonObject.put("name", bookmark.name)
            jsonObject.put("path", bookmark.path)
            jsonObject.put("description", bookmark.description)
            jsonObject.put("color", bookmark.color.name)
            jsonObject.put("createdDate", bookmark.createdDate)
            jsonObject.put("isDirectory", bookmark.isDirectory)
            jsonArray.put(jsonObject)
        }
        
        editor.putString(KEY_BOOKMARKS, jsonArray.toString())
        editor.apply()
    }

    fun addBookmark(context: Context, bookmark: FileBookmark) {
        val bookmarks = getBookmarks(context)
        bookmarks.add(bookmark)
        saveBookmarks(context, bookmarks)
    }

    fun removeBookmarkByPath(context: Context, path: String) {
        val bookmarks = getBookmarks(context)
        val iterator = bookmarks.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().path == path) {
                iterator.remove()
                saveBookmarks(context, bookmarks)
                return
            }
        }
    }

    fun isBookmarked(context: Context, path: String): Boolean {
        val bookmarks = getBookmarks(context)
        return bookmarks.any { it.path == path }
    }
}
