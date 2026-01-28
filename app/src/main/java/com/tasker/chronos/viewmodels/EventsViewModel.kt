// Plik: viewmodels/EventsViewModel.kt
package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tasker.chronos.data.models.CalendarEvent
import com.tasker.chronos.data.models.CustomEvent
import com.tasker.chronos.data.models.EventType
import com.tasker.chronos.data.repository.EventsRepository
import com.tasker.chronos.data.repository.CustomEventsRepository
import com.tasker.chronos.notifications.CustomEventScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EventsRepository(application)
    private val customEventsRepository = CustomEventsRepository(application)



    // Aktualnie wybrany dzień
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // Aktualnie wyświetlany miesiąc
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    // Wydarzenia z nawyków, zadań, celów
    val allEvents: StateFlow<List<CalendarEvent>> = repository.getAllCalendarEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Własne wydarzenia (custom z blokami czasowymi)
    val customEvents: StateFlow<List<CustomEvent>> = customEventsRepository.getAllCustomEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Wydarzenia dla wybranego dnia (połączone)
    val eventsForSelectedDay: StateFlow<List<CalendarEvent>> = combine(
        _selectedDate,
        allEvents
    ) { date, events ->
        events.filter { it.date == date.toString() }
            .sortedBy { it.startTime ?: "00:00" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Custom wydarzenia dla wybranego dnia
    val customEventsForSelectedDay: StateFlow<List<CustomEvent>> = combine(
        _selectedDate,
        customEvents
    ) { date, events ->
        android.util.Log.d("EventsViewModel", "📅 Filtrowanie dla daty: $date")
        android.util.Log.d("EventsViewModel", "📊 Wszystkie custom events: ${events.size}")

        val filtered = events.filter { it.date == date.toString() }
            .sortedBy { it.startTime }

        android.util.Log.d("EventsViewModel", "✅ Przefiltrowane: ${filtered.size}")
        filtered.forEach {
            android.util.Log.d("EventsViewModel", "  - ${it.title}: ${it.startTime}-${it.endTime}")
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mapa: dzień -> lista typów wydarzeń (dla kolorowych kropek)
    val eventTypesPerDay: StateFlow<Map<String, List<EventType>>> = allEvents
        .map { events ->
            events.groupBy { it.date }
                .mapValues { (_, dayEvents) ->
                    dayEvents.map { it.type }.distinct()
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // === CUSTOM WYDARZENIA - CRUD ===

    fun addCustomEvent(event: CustomEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("EventsViewModel", "════════════════════════════════")
            android.util.Log.d("EventsViewModel", "➕ addCustomEvent wywołany")
            android.util.Log.d("EventsViewModel", "➕ Event: ${event.title}")
            android.util.Log.d("EventsViewModel", "➕ Data: ${event.date}")
            android.util.Log.d(
                "EventsViewModel",
                "➕ Start: ${event.startTime}, End: ${event.endTime}"
            )

            customEventsRepository.addEvent(event)

            android.util.Log.d("EventsViewModel", "✅ Zapisano w repository")

            // ✅ NOWY SYSTEM: Zaplanuj START + opcjonalnie REMINDER
            CustomEventScheduler.schedule(getApplication(), event)
            android.util.Log.d("EventsViewModel", "🔔 Zaplanowano powiadomienia (START + REMINDER)")

            android.util.Log.d("EventsViewModel", "════════════════════════════════")
        }
    }

    fun updateCustomEvent(event: CustomEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("EventsViewModel", "════════════════════════════════")
            android.util.Log.d("EventsViewModel", "🔄 updateCustomEvent wywołany")
            android.util.Log.d("EventsViewModel", "🔄 Event ID: ${event.id}")
            android.util.Log.d("EventsViewModel", "🔄 Nowy tytuł: ${event.title}")
            android.util.Log.d(
                "EventsViewModel",
                "🔄 Nowy czas: ${event.startTime} - ${event.endTime}"
            )

            customEventsRepository.updateEvent(event)

            android.util.Log.d("EventsViewModel", "✅ Zaktualizowano w repository")

            // ✅ NOWY SYSTEM: Anuluj stare + zaplanuj nowe
            CustomEventScheduler.reschedule(getApplication(), event)
            android.util.Log.d("EventsViewModel", "🔔 Zaktualizowano powiadomienia")

            delay(100)
            val updated = customEvents.value.find { it.id == event.id }
            android.util.Log.d(
                "EventsViewModel",
                "🔍 Sprawdzenie: ${updated?.startTime} - ${updated?.endTime}"
            )
            android.util.Log.d("EventsViewModel", "════════════════════════════════")
        }
    }

    fun deleteCustomEvent(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // ✅ ZNAJDŹ WYDARZENIE PRZED USUNIĘCIEM
            val event = customEvents.value.find { it.id == eventId }

            customEventsRepository.deleteEvent(eventId)

            // ✅ NOWY SYSTEM: Anuluj wszystkie powiadomienia
            event?.let {
                CustomEventScheduler.cancel(getApplication(), it)
                android.util.Log.d(
                    "EventsViewModel",
                    "🗑️ Usunięto wydarzenie + anulowano powiadomienia: ${it.title}"
                )
            }
        }
    }

    // === NAWIGACJA ===

    fun selectDate(date: LocalDate) {
        android.util.Log.d("EventsViewModel", "📅 Wybrano datę: $date")
        _selectedDate.value = date
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _selectedMonth.value = YearMonth.from(today)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }


    /**
     * ✅ NOWE: Usuń wszystkie wydarzenia pochodzące z danego celu
     */
    // ✅ NOWY KOD:
    // ✅ ZNAJDŹ I ZASTĄP TĘ FUNKCJĘ:
    // ✅ POPRAWNA WERSJA:
    // ✅ POPRAWIONA WERSJA:
    // ✅ POPRAWIONA WERSJA (zmień TYLKO tę linię):
    // ✅ ZAMIEŃ deleteEventsBySourceGoalId NA TO:
    fun deleteEventsBySourceGoalId(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("EventsViewModel", "🔍 Szukam wydarzeń dla celu: $goalId")

            // ✅ ZMIANA: Pobierz ŚWIEŻE dane z DataStore zamiast z Flow
            val freshEvents = customEventsRepository.getAllCustomEvents().first()  // ✅ DODAJ .first()

            val eventsToDelete = freshEvents.filter { event ->
                event.sourceGoalId == goalId
            }

            android.util.Log.d("EventsViewModel", "📊 Znaleziono: ${eventsToDelete.size} wydarzeń")

            eventsToDelete.forEach { event ->
                customEventsRepository.deleteEvent(event.id)
                android.util.Log.d("EventsViewModel", "   🗑️ Usunięto: ${event.title}")
            }

            android.util.Log.d("EventsViewModel", "✅ Usunięto ${eventsToDelete.size} wydarzeń z celu: $goalId")
        }
    }
}