package com.example.mobdev_lab3.presentation.bookmarks

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.adapter.BookmarkAdapter
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class BookmarksFragment : Fragment() {

    private val viewModel: BookmarksViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: BookmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_bookmarks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewBookmarks)
        fabAdd = view.findViewById(R.id.fabAddBookmark)

        adapter = BookmarkAdapter(
            bookmarks = mutableListOf(),
            onItemClick = { bookmark -> openBookmark(bookmark) },
            onDeleteClick = { bookmark -> confirmDelete(bookmark) },
            onEditClick = { bookmark -> showEditDialog(bookmark) }
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            BookmarksDialogHelper(requireContext()).showAddBookmarkDialog {
                viewModel.loadBookmarks()
            }
        }

        viewModel.bookmarks.observe(viewLifecycleOwner) { list ->
            adapter.updateBookmarks(list)
        }
    }

    private fun openBookmark(bookmark: FileBookmark) {
        val parentPath = if (bookmark.isDirectory) bookmark.path
        else File(bookmark.path).parent ?: bookmark.path

        requireActivity().supportFragmentManager.apply {
            val fragment = com.example.mobdev_lab3.presentation.filemanager.FileManagerFragment().apply {
                arguments = Bundle().apply {
                    putString(com.example.mobdev_lab3.presentation.filemanager.FileManagerFragment.EXTRA_CURRENT_PATH, parentPath)
                }
            }
            beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun confirmDelete(bookmark: FileBookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить закладку?")
            .setMessage("Вы уверены, что хотите удалить \"${bookmark.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteBookmark(bookmark)
                Toast.makeText(requireContext(), "Закладка удалена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditDialog(bookmark: FileBookmark) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bookmark, null)
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextName)
        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.editTextDescription)
        val spinnerColor = dialogView.findViewById<Spinner>(R.id.spinnerColor)
        val buttonSelectPath = dialogView.findViewById<android.widget.Button>(R.id.buttonSelectPath)
        dialogView.findViewById<TextView>(R.id.textViewTitle).text = "Редактировать закладку"
        dialogView.findViewById<TextView>(R.id.textViewPath).text = bookmark.path

        editTextName.setText(bookmark.name)
        editTextDescription.setText(bookmark.description)
        buttonSelectPath.visibility = View.GONE

        val colors = BookmarkColor.values().map { it.displayName }
        val arr = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colors)
        arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = arr
        spinnerColor.setSelection(bookmark.color.ordinal)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialogView.findViewById<android.widget.Button>(R.id.buttonSave).setOnClickListener {
            val name = editTextName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = FileBookmark(
                id = bookmark.id,
                name = name,
                path = bookmark.path,
                description = editTextDescription.text.toString().trim(),
                color = BookmarkColor.values()[spinnerColor.selectedItemPosition],
                createdDate = bookmark.createdDate,
                isDirectory = bookmark.isDirectory
            )
            viewModel.updateBookmark(bookmark, updated)
            Toast.makeText(requireContext(), "Закладка обновлена", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialogView.findViewById<android.widget.Button>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
