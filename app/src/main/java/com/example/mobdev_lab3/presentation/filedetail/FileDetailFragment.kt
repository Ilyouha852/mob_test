package com.example.mobdev_lab3.presentation.filedetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.data.repository.FileOperationsRepositoryImpl

class FileDetailFragment : Fragment() {

    companion object {
        private const val ARG_FILE_PATH = "file_path"

        fun newInstance(filePath: String): FileDetailFragment {
            val fragment = FileDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_FILE_PATH, filePath)
            }
            return fragment
        }
    }

    private lateinit var textViewFileName: TextView
    private lateinit var textViewFilePath: TextView
    private lateinit var textViewFileSize: TextView
    private lateinit var textViewFileType: TextView
    private lateinit var textViewFileDate: TextView
    private lateinit var textViewFileContent: TextView
    private lateinit var scrollViewContent: ScrollView
    private lateinit var buttonBack: Button
    private lateinit var buttonOpen: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonRename: Button
    private lateinit var buttonAddToBookmarks: Button
    private lateinit var buttonAddToDatabase: Button

    private val viewModel: FileDetailViewModel by viewModels()
    private val fileOpsRepo = FileOperationsRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_file_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewFileName = view.findViewById(R.id.textViewFileName)
        textViewFilePath = view.findViewById(R.id.textViewFilePath)
        textViewFileSize = view.findViewById(R.id.textViewFileSize)
        textViewFileType = view.findViewById(R.id.textViewFileType)
        textViewFileDate = view.findViewById(R.id.textViewFileDate)
        textViewFileContent = view.findViewById(R.id.textViewFileContent)
        scrollViewContent = view.findViewById(R.id.scrollViewContent)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonOpen = view.findViewById(R.id.buttonOpen)
        buttonDelete = view.findViewById(R.id.buttonDelete)
        buttonRename = view.findViewById(R.id.buttonRename)
        buttonAddToBookmarks = view.findViewById(R.id.buttonAddToBookmarks)
        buttonAddToDatabase = view.findViewById(R.id.buttonAddToDatabase)

        setupClickListeners()
        setupObservers()

        val filePath = arguments?.getString(ARG_FILE_PATH) ?: run {
            Toast.makeText(requireContext(), "Ошибка: путь к файлу не задан", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        viewModel.loadFile(filePath)
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            state.file?.let { file -> renderFileInfo(file) }
            textViewFileContent.text = state.content
            updateDatabaseButton(state.isInDatabase)
            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            when (event) {
                is FileDetailEvent.Deleted -> {
                    Toast.makeText(requireContext(), "Файл или папка удалены", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is FileDetailEvent.Renamed -> {
                    Toast.makeText(requireContext(), "Файл переименован", Toast.LENGTH_SHORT).show()
                }
                is FileDetailEvent.BookmarkAdded -> {
                    Toast.makeText(requireContext(), "Закладка добавлена", Toast.LENGTH_SHORT).show()
                }
                is FileDetailEvent.Error -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.consumeEvent()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderFileInfo(file: FileItem) {
        textViewFileName.text = "Имя: ${file.name}"
        textViewFilePath.text = "Путь: ${file.path}"
        textViewFileSize.text = "Размер: ${file.getFormattedSize()}"
        textViewFileType.text = "Тип: ${file.getFileType()}"
        textViewFileDate.text = "Дата изменения: ${file.getFormattedDate()}"
        buttonOpen.text = if (file.isDirectory) "Открыть папку" else "Открыть файл"
    }

    private fun updateDatabaseButton(isInDb: Boolean) {
        buttonAddToDatabase.text = if (isInDb) "Remove from Database" else "Add to Database"
    }

    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonOpen.setOnClickListener {
            val file = viewModel.uiState.value?.file ?: return@setOnClickListener
            if (file.isDirectory) {
                Toast.makeText(requireContext(), "Навигация: ${file.path}", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(
                        android.net.Uri.fromFile(java.io.File(file.path)),
                        fileOpsRepo.getMimeType(file.extension)
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonDelete.setOnClickListener {
            val file = viewModel.uiState.value?.file ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("Удалить файл")
                .setMessage("Вы уверены, что хотите удалить «${file.name}»?")
                .setPositiveButton("Удалить") { _, _ -> viewModel.deleteFile() }
                .setNegativeButton("Отмена", null)
                .show()
        }

        buttonRename.setOnClickListener {
            val file = viewModel.uiState.value?.file ?: return@setOnClickListener
            val input = EditText(requireContext())
            input.setText(file.name)
            AlertDialog.Builder(requireContext())
                .setTitle("Переименовать файл")
                .setView(input)
                .setPositiveButton("Переименовать") { _, _ ->
                    val newName = input.text.toString().trim()
                    viewModel.renameFile(newName)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        buttonAddToBookmarks.setOnClickListener {
            val file = viewModel.uiState.value?.file ?: return@setOnClickListener
            BookmarksDialogHelper(requireContext()).showAddBookmarkDialog(
                initialPath = file.path,
                initialIsDir = file.isDirectory,
                initialName = file.name
            ) {
                Toast.makeText(requireContext(), "Закладка добавлена", Toast.LENGTH_SHORT).show()
            }
        }

        buttonAddToDatabase.setOnClickListener {
            viewModel.toggleDatabaseStatus()
        }
    }
}
