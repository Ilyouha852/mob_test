package com.example.mobdev_lab3.presentation.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.FileDetailActivity
import com.example.mobdev_lab3.FileManagementActivity
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode
import com.example.mobdev_lab3.presentation.settings.SettingsViewModel
import com.example.mobdev_lab3.viewmodel.FileManagerViewModel

@Suppress("DEPRECATION")
class FileManagerFragment : Fragment() {

    private lateinit var radioByName: RadioButton
    private lateinit var radioByType: RadioButton
    private lateinit var radioByDate: RadioButton
    private lateinit var textViewFiles: TextView
    private lateinit var listViewFiles: ListView
    private lateinit var buttonUp: Button
    private lateinit var buttonHome: Button
    private lateinit var buttonFileManagement: Button

    private val viewModel: FileManagerViewModel by viewModels({ requireActivity() })
    private val settingsViewModel: SettingsViewModel by viewModels({ requireActivity() })

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val EXTRA_CURRENT_PATH = "current_path"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_file_manager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioByName = view.findViewById(R.id.radioByName)
        radioByType = view.findViewById(R.id.radioByType)
        radioByDate = view.findViewById(R.id.radioByDate)
        textViewFiles = view.findViewById(R.id.textViewFiles)
        listViewFiles = view.findViewById(R.id.listViewFiles)
        buttonUp = view.findViewById(R.id.buttonUp)
        buttonHome = view.findViewById(R.id.buttonHome)
        buttonFileManagement = view.findViewById(R.id.buttonFileManagement)

        setupUI()
        setupObservers()
        applySettings()

        if (checkPermissions()) {
            val currentPath = arguments?.getString(EXTRA_CURRENT_PATH)
            if (currentPath != null) viewModel.setCurrentPath(currentPath)
            else viewModel.loadFiles()
        } else {
            requestStoragePermission()
        }
    }

    private fun setupUI() {
        radioByName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSortMode(SortMode.BY_NAME)
        }
        radioByType.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSortMode(SortMode.BY_TYPE)
        }
        radioByDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSortMode(SortMode.BY_DATE)
        }

        buttonUp.setOnClickListener { viewModel.goUpDirectory() }
        buttonHome.setOnClickListener { viewModel.goToHomeDirectory() }

        buttonFileManagement.setOnClickListener {
            val intent = Intent(requireContext(), FileManagementActivity::class.java)
            intent.putExtra(FileManagementActivity.EXTRA_CURRENT_PATH, viewModel.getCurrentPath())
            startActivityForResult(intent, 1003)
        }

        listViewFiles.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val files = viewModel.files.value ?: return@OnItemClickListener
            if (position < files.size) {
                val file = files[position]
                if (file.isDirectory) viewModel.navigateToDirectory(file.path)
                else openFileDetail(file)
            }
        }

        listViewFiles.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val files = viewModel.files.value ?: return@OnItemLongClickListener false
            if (position < files.size) { openFileDetail(files[position]); true } else false
        }

        radioByName.isChecked = true
    }

    private fun setupObservers() {
        viewModel.files.observe(viewLifecycleOwner) { updateFileList() }
        viewModel.sortMode.observe(viewLifecycleOwner) { updateFileList() }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) textViewFiles.text = "Загрузка файлов..."
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { err ->
            if (err != null) textViewFiles.text = err
        }

        settingsViewModel.settings.observe(viewLifecycleOwner) { settings ->
            viewModel.setShowHidden(settings.showHidden)
            viewModel.setShowSystemFiles(settings.showSystemFiles)
            when (settings.sortMode) {
                SortMode.BY_NAME -> radioByName.isChecked = true
                SortMode.BY_TYPE -> radioByType.isChecked = true
                SortMode.BY_DATE -> radioByDate.isChecked = true
            }
        }
    }

    private fun applySettings() {
        settingsViewModel.reload()
    }

    private fun updateFileList() {
        val files = viewModel.files.value ?: return
        textViewFiles.text = "Путь: ${viewModel.getCurrentPath()}\nНайдено файлов: ${files.size}\n"
        val fileNames = files.map { f ->
            val icon = if (f.isDirectory) "📁" else "📄"
            "$icon ${f.name}\n   ${f.getFormattedSize()} | ${f.getFormattedDate()}"
        }
        listViewFiles.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, fileNames)
    }

    private fun openFileDetail(file: FileItem) {
        val intent = Intent(requireContext(), FileDetailActivity::class.java)
        intent.putExtra(FileDetailActivity.EXTRA_FILE_PATH, file.path)
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:${requireContext().packageName}".toUri()
                startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            } catch (_: Exception) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE && checkPermissions()) {
            viewModel.loadFiles()
        }
        if (requestCode == 1003) viewModel.loadFiles()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadFiles()
        }
    }
}
