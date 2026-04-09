package com.example.mobdev_lab3.repository

import com.example.mobdev_lab3.model.Note

/**
 * Интерфейс для всех типов хранилищ заметок.
 * Определяет контракт CRUD операций.
 */
interface NoteStorage {
    /**
     * Создать новую заметку
     * @param note заметка для создания
     * @return Result с созданной заметкой или ошибкой
     */
    suspend fun create(note: Note): Result<Note>
    
    /**
     * Прочитать заметку по ID
     * @param id идентификатор заметки
     * @return Result с заметкой или null если не найдена
     */
    suspend fun read(id: String): Result<Note?>
    
    /**
     * Обновить существующую заметку
     * @param note заметка с обновленными данными
     * @return Result с обновленной заметкой или ошибкой
     */
    suspend fun update(note: Note): Result<Note>
    
    /**
     * Удалить заметку по ID
     * @param id идентификатор заметки
     * @return Result с true если удалено успешно
     */
    suspend fun delete(id: String): Result<Boolean>
    
    /**
     * Получить все заметки
     * @return Result со списком всех заметок
     */
    suspend fun getAll(): Result<List<Note>>
    
    /**
     * Получить информацию о хранилище (тип и путь)
     * @return строка с информацией о хранилище
     */
    fun getStorageInfo(): String
}
