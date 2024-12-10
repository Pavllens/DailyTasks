package com.example.dailytasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class TaskDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskName = intent.getStringExtra("task_name") ?: "No Name"
        val taskDescription = intent.getStringExtra("task_description") ?: "No Description"
        val dateStart = intent.getStringExtra("task_date_start") ?: ""
        val dateEnd = intent.getStringExtra("task_date_end") ?: ""

        setContent {
            TaskDetailDialog(
                taskName = taskName,
                taskDescription = taskDescription,
                dateStart = dateStart,
                dateEnd = dateEnd,
                onDismissRequest = { finish() }
            )
        }
    }
}

@Composable
fun TaskDetailDialog(
    taskName: String,
    taskDescription: String,
    dateStart: String,
    dateEnd: String,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Task Details") },
        text = {
            Column {
                Text(text = "Title: $taskName")
                Text(text = "Description: $taskDescription")
                Text(text = "Start: $dateStart")
                Text(text = "End: $dateEnd")
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}