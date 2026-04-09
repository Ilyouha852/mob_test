package com.example.mobdev_lab3.ui.storage

import android.content.Intent
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.example.mobdev_lab3.repository.CsvFileStorage
import com.example.mobdev_lab3.repository.NoteStorage

// Фрагмент для работы с CSV файлом в shared storage (Downloads)
class CsvFileFragment : BaseStorageFragment() {
    
    override val storage: NoteStorage by lazy {
        CsvFileStorage(requireContext())
    }
    
    override fun setupOpenFileButton() {
        val button = view?.findViewById<android.widget.Button>(com.example.mobdev_lab3.R.id.openFileButton)
        button?.visibility = View.VISIBLE
        button?.text = "Открыть в файловом менеджере"
        button?.setOnClickListener {
            openDownloadsFolder()
        }
    }
    
    private fun openDownloadsFolder() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "resource/folder"
                // Попытка открыть папку Downloads
                val downloadsPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).path
                setDataAndType(android.net.Uri.parse(downloadsPath), "resource/folder")
            }
            
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Альтернативный способ - открыть системный файловый менеджер
                val alternativeIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivity(Intent.createChooser(alternativeIntent, "Выберите файловый менеджер"))
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Файл notes.csv находится в папке Downloads",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
