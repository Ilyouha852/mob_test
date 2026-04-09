package com.example.mobdev_lab3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.model.FileBookmark

class BookmarkAdapter(
    private val bookmarks: MutableList<FileBookmark>,
    private val onItemClick: (FileBookmark) -> Unit,
    private val onDeleteClick: (FileBookmark) -> Unit,
    private val onEditClick: (FileBookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewIcon)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        private val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewPath: TextView = itemView.findViewById(R.id.textViewPath)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)

        fun bind(bookmark: FileBookmark) {
            // Установка иконки в зависимости от типа
            if (bookmark.isDirectory) {
                imageViewIcon.setImageResource(R.drawable.ic_home)
            } else {
                imageViewIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            }

            // Установка индикатора цвета
            val color = ContextCompat.getColor(itemView.context, bookmark.color.colorResId)
            viewColorIndicator.setBackgroundColor(color)

            // Установка текстовых полей
            textViewName.text = bookmark.name
            textViewPath.text = bookmark.getShortPath()
            textViewDescription.text = bookmark.description
            textViewDate.text = bookmark.getFormattedDate()

            // Слушатели нажатий
            itemView.setOnClickListener {
                onItemClick(bookmark)
            }

            // Долгое нажатие для контекстного меню
            itemView.setOnLongClickListener {
                onEditClick(bookmark)
                true
            }

            // Кнопка удаления
            buttonDelete.setOnClickListener {
                onDeleteClick(bookmark)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    override fun getItemCount(): Int = bookmarks.size

    fun addBookmark(bookmark: FileBookmark) {
        bookmarks.add(bookmark)
        notifyItemInserted(bookmarks.size - 1)
    }

    fun updateBookmark(position: Int, bookmark: FileBookmark) {
        bookmarks[position] = bookmark
        notifyItemChanged(position)
    }

    fun removeBookmark(position: Int) {
        bookmarks.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getBookmarkPosition(bookmark: FileBookmark): Int {
        return bookmarks.indexOfFirst { it.id == bookmark.id }
    }

    fun updateBookmarks(newBookmarks: List<FileBookmark>) {
        bookmarks.clear()
        bookmarks.addAll(newBookmarks)
        notifyDataSetChanged()
    }
}
