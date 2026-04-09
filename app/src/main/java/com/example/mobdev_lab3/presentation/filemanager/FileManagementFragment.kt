package com.example.mobdev_lab3.presentation.filemanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.R
import java.io.File

class FileManagementFragment : Fragment() {

    companion object {
        private const val ARG_START_PATH = "start_path"

        fun newInstance(startPath: String = ""): FileManagementFragment {
            val fragment = FileManagementFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_START_PATH, startPath)
            }
            return fragment
        }
    }

    private lateinit var textViewCurrentPath: TextView
    private lateinit var buttonHome: Button
    private lateinit var buttonUp: Button
    private lateinit var buttonBack: Button
    private lateinit var buttonCreateFile: Button
    private lateinit var buttonCreateFolder: Button
    private lateinit var listViewFiles: ListView

    private val viewModel: FileManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_file_management, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewCurrentPath = view.findViewById(R.id.textViewCurrentPath)
        buttonHome = view.findViewById(R.id.buttonHome)
        buttonUp = view.findViewById(R.id.buttonUp)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonCreateFile = view.findViewById(R.id.buttonCreateFile)
        buttonCreateFolder = view.findViewById(R.id.buttonCreateFolder)
        listViewFiles = view.findViewById(R.id.listViewFiles)

        setupClickListeners()
        setupObservers()

        val startPath = arguments?.getString(ARG_START_PATH) ?: ""
        viewModel.initialize(startPath)
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updatePathDisplay(state.currentPath)
            updateFileList(state.files)
            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            when (event) {
                is FileManagementEvent.OperationSuccess ->
                    Toast.makeText(requireContext(), "Операция выполнена успешно", Toast.LENGTH_SHORT).show()
                is FileManagementEvent.Error ->
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
            viewModel.consumeEvent()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePathDisplay(path: String) {
        textViewCurrentPath.text = "Текущая папка: $path"
    }

    private fun updateFileList(files: List<File>) {
        val fileNames = files.map { file ->
            val icon = if (file.isDirectory) "📁" else "📄"
            "$icon ${file.name}"
        }
        listViewFiles.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, fileNames)
    }

    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonHome.setOnClickListener { viewModel.goHome() }
        buttonUp.setOnClickListener { viewModel.goUp() }

        buttonCreateFile.setOnClickListener {
            val input = EditText(requireContext())
            input.hint = "Введите имя файла"
            AlertDialog.Builder(requireContext())
                .setTitle("Создать файл")
                .setView(input)
                .setPositiveButton("Создать") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) viewModel.createFile(name)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        buttonCreateFolder.setOnClickListener {
            val input = EditText(requireContext())
            input.hint = "Введите имя папки"
            AlertDialog.Builder(requireContext())
                .setTitle("Создать папку")
                .setView(input)
                .setPositiveButton("Создать") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) viewModel.createDirectory(name)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        listViewFiles.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val files = viewModel.uiState.value?.files ?: return@OnItemClickListener
            if (position < files.size) {
                val file = files[position]
                if (file.isDirectory) viewModel.navigateTo(file.absolutePath)
                else showFileOptions(file)
            }
        }

        listViewFiles.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val files = viewModel.uiState.value?.files ?: return@OnItemLongClickListener false
            if (position < files.size) { showFileOptions(files[position]); true } else false
        }
    }

    private fun showFileOptions(file: File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Опции: ${file.name}")
            .setItems(arrayOf("Переименовать", "Удалить")) { _, which ->
                when (which) {
                    0 -> showRenameDialog(file)
                    1 -> showDeleteConfirmation(file)
                }
            }
            .show()
    }

    private fun showRenameDialog(file: File) {
        val input = EditText(requireContext())
        input.setText(file.name)
        AlertDialog.Builder(requireContext())
            .setTitle("Переименовать")
            .setView(input)
            .setPositiveButton("Переименовать") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != file.name) {
                    viewModel.renameFile(file.absolutePath, newName)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteConfirmation(file: File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить")
            .setMessage("Удалить «${file.name}»?")
            .setPositiveButton("Удалить") { _, _ -> viewModel.deleteFile(file.absolutePath) }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
