package com.example.mobdev_lab3.presentation.filemetadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.adapter.FileMetadataAdapter
import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.dialog.TagManagementDialog
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.example.mobdev_lab3.model.BookmarkColor
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FileMetadataFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileMetadataAdapter
    private lateinit var fabAddTag: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var spinnerFilterTags: Spinner
    private lateinit var checkBoxFilterFavorite: CheckBox

    private val viewModel: FileMetadataViewModel by viewModels()

    private var tagsList: List<Tag> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_file_metadata, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        fabAddTag = view.findViewById(R.id.fabAddTag)
        searchView = view.findViewById(R.id.searchView)
        spinnerFilterTags = view.findViewById(R.id.spinnerFilterTags)
        checkBoxFilterFavorite = view.findViewById(R.id.checkBoxFilterFavorite)

        setupRecyclerView()
        setupObservers()
        setupSearchAndFilters()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FileMetadataAdapter(
            emptyList(),
            emptyMap(),
            onItemClick = { file ->
                val fileTags = tagsList.filter { tag ->
                    viewModel.uiState.value?.tagsMap?.get(file.id)?.contains(tag.name) == true
                }
                val tagsStr = if (fileTags.isNotEmpty()) fileTags.joinToString(", ") { it.name } else "Нет тегов"
                val lastAccess = file.lastAccessDate?.let {
                    java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(it))
                } ?: "Неизвестно"
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Метаданные файла")
                    .setMessage("Имя: ${file.fileName}\n\nПуть: ${file.filePath}\n\nПоследний доступ: $lastAccess\n\nИзбранное: ${if (file.isFavorite == true) "Да" else "Нет"}\n\nТеги: $tagsStr")
                    .setPositiveButton("OK", null)
                    .show()
            },
            onFavoriteClick = { file ->
                viewModel.toggleFavorite(file) { fileToAdd, fileTags ->
                    val initialColor = fileTags.firstOrNull()?.let { tag ->
                        BookmarkColor.values().find { it.displayName == tag.name }
                    }
                    BookmarksDialogHelper(requireContext()).showAddBookmarkDialog(
                        initialPath = fileToAdd.filePath,
                        initialIsDir = java.io.File(fileToAdd.filePath).isDirectory,
                        initialName = fileToAdd.fileName,
                        initialColor = initialColor
                    ) {
                        viewModel.confirmAddToFavorites(fileToAdd)
                    }
                }
            },
            onItemLongClick = { file ->
                val allTags = viewModel.uiState.value?.availableTags ?: emptyList()
                val fileTags = allTags.filter { tag ->
                    viewModel.uiState.value?.tagsMap?.get(file.id)?.contains(tag.name) == true
                }
                TagManagementDialog(requireContext()).showAssignTagsDialog(
                    requireContext(), allTags, fileTags
                ) { selectedTags ->
                    viewModel.assignTags(file.id, selectedTags)
                }
            },
            onDeleteClick = { file ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Удалить метаданные файла")
                    .setMessage("Вы уверены, что хотите удалить «${file.fileName}» из базы данных?")
                    .setPositiveButton("Удалить") { _, _ -> viewModel.deleteMetadata(file) }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.updateData(state.files, state.tagsMap)
            tagsList = state.availableTags
            refreshTagsSpinner(state.availableTags)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            when (event) {
                is FileMetadataEvent.Message ->
                    Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
            }
            viewModel.consumeEvent()
        }
    }

    private fun setupSearchAndFilters() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        checkBoxFilterFavorite.setOnCheckedChangeListener { _, checked ->
            viewModel.setFavoriteFilter(checked)
        }
    }

    private fun refreshTagsSpinner(tags: List<Tag>) {
        val allOption = Tag().apply { name = "Все теги"; id = -1L }
        val allTags = listOf(allOption) + tags

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            allTags.map { it.name }
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterTags.adapter = spinnerAdapter

        spinnerFilterTags.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setTagFilter(if (allTags[position].id == -1L) null else allTags[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setTagFilter(null)
            }
        }
    }

    private fun setupFab() {
        fabAddTag.setOnClickListener {
            TagManagementDialog(requireContext()).showCreateTagDialog { name ->
                viewModel.createTag(name)
            }
        }
    }
}
