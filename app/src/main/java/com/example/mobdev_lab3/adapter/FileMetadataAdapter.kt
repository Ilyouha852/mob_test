package com.example.mobdev_lab3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.database.entity.FileMetadata

class FileMetadataAdapter(
    private var files: List<FileMetadata>,
    private var tagsMap: Map<Long, List<String>> = emptyMap(),
    private val onItemClick: (FileMetadata) -> Unit,
    private val onFavoriteClick: (FileMetadata) -> Unit,
    private val onItemLongClick: (FileMetadata) -> Unit,
    private val onDeleteClick: (FileMetadata) -> Unit
) : RecyclerView.Adapter<FileMetadataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.textViewFileName)
        val filePath: TextView = view.findViewById(R.id.textViewFilePath)
        val tags: TextView = view.findViewById(R.id.textViewTags)
        val favorite: ImageView = view.findViewById(R.id.imageViewFavorite)
        val delete: ImageView = view.findViewById(R.id.imageViewDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_metadata, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = file.fileName
        holder.filePath.text = file.filePath
        
        // Отображение тегов
        val tags = tagsMap[file.id] ?: emptyList()
        holder.tags.text = if (tags.isNotEmpty()) "Tags: ${tags.joinToString(", ")}" else "No tags"

        // Отображение избранного
        val favoriteIcon = if (file.isFavorite == true) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        holder.favorite.setImageResource(favoriteIcon)

        holder.itemView.setOnClickListener { onItemClick(file) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(file)
            true
        }
        holder.favorite.setOnClickListener { onFavoriteClick(file) }
        holder.delete.setOnClickListener { onDeleteClick(file) }
    }

    override fun getItemCount() = files.size

    fun updateData(newFiles: List<FileMetadata>, newTagsMap: Map<Long, List<String>>) {
        files = newFiles
        tagsMap = newTagsMap
        notifyDataSetChanged()
    }
}
