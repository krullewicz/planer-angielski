package pl.planer.angielski.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.planer.angielski.PlannerViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.data.Content
import pl.planer.angielski.data.DayEntry
import pl.planer.angielski.streak
import pl.planer.angielski.ui.NoteField
import pl.planer.angielski.ui.PeriodSwitcher
import pl.planer.angielski.ui.PlannerCard
import pl.planer.angielski.ui.TaskList
import pl.planer.angielski.ui.theme.Accents
import pl.planer.angielski.ui.theme.Coral
import pl.planer.angielski.ui.theme.Lilac
import pl.planer.angielski.ui.theme.Mint
import pl.planer.angielski.ui.theme.Navy
import pl.planer.angielski.ui.theme.Sky
import pl.planer.angielski.ui.theme.Sun
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val MOODS = listOf("😫", "😕", "😐", "🙂", "🤩")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(data: AppData, vm: PlannerViewModel) {
    var date by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    val day = data.days[date.toString()] ?: DayEntry()
    val content = Content.forDate(date)
    val goal = data.settings.dailyGoalMinutes

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PeriodSwitcher(
            title = date.format(DateTimeFormatter.ofPattern("EEEE", PL)).replaceFirstChar { it.uppercase() },
            subtitle = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", PL)) + if (date == today) " · dziś" else "",
            onPrev = { date = date.minusDays(1) },
            onNext = { date = date.plusDays(1) },
        )
        if (date != today) {
            TextButton(onClick = { date = today }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Wróć do dziś")
            }
        }

        // Motto dnia
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "„${content.motto.first}”",
                    style = MaterialTheme.typography.titleMedium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    content.motto.second,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        // Licznik minut
        PlannerCard("Czas nauki", Coral, trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Coral, modifier = Modifier.size(18.dp))
                val streak = data.streak()
                Text(" $streak ${if (streak == 1) "dzień" else "dni"} z rzędu", style = MaterialTheme.typography.bodySmall)
            }
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
                    CircularProgressIndicator(
                        progress = { (day.minutes / goal.toFloat()).coerceAtMost(1f) },
                        modifier = Modifier.size(96.dp),
                        strokeWidth = 9.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${day.minutes}", style = MaterialTheme.typography.headlineMedium)
                        Text("/ $goal min", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.size(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (day.minutes >= goal) "Cel dnia osiągnięty! 🎉" else "Zostało ${goal - day.minutes} min do celu",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 15, 30).forEach { m ->
                            FilledTonalButton(onClick = { vm.addMinutes(date, m) }) { Text("+$m") }
                        }
                        OutlinedButton(onClick = { vm.addMinutes(date, -5) }, enabled = day.minutes > 0) { Text("−5") }
                    }
                }
            }
        }

        // Tracker nawyków
        PlannerCard("Dziś ćwiczę", Mint) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data.settings.habits.forEachIndexed { i, habit ->
                    val selected = habit in day.habitsDone
                    FilterChip(
                        selected = selected,
                        onClick = { vm.toggleHabit(date, habit) },
                        label = { Text(habit) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accents[i % Accents.size].copy(alpha = 0.25f),
                        ),
                    )
                }
            }
        }

        // Słówko dnia
        val word = content.word
        val inVocabulary = data.words.any { it.english.equals(word.english, ignoreCase = true) }
        PlannerCard("Słówko dnia", Sky) {
            Text(word.english, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
            Text(word.polish, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(word.example, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = { vm.addWord(word.english, word.polish, word.example) }, enabled = !inVocabulary) {
                Text(if (inVocabulary) "W słowniczku ✓" else "Dodaj do słowniczka")
            }
        }

        // Zwrot dnia
        val phrase = content.phrase
        val phraseSaved = data.words.any { it.english.equals(phrase.english, ignoreCase = true) }
        PlannerCard("Zwrot dnia", Lilac) {
            Text(phrase.english, style = MaterialTheme.typography.titleLarge)
            Text(phrase.meaning, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(6.dp))
            Text(phrase.example, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { vm.addWord(phrase.english, phrase.meaning, phrase.example) }, enabled = !phraseSaved) {
                Text(if (phraseSaved) "W słowniczku ✓" else "Zapisz zwrot")
            }
        }

        // Mini-gramatyka
        PlannerCard("Mini-gramatyka", Navy) {
            Text(content.grammar.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(content.grammar.body, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
        }

        // Wyzwanie dnia
        PlannerCard("Wyzwanie dnia", Sun) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(content.challenge, modifier = Modifier.weight(1f))
                Checkbox(checked = day.challengeDone, onCheckedChange = { vm.toggleChallenge(date) })
            }
        }

        // Zadania
        PlannerCard("Plan na dziś", Coral) {
            TaskList(
                tasks = day.tasks,
                placeholder = "Np. 2 strony z podręcznika",
                onAdd = { vm.addDayTask(date, it) },
                onToggle = { vm.toggleDayTask(date, it) },
                onDelete = { vm.deleteDayTask(date, it) },
            )
        }

        // Nastrój i notatka
        PlannerCard("Jak mi dziś poszło?", Mint) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                MOODS.forEachIndexed { i, emoji ->
                    val selected = day.mood == i
                    Surface(
                        onClick = { vm.setMood(date, i) },
                        shape = CircleShape,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = if (selected) 30.sp else 24.sp) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            NoteField(
                value = day.note,
                key = date,
                label = "Notatki: nowe słowa, sukcesy, trudności…",
                onChange = { vm.setNote(date, it) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
