package com.example.dailytasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.Task
import com.example.dailytasks.viewmodel.TaskViewModel
import com.example.dailytasks.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val taskDao = database.taskDao()

    val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskDao))
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(selectedDate) {
        viewModel.loadTasksForDay(selectedDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Tasks") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                taskToEdit = null
                showDialog = true
            }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            DateSelector(selectedDate) { newDate ->
                selectedDate = newDate
            }
            Spacer(modifier = Modifier.height(16.dp))
            TaskList(tasks,
                onTaskClick = { task ->
                    val intent = Intent(context, TaskDetailActivity::class.java).apply {
                        putExtra("task_name", task.name)
                        putExtra("task_description", task.description)
                        putExtra("task_date_start", task.dateStart.toString())
                        putExtra("task_date_end", task.dateFinish.toString())
                    }
                    context.startActivity(intent)
                },
                onEditTask = { task ->
                    taskToEdit = task
                    showDialog = true
                },
                onDeleteTask = { task ->
                    viewModel.deleteTask(task)
                }
            )
        }

        if (showDialog) {
            AddTaskDialog(
                task = taskToEdit,
                onDismiss = { showDialog = false },
                onAddOrUpdateTask = { title, description, startDate, endDate ->
                    taskToEdit?.let {
                        viewModel.updateTask(it.copy(
                            name = title,
                            description = description,
                            dateStart = startDate,
                            dateFinish = endDate
                        ))
                    } ?: run {
                        viewModel.addTask(
                            Task(
                                name = title,
                                description = description,
                                dateStart = startDate,
                                dateFinish = endDate
                            )
                        )
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun DateSelector(selectedDate: LocalDate, onDateChanged: (LocalDate) -> Unit) {
    val context = LocalContext.current
    Button(onClick = {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val newDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                onDateChanged(newDate)
            },
            year, month, day
        ).show()
    }) {
        Text(text = "Select Date: $selectedDate")
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Column {
        for (task in tasks) {
            TaskItem(task, onTaskClick, onEditTask, onDeleteTask)
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTaskClick(task) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = task.name)
                Text(text = "Time: ${task.dateStart.toLocalTime()} - ${task.dateFinish.toLocalTime()}")
            }
            Row {
                TextButton(onClick = { onEditTask(task) }) {
                    Text("Edit")
                }
                TextButton(onClick = { onDeleteTask(task) }) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onAddOrUpdateTask: (String, String, LocalDateTime, LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(task?.dateStart?.toLocalDate() ?: LocalDate.now()) }
    var startTime by remember { mutableStateOf(task?.dateStart?.toLocalTime() ?: LocalTime.of(12, 0)) }
    var endTime by remember { mutableStateOf(task?.dateFinish?.toLocalTime() ?: LocalTime.of(13, 0)) }
    var currentTitle by remember { mutableStateOf(task?.name ?: "") }
    var currentDescription by remember { mutableStateOf(task?.description ?: "") }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
            },
            selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task?.let { "Edit Task" } ?: "Add New Task") },
        text = {
            Column {
                TextField(
                    value = currentTitle,
                    onValueChange = { currentTitle = it },
                    label = { Text("Task Title") },
                    singleLine = true
                )
                TextField(
                    value = currentDescription,
                    onValueChange = { currentDescription = it },
                    label = { Text("Description") }
                )
                Button(onClick = { showDatePicker() }) {
                    Text("Date: $selectedDate")
                }
                TimePickerComponent("Start Time", startTime) { time ->
                    startTime = time
                }
                TimePickerComponent("End Time", endTime) { time ->
                    endTime = time
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val startDateTime = LocalDateTime.of(selectedDate, startTime)
                val endDateTime = LocalDateTime.of(selectedDate, endTime)
                onAddOrUpdateTask(currentTitle, currentDescription, startDateTime, endDateTime)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePickerComponent(title: String, initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    var time by remember { mutableStateOf(initialTime) }

    Button(onClick = {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newTime = LocalTime.of(hourOfDay, minute)
                time = newTime
                onTimeSelected(newTime)
            },
            initialTime.hour,
            initialTime.minute,
            true
        ).show()
    }) {
        Text("$title: $time")
    }
}