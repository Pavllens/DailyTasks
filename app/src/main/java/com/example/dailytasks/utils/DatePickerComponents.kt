package com.example.dailytasks.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.ui.Modifier

class DatePickerComponents {
    @Composable
    fun DatePickerDialog(onDateSelected: (LocalDate) -> Unit) {
        val dateState = remember { mutableStateOf(LocalDate.now()) }
        val openDatePicker = remember { mutableStateOf(false) }

        Box(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { openDatePicker.value = true }) {
                Text(text = "Select Date")
            }
            if (openDatePicker.value) {
                DatePicker(onDateSelected = {
                    dateState.value = it
                    onDateSelected(it)
                    openDatePicker.value = false
                })
            }
        }
    }

    @Composable
    fun DatePicker(onDateSelected: (LocalDate) -> Unit) {
        Button(onClick = { onDateSelected(LocalDate.now()) }) {
            Text(text = "Select Today's Date (replace with actual picker)")
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DatePickerDialogPreview() {
        DatePickerDialog(onDateSelected = { })
    }
}