package com.example.dailytasks.utils

import java.time.LocalDate
import java.time.LocalDateTime

class DateUtils {

    fun getStartAndEndOfDay(selectedDate: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val startOfDay = selectedDate.atStartOfDay()
        val endOfDay = selectedDate.atTime(23, 59, 59)
        return Pair(startOfDay, endOfDay)
    }
}