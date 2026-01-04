package app.olauncher.data.repository

import androidx.lifecycle.LiveData
import app.olauncher.data.local.NoteEntity
import app.olauncher.data.local.ProductivityDao
import app.olauncher.data.local.TaskEntity

class ProductivityRepository(private val dao: ProductivityDao) {

    val allNotes: LiveData<List<NoteEntity>> = dao.getAllNotes()
    val allTasks: LiveData<List<TaskEntity>> = dao.getAllTasks()

    suspend fun insertNote(note: NoteEntity) {
        dao.insertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        dao.deleteNote(note)
    }

    suspend fun insertTask(task: TaskEntity) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteTask(task)
    }
}
