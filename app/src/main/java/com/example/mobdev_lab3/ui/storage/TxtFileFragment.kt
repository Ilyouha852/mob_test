package com.example.mobdev_lab3.ui.storage

import android.app.AlertDialog
import android.view.View
import com.example.mobdev_lab3.repository.NoteStorage
import com.example.mobdev_lab3.repository.TxtFileStorage
import java.io.File

// Фрагмент для работы с TXT файлом в app-specific storage
class TxtFileFragment : BaseStorageFragment() {
    
    override val storage: NoteStorage by lazy {
        TxtFileStorage(requireContext())
    }
    
    override fun setupOpenFileButton() {
        val button = view?.findViewById<android.widget.Button>(com.example.mobdev_lab3.R.id.openFileButton)
        button?.visibility = View.VISIBLE
        button?.setOnClickListener {
            showFileContent()
        }
    }
    
    private fun showFileContent() {
        val file = File(requireContext().filesDir, "notes.txt")
        
        val content = if (file.exists()) {
            file.readText()
        } else {
            "Файл пока не создан"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Содержимое notes.txt")
            .setMessage(content)
            .setPositiveButton("OK", null)
            .show()
    }
}
