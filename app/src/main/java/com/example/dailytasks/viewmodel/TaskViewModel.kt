package com.example.dailytasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailytasks.data.Task
import com.example.dailytasks.data.TaskDao
import com.example.dailytasks.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
            loadTasksForDay(task.dateStart.toLocalDate())
        }
    }

    fun loadTasksForDay(selectedDate: LocalDate) {
        val (startOfDay, endOfDay) = DateUtils().getStartAndEndOfDay(selectedDate)
        viewModelScope.launch {
            val tasksForDay = taskDao.getTasksForDate(startOfDay, endOfDay)
            _tasks.value = tasksForDay
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
            loadTasksForDay(task.dateStart.toLocalDate())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
            loadTasksForDay(task.dateStart.toLocalDate())
        }
    }
}