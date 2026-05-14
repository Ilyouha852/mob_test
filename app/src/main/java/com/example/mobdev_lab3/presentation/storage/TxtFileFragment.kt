package com.example.mobdev_lab3.presentation.storage

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.data.storage.NoteStorage
import com.example.mobdev_lab3.data.storage.TxtFileStorage
import java.io.File

// Фрагмент для работы с TXT файлом в app-specific storage
class TxtFileFragment : BaseStorageFragment() {
    
    override val storage: NoteStorage by lazy {
        TxtFileStorage(requireContext())
    }
    
    override fun setupOpenFileButton() {
        val button = view?.findViewById<Button>(R.id.openFileButton)
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
