package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.CustomEvent
import com.tasker.chronos.data.models.DaySchedule
import com.tasker.chronos.data.models.ScheduleEvent
import com.tasker.chronos.data.repository.DayScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DayScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DayScheduleRepository(application)

    val allSchedules: StateFlow<List<DaySchedule>> = repository.getAllSchedules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSchedule(schedule: DaySchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: DaySchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSchedule(scheduleId)
        }
    }

    /**
     * Konwertuje schemat na listę CustomEvent dla danego dnia
     */
    fun applyScheduleToDay(
        schedule: DaySchedule,
        date: String,
        eventsViewModel: EventsViewModel
    ) {
        schedule.events.forEach { scheduleEvent ->
            eventsViewModel.addCustomEvent(
                CustomEvent(
                    id = UUID.randomUUID().toString(),
                    title = scheduleEvent.title,
                    description = scheduleEvent.description,
                    date = date,
                    startTime = scheduleEvent.startTime,
                    endTime = scheduleEvent.endTime,
                    color = scheduleEvent.color
                )
            )
        }
    }
}