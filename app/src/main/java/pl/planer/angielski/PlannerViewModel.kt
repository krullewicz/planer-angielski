package pl.planer.angielski

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.data.DayEntry
import pl.planer.angielski.data.LEITNER_INTERVALS
import pl.planer.angielski.data.MonthPlan
import pl.planer.angielski.data.Repository
import pl.planer.angielski.data.Settings
import pl.planer.angielski.data.TaskItem
import pl.planer.angielski.data.WeekPlan
import pl.planer.angielski.data.Word
import pl.planer.angielski.reminder.Reminders
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private fun newId() = System.nanoTime()

fun List<TaskItem>.plusTask(text: String) =
    if (text.isBlank()) this else this + TaskItem(newId(), text.trim())

fun List<TaskItem>.toggled(id: Long) = map { if (it.id == id) it.copy(done = !it.done) else it }
fun List<TaskItem>.removed(id: Long) = filterNot { it.id == id }

/** Kolejne dni nauki zakończone dziś (lub wczoraj, jeśli dziś jeszcze nic nie zrobiono). */
fun AppData.streak(today: LocalDate = LocalDate.now()): Int {
    fun active(d: LocalDate) = days[d.toString()]?.let { it.minutes > 0 || it.habitsDone.isNotEmpty() } == true
    var day = if (active(today)) today else today.minusDays(1)
    var count = 0
    while (active(day)) {
        count++
        day = day.minusDays(1)
    }
    return count
}

fun AppData.dueWords(today: LocalDate = LocalDate.now()): List<Word> =
    words.filter { LocalDate.parse(it.nextReview) <= today }

class PlannerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository.get(app)
    val data = repo.data

    // --- Dzień ---

    private fun updateDay(date: LocalDate, transform: (DayEntry) -> DayEntry) = repo.update { d ->
        val key = date.toString()
        d.copy(days = d.days + (key to transform(d.days[key] ?: DayEntry())))
    }

    fun addMinutes(date: LocalDate, delta: Int) = updateDay(date) { it.copy(minutes = (it.minutes + delta).coerceAtLeast(0)) }
    fun toggleHabit(date: LocalDate, habit: String) = updateDay(date) {
        it.copy(habitsDone = if (habit in it.habitsDone) it.habitsDone - habit else it.habitsDone + habit)
    }
    fun setMood(date: LocalDate, mood: Int) = updateDay(date) { it.copy(mood = if (it.mood == mood) null else mood) }
    fun setNote(date: LocalDate, note: String) = updateDay(date) { it.copy(note = note) }
    fun toggleChallenge(date: LocalDate) = updateDay(date) { it.copy(challengeDone = !it.challengeDone) }
    fun addDayTask(date: LocalDate, text: String) = updateDay(date) { it.copy(tasks = it.tasks.plusTask(text)) }
    fun toggleDayTask(date: LocalDate, id: Long) = updateDay(date) { it.copy(tasks = it.tasks.toggled(id)) }
    fun deleteDayTask(date: LocalDate, id: Long) = updateDay(date) { it.copy(tasks = it.tasks.removed(id)) }

    // --- Tydzień ---

    private fun updateWeek(date: LocalDate, transform: (WeekPlan) -> WeekPlan) = repo.update { d ->
        val key = date.weekStart().toString()
        d.copy(weeks = d.weeks + (key to transform(d.weeks[key] ?: WeekPlan())))
    }

    fun addWeekGoal(date: LocalDate, text: String) = updateWeek(date) { it.copy(goals = it.goals.plusTask(text)) }
    fun toggleWeekGoal(date: LocalDate, id: Long) = updateWeek(date) { it.copy(goals = it.goals.toggled(id)) }
    fun deleteWeekGoal(date: LocalDate, id: Long) = updateWeek(date) { it.copy(goals = it.goals.removed(id)) }
    fun setWeekReflection(date: LocalDate, text: String) = updateWeek(date) { it.copy(reflection = text) }

    // --- Miesiąc ---

    private fun updateMonth(month: YearMonth, transform: (MonthPlan) -> MonthPlan) = repo.update { d ->
        val key = month.toString()
        d.copy(months = d.months + (key to transform(d.months[key] ?: MonthPlan())))
    }

    fun setMonthFocus(month: YearMonth, text: String) = updateMonth(month) { it.copy(focus = text) }
    fun addMonthGoal(month: YearMonth, text: String) = updateMonth(month) { it.copy(goals = it.goals.plusTask(text)) }
    fun toggleMonthGoal(month: YearMonth, id: Long) = updateMonth(month) { it.copy(goals = it.goals.toggled(id)) }
    fun deleteMonthGoal(month: YearMonth, id: Long) = updateMonth(month) { it.copy(goals = it.goals.removed(id)) }
    fun setMonthReflection(month: YearMonth, text: String) = updateMonth(month) { it.copy(reflection = text) }

    // --- Słówka ---

    fun addWord(english: String, polish: String, example: String = "") {
        if (english.isBlank() || polish.isBlank()) return
        val today = LocalDate.now().toString()
        repo.update { d ->
            if (d.words.any { it.english.equals(english.trim(), ignoreCase = true) }) d
            else d.copy(words = listOf(Word(newId(), english.trim(), polish.trim(), example.trim(), 1, today, today)) + d.words)
        }
    }

    fun deleteWord(id: Long) = repo.update { d -> d.copy(words = d.words.filterNot { it.id == id }) }

    /** Znane słówko trafia do kolejnego pudełka, nieznane wraca do pierwszego. */
    fun reviewWord(id: Long, known: Boolean) = repo.update { d ->
        d.copy(words = d.words.map { w ->
            if (w.id != id) return@map w
            val box = if (known) (w.box + 1).coerceAtMost(LEITNER_INTERVALS.size) else 1
            val days = if (known) LEITNER_INTERVALS[box - 1].toLong() else 0L
            w.copy(box = box, nextReview = LocalDate.now().plusDays(days).toString())
        })
    }

    // --- Ustawienia ---

    private fun updateSettings(transform: (Settings) -> Settings) = repo.update { it.copy(settings = transform(it.settings)) }

    fun setDailyGoal(minutes: Int) = updateSettings { it.copy(dailyGoalMinutes = minutes.coerceIn(5, 240)) }

    fun addHabit(name: String) = updateSettings {
        if (name.isBlank() || name.trim() in it.habits) it else it.copy(habits = it.habits + name.trim())
    }

    fun removeHabit(name: String) = updateSettings { it.copy(habits = it.habits - name) }

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        updateSettings { it.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute) }
        val ctx = getApplication<Application>()
        if (enabled) Reminders.schedule(ctx, hour, minute) else Reminders.cancel(ctx)
    }

    fun exportJson(): String = repo.exportJson()
    fun importJson(text: String): Boolean {
        if (!repo.importJson(text)) return false
        val s = data.value.settings
        setReminder(s.reminderEnabled, s.reminderHour, s.reminderMinute)
        return true
    }
}
