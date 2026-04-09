package com.example.mobdev_lab3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.adapter.FileMetadataAdapter
import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.database.repository.FileMetadataRepository
import com.example.mobdev_lab3.database.repository.TagRepository
import com.example.mobdev_lab3.dialog.TagManagementDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.example.mobdev_lab3.manager.BookmarksManager
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import java.io.File
import java.util.Date

class FileMetadataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileMetadataAdapter
    private lateinit var fabAddTag: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var spinnerFilterTags: Spinner
    private lateinit var checkBoxFilterFavorite: CheckBox
    
    private val fileRepository = FileMetadataRepository()
    private val tagRepository = TagRepository()
    
    private var currentSearchQuery = ""
    private var currentTagFilter: Tag? = null
    private var isFavoriteFilter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_metadata)

        recyclerView = findViewById(R.id.recyclerView)
        fabAddTag = findViewById(R.id.fabAddTag)
        searchView = findViewById(R.id.searchView)
        spinnerFilterTags = findViewById(R.id.spinnerFilterTags)
        checkBoxFilterFavorite = findViewById(R.id.checkBoxFilterFavorite)

        setupRecyclerView()
        setupFab()
        setupFilters()
        loadData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FileMetadataAdapter(
            emptyList(),
            emptyMap(),
            onItemClick = { file ->
                // Show file metadata details dialog
                showFileDetailsDialog(file)
            },
            onFavoriteClick = { file ->
                val newStatus = !(file.isFavorite ?: false)
                
                if (newStatus) {
                    // Adding to favorites -> Show Add Bookmark Dialog
                    val fileObj = File(file.filePath)
                    
                    // Determine initial color/type based on existing tag
                    val fileTags = tagRepository.getTagsByFile(file.id)
                    var initialColor: BookmarkColor? = null
                    if (fileTags.isNotEmpty()) {
                        // Try to match tag name to BookmarkColor display name
                        val tagName = fileTags[0].name
                        initialColor = BookmarkColor.values().find { it.displayName == tagName }
                    }

                    BookmarksDialogHelper(this).showAddBookmarkDialog(
                        initialPath = file.filePath,
                        initialIsDir = fileObj.isDirectory,
                        initialName = file.fileName,
                        initialColor = initialColor
                    ) {
                        // Callback when bookmark is added
                        file.isFavorite = true
                        fileRepository.updateFileMetadata(file)
                        loadData()
                    }
                } else {
                    // Removing from favorites
                    file.isFavorite = false
                    fileRepository.updateFileMetadata(file)
                    
                    if (BookmarksManager.isBookmarked(this, file.filePath)) {
                        BookmarksManager.removeBookmarkByPath(this, file.filePath)
                        Toast.makeText(this, "Удалено из закладок", Toast.LENGTH_SHORT).show()
                    }
                    loadData()
                }
            },
            onItemLongClick = { file ->
                val allTags = tagRepository.getAllTags()
                val fileTags = tagRepository.getTagsByFile(file.id)
                
                TagManagementDialog(this).showAssignTagsDialog(this, allTags, fileTags) { selectedTags ->
                    // Update tags for file
                    // First remove all existing
                    val existingTags = tagRepository.getTagsByFile(file.id)
                    for (tag in existingTags) {
                        fileRepository.removeTagFromFile(file.id, tag.id)
                    }
                    // Add new ones
                    for (tag in selectedTags) {
                        fileRepository.addTagToFile(file.id, tag.id)
                    }
                    loadData()
                    Toast.makeText(this, "Теги обновлены", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { file ->
                android.app.AlertDialog.Builder(this)
                    .setTitle("Удалить метаданные файла")
                    .setMessage("Вы уверены, что хотите удалить этот файл из базы данных?")
                    .setPositiveButton("Удалить") { _, _ ->
                        fileRepository.deleteFileMetadata(file.id)
                        loadData()
                        Toast.makeText(this, "Файл удален из базы данных", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        // Search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                loadData()
                return true
            }
        })

        // CheckBox
        checkBoxFilterFavorite.setOnCheckedChangeListener { _, isChecked ->
            isFavoriteFilter = isChecked
            loadData()
        }

        // Spinner
        val allTags = tagRepository.getAllTags().toMutableList()
        // Add a dummy "All Tags" option
        val allTagsOption = Tag()
        allTagsOption.name = "Все теги"
        allTagsOption.id = -1L
        allTags.add(0, allTagsOption)

        val tagNames = allTags.map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tagNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterTags.adapter = spinnerAdapter

        spinnerFilterTags.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTag = allTags[position]
                currentTagFilter = if (selectedTag.id == -1L) null else selectedTag
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentTagFilter = null
                loadData()
            }
        }
    }

    private fun setupFab() {
        fabAddTag.setOnClickListener {
            TagManagementDialog(this).showCreateTagDialog { name ->
                val newTag = Tag()
                newTag.name = name
                newTag.createdDate = Date().time
                tagRepository.createTag(newTag)
                Toast.makeText(this, "Тег создан!", Toast.LENGTH_SHORT).show()
                
                // Refresh spinner
                setupFilters()
            }
        }
    }

    private fun loadData() {
        var files = fileRepository.getAllMetadata()

        // Filter by Name
        if (currentSearchQuery.isNotEmpty()) {
            files = files.filter { it.fileName.contains(currentSearchQuery, ignoreCase = true) }
        }

        // Filter by Favorite
        if (isFavoriteFilter) {
            files = files.filter { it.isFavorite == true }
        }

        // Filter by Tag
        currentTagFilter?.let { tag ->
            val filesWithTag = fileRepository.getFilesByTag(tag.id).map { it.id }.toSet()
            files = files.filter { it.id in filesWithTag }
        }

        val tagsMap = files.associate { file ->
            file.id to tagRepository.getTagsByFile(file.id).map { it.name }
        }
        
        // Sync favorites status from BookmarksManager
        val bookmarkedPaths = BookmarksManager.getBookmarks(this).map { it.path }.toSet()
        var dataChanged = false
        for (file in files) {
            if (bookmarkedPaths.contains(file.filePath) && (file.isFavorite != true)) {
                file.isFavorite = true
                fileRepository.updateFileMetadata(file)
                dataChanged = true
            }
        }
        
        if (dataChanged) {
            // Reload if we updated database
            files = fileRepository.getAllMetadata()
            // Re-apply filters if needed, but for now just update adapter
             // Filter by Name
            if (currentSearchQuery.isNotEmpty()) {
                files = files.filter { it.fileName.contains(currentSearchQuery, ignoreCase = true) }
            }

            // Filter by Favorite
            if (isFavoriteFilter) {
                files = files.filter { it.isFavorite == true }
            }

            // Filter by Tag
            currentTagFilter?.let { tag ->
                val filesWithTag = fileRepository.getFilesByTag(tag.id).map { it.id }.toSet()
                files = files.filter { it.id in filesWithTag }
            }
        }

        adapter.updateData(files, tagsMap)
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showFileDetailsDialog(file: FileMetadata) {
        val fileTags = tagRepository.getTagsByFile(file.id)
        val tagsString = if (fileTags.isNotEmpty()) {
            fileTags.joinToString(", ") { it.name }
        } else {
            "Нет тегов"
        }
        
        val lastAccessFormatted = file.lastAccessDate?.let {
            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))
        } ?: "Неизвестно"
        
        val favoriteStatus = if (file.isFavorite == true) "Да" else "Нет"
        
        val message = """
            |Имя файла: ${file.fileName}
            |
            |Путь: ${file.filePath}
            |
            |Последний доступ: $lastAccessFormatted
            |
            |Избранное: $favoriteStatus
            |
            |Теги: $tagsString
        """.trimMargin()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Метаданные файла")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
