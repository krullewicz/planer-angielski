package pl.planer.angielski.data

import kotlinx.serialization.Serializable

/** Pojedyncze zadanie lub cel z polem wyboru. */
@Serializable
data class TaskItem(
    val id: Long,
    val text: String,
    val done: Boolean = false,
)

/** Wpis z jednego dnia nauki (klucz: data ISO, np. 2026-10-08). */
@Serializable
data class DayEntry(
    val minutes: Int = 0,
    val habitsDone: Set<String> = emptySet(),
    val mood: Int? = null,
    val note: String = "",
    val challengeDone: Boolean = false,
    val tasks: List<TaskItem> = emptyList(),
)

/** Plan tygodnia (klucz: data poniedziałku). */
@Serializable
data class WeekPlan(
    val goals: List<TaskItem> = emptyList(),
    val reflection: String = "",
)

/** Plan miesiąca (klucz: np. 2026-10). */
@Serializable
data class MonthPlan(
    val focus: String = "",
    val goals: List<TaskItem> = emptyList(),
    val reflection: String = "",
)

/** Słówko w osobistym słowniczku, powtarzane metodą pudełek Leitnera. */
@Serializable
data class Word(
    val id: Long,
    val english: String,
    val polish: String,
    val example: String = "",
    val box: Int = 1,
    val nextReview: String,
    val added: String,
)

@Serializable
data class Settings(
    val dailyGoalMinutes: Int = 15,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val habits: List<String> = DEFAULT_HABITS,
)

@Serializable
data class AppData(
    val days: Map<String, DayEntry> = emptyMap(),
    val weeks: Map<String, WeekPlan> = emptyMap(),
    val months: Map<String, MonthPlan> = emptyMap(),
    val words: List<Word> = emptyList(),
    val settings: Settings = Settings(),
)

val DEFAULT_HABITS = listOf("Słówka", "Słuchanie", "Czytanie", "Mówienie", "Pisanie", "Gramatyka")

/** Odstępy (w dniach) między powtórkami dla kolejnych pudełek 1..5. */
val LEITNER_INTERVALS = listOf(1, 2, 4, 8, 16)
