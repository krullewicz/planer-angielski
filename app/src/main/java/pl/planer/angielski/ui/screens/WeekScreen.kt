package pl.planer.angielski.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.planer.angielski.PlannerViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.data.DayEntry
import pl.planer.angielski.data.WeekPlan
import pl.planer.angielski.ui.NoteField
import pl.planer.angielski.ui.PeriodSwitcher
import pl.planer.angielski.ui.PlannerCard
import pl.planer.angielski.ui.StatTile
import pl.planer.angielski.ui.TaskList
import pl.planer.angielski.ui.theme.Accents
import pl.planer.angielski.ui.theme.Coral
import pl.planer.angielski.ui.theme.Lilac
import pl.planer.angielski.ui.theme.Mint
import pl.planer.angielski.ui.theme.Sky
import pl.planer.angielski.ui.theme.Sun
import pl.planer.angielski.weekStart
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeekScreen(data: AppData, vm: PlannerViewModel) {
    var monday by rememberSaveable { mutableStateOf(LocalDate.now().weekStart()) }
    val week = data.weeks[monday.toString()] ?: WeekPlan()
    val days = (0L..6L).map { monday.plusDays(it) }
    val entries = days.map { data.days[it.toString()] ?: DayEntry() }
    val totalMinutes = entries.sumOf { it.minutes }
    val activeDays = entries.count { it.minutes > 0 || it.habitsDone.isNotEmpty() }
    val fmt = DateTimeFormatter.ofPattern("d MMM", PL)
    var expanded by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PeriodSwitcher(
            title = "Tydzień",
            subtitle = "${monday.format(fmt)} – ${monday.plusDays(6).format(fmt)}",
            onPrev = { monday = monday.minusWeeks(1) },
            onNext = { monday = monday.plusWeeks(1) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("$totalMinutes min", "czas nauki", Coral, Modifier.weight(1f))
            StatTile("$activeDays / 7", "aktywne dni", Mint, Modifier.weight(1f))
            StatTile(
                "${week.goals.count { it.done }} / ${week.goals.size}", "cele", Sky, Modifier.weight(1f),
            )
        }

        // Wykres minut z tygodnia
        PlannerCard("Minuty dzień po dniu", Sun) {
            val max = (entries.maxOf { it.minutes }).coerceAtLeast(data.settings.dailyGoalMinutes).toFloat()
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEachIndexed { i, d ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                            val minutes = entries[i].minutes
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .fillMaxHeight((minutes / max).coerceIn(0.02f, 1f))
                                    .background(
                                        if (minutes >= data.settings.dailyGoalMinutes) Coral else Coral.copy(alpha = 0.35f),
                                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                    )
                            )
                        }
                        Text(
                            DAY_SHORT[i],
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (d == LocalDate.now()) FontWeight.Bold else null,
                        )
                    }
                }
            }
        }

        PlannerCard("Cele na ten tydzień", Coral) {
            TaskList(
                tasks = week.goals,
                placeholder = "Np. nauczyć się 30 słówek",
                onAdd = { vm.addWeekGoal(monday, it) },
                onToggle = { vm.toggleWeekGoal(monday, it) },
                onDelete = { vm.deleteWeekGoal(monday, it) },
            )
        }

        PlannerCard("Plan tygodnia", Lilac) {
            days.forEachIndexed { i, d ->
                val e = entries[i]
                val isOpen = expanded == d.toString()
                Column(Modifier.animateContentSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = if (isOpen) "" else d.toString() }
                            .padding(vertical = 10.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                d.format(DateTimeFormatter.ofPattern("EEEE, d MMM", PL)).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (d == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            val done = e.tasks.count { it.done }
                            Text(
                                buildString {
                                    append("${e.minutes} min")
                                    if (e.tasks.isNotEmpty()) append(" · zadania $done/${e.tasks.size}")
                                    if (e.mood != null) append(" · ${MOODS[e.mood]}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        // Kropki wykonanych nawyków
                        data.settings.habits.forEachIndexed { h, habit ->
                            if (habit in e.habitsDone) {
                                Box(
                                    Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(8.dp)
                                        .background(Accents[h % Accents.size], CircleShape)
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    if (isOpen) {
                        TaskList(
                            tasks = e.tasks,
                            placeholder = "Dodaj zadanie",
                            onAdd = { vm.addDayTask(d, it) },
                            onToggle = { vm.toggleDayTask(d, it) },
                            onDelete = { vm.deleteDayTask(d, it) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (i < 6) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        PlannerCard("Podsumowanie tygodnia", Mint) {
            NoteField(
                value = week.reflection,
                key = monday,
                label = "Co poszło dobrze? Co poprawię w przyszłym tygodniu?",
                onChange = { vm.setWeekReflection(monday, it) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
