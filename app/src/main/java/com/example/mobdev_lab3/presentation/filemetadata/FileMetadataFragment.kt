package com.example.mobdev_lab3.presentation.filemetadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.adapter.FileMetadataAdapter
import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.database.repository.FileMetadataRepository
import com.example.mobdev_lab3.database.repository.TagRepository
import com.example.mobdev_lab3.dialog.TagManagementDialog
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.example.mobdev_lab3.manager.BookmarksManager
import com.example.mobdev_lab3.model.BookmarkColor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.Date

class FileMetadataFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.activity_file_metadata, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView           = view.findViewById(R.id.recyclerView)
        fabAddTag              = view.findViewById(R.id.fabAddTag)
        searchView             = view.findViewById(R.id.searchView)
        spinnerFilterTags      = view.findViewById(R.id.spinnerFilterTags)
        checkBoxFilterFavorite = view.findViewById(R.id.checkBoxFilterFavorite)

        setupRecyclerView()
        setupFab()
        setupFilters()
        loadData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FileMetadataAdapter(
            emptyList(),
            emptyMap(),
            onItemClick = { file -> showFileDetailsDialog(file) },
            onFavoriteClick = { file ->
                val newStatus = !(file.isFavorite ?: false)
                if (newStatus) {
                    val fileObj = File(file.filePath)
                    val fileTags = tagRepository.getTagsByFile(file.id)
                    val initialColor = fileTags.firstOrNull()?.let { tag ->
                        BookmarkColor.values().find { it.displayName == tag.name }
                    }
                    BookmarksDialogHelper(requireContext()).showAddBookmarkDialog(
                        initialPath = file.filePath,
                        initialIsDir = fileObj.isDirectory,
                        initialName = file.fileName,
                        initialColor = initialColor
                    ) {
                        file.isFavorite = true
                        fileRepository.updateFileMetadata(file)
                        loadData()
                    }
                } else {
                    file.isFavorite = false
                    fileRepository.updateFileMetadata(file)
                    if (BookmarksManager.isBookmarked(requireContext(), file.filePath)) {
                        BookmarksManager.removeBookmarkByPath(requireContext(), file.filePath)
                        Toast.makeText(requireContext(), "Удалено из закладок", Toast.LENGTH_SHORT).show()
                    }
                    loadData()
                }
            },
            onItemLongClick = { file ->
                val allTags = tagRepository.getAllTags()
                val fileTags = tagRepository.getTagsByFile(file.id)
                TagManagementDialog(requireContext()).showAssignTagsDialog(
                    requireContext(), allTags, fileTags
                ) { selectedTags ->
                    tagRepository.getTagsByFile(file.id).forEach { tag ->
                        fileRepository.removeTagFromFile(file.id, tag.id)
                    }
                    selectedTags.forEach { tag -> fileRepository.addTagToFile(file.id, tag.id) }
                    loadData()
                    Toast.makeText(requireContext(), "Теги обновлены", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { file ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Удалить метаданные файла")
                    .setMessage("Вы уверены?")
                    .setPositiveButton("Удалить") { _, _ ->
                        fileRepository.deleteFileMetadata(file.id)
                        loadData()
                        Toast.makeText(requireContext(), "Файл удалён из базы", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAddTag.setOnClickListener {
            TagManagementDialog(requireContext()).showCreateTagDialog { name ->
                val newTag = Tag()
                newTag.name = name
                newTag.createdDate = Date().time
                tagRepository.createTag(newTag)
                Toast.makeText(requireContext(), "Тег создан!", Toast.LENGTH_SHORT).show()
                setupFilters()
            }
        }
    }

    private fun setupFilters() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                loadData()
                return true
            }
        })

        checkBoxFilterFavorite.setOnCheckedChangeListener { _, checked ->
            isFavoriteFilter = checked
            loadData()
        }

        val allTags = tagRepository.getAllTags().toMutableList()
        val allOption = Tag().apply { name = "Все теги"; id = -1L }
        allTags.add(0, allOption)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, allTags.map { it.name })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterTags.adapter = spinnerAdapter

        spinnerFilterTags.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentTagFilter = if (allTags[position].id == -1L) null else allTags[position]
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { currentTagFilter = null; loadData() }
        }
    }

    private fun loadData() {
        var files = fileRepository.getAllMetadata()

        if (currentSearchQuery.isNotEmpty())
            files = files.filter { it.fileName.contains(currentSearchQuery, ignoreCase = true) }
        if (isFavoriteFilter)
            files = files.filter { it.isFavorite == true }
        currentTagFilter?.let { tag ->
            val ids = fileRepository.getFilesByTag(tag.id).map { it.id }.toSet()
            files = files.filter { it.id in ids }
        }

        val tagsMap = files.associate { file ->
            file.id to tagRepository.getTagsByFile(file.id).map { it.name }
        }

        val bookmarkedPaths = BookmarksManager.getBookmarks(requireContext()).map { it.path }.toSet()
        var changed = false
        for (file in files) {
            if (bookmarkedPaths.contains(file.filePath) && file.isFavorite != true) {
                file.isFavorite = true
                fileRepository.updateFileMetadata(file)
                changed = true
            }
        }
        if (changed) {
            files = fileRepository.getAllMetadata()
            if (currentSearchQuery.isNotEmpty())
                files = files.filter { it.fileName.contains(currentSearchQuery, ignoreCase = true) }
            if (isFavoriteFilter)
                files = files.filter { it.isFavorite == true }
            currentTagFilter?.let { tag ->
                val ids = fileRepository.getFilesByTag(tag.id).map { it.id }.toSet()
                files = files.filter { it.id in ids }
            }
        }
        adapter.updateData(files, tagsMap)
    }

    private fun showFileDetailsDialog(file: FileMetadata) {
        val fileTags = tagRepository.getTagsByFile(file.id)
        val tagsStr = if (fileTags.isNotEmpty()) fileTags.joinToString(", ") { it.name } else "Нет тегов"
        val lastAccess = file.lastAccessDate?.let {
            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(Date(it))
        } ?: "Неизвестно"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Метаданные файла")
            .setMessage("Имя: ${file.fileName}\n\nПуть: ${file.filePath}\n\nДоступ: $lastAccess\n\nИзбранное: ${if (file.isFavorite == true) "Да" else "Нет"}\n\nТеги: $tagsStr")
            .setPositiveButton("OK", null)
            .show()
    }
}
