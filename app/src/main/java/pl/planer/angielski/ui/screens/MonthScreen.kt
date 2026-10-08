package pl.planer.angielski.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.planer.angielski.PlannerViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.data.MonthPlan
import pl.planer.angielski.ui.NoteField
import pl.planer.angielski.ui.PeriodSwitcher
import pl.planer.angielski.ui.PlannerCard
import pl.planer.angielski.ui.StatTile
import pl.planer.angielski.ui.TaskList
import pl.planer.angielski.ui.theme.Accents
import pl.planer.angielski.ui.theme.Coral
import pl.planer.angielski.ui.theme.Lilac
import pl.planer.angielski.ui.theme.Mint
import pl.planer.angielski.ui.theme.Navy
import pl.planer.angielski.ui.theme.Sky
import pl.planer.angielski.ui.theme.Sun
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthScreen(data: AppData, vm: PlannerViewModel) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    val plan = data.months[month.toString()] ?: MonthPlan()
    val dates = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val entries = dates.associateWith { data.days[it.toString()] }
    val goal = data.settings.dailyGoalMinutes
    val today = LocalDate.now()
    val totalMinutes = entries.values.sumOf { it?.minutes ?: 0 }
    val activeDays = entries.values.count { it != null && (it.minutes > 0 || it.habitsDone.isNotEmpty()) }
    val newWords = data.words.count { YearMonth.from(LocalDate.parse(it.added)) == month }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PeriodSwitcher(
            title = month.format(DateTimeFormatter.ofPattern("LLLL", PL)).replaceFirstChar { it.uppercase() },
            subtitle = month.year.toString(),
            onPrev = { month = month.minusMonths(1) },
            onNext = { month = month.plusMonths(1) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(formatHours(totalMinutes), "czas nauki", Coral, Modifier.weight(1f))
            StatTile("$activeDays", "aktywne dni", Mint, Modifier.weight(1f))
            StatTile("$newWords", "nowe słówka", Sky, Modifier.weight(1f))
        }

        PlannerCard("Mój cel przewodni", Navy) {
            NoteField(
                value = plan.focus,
                key = month,
                label = "Np. „W tym miesiącu skupiam się na mówieniu”",
                onChange = { vm.setMonthFocus(month, it) },
                minLines = 1,
            )
        }

        // Kalendarz-mapa aktywności
        PlannerCard("Kalendarz nauki", Coral) {
            Row(Modifier.fillMaxWidth()) {
                DAY_SHORT.forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            val offset = month.atDay(1).dayOfWeek.value - 1
            val cells = List(offset) { null } + dates
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    for (i in 0 until 7) {
                        val d = week.getOrNull(i)
                        Box(Modifier.weight(1f).padding(2.dp).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (d != null) {
                                val minutes = entries[d]?.minutes ?: 0
                                val level = (minutes / goal.toFloat()).coerceIn(0f, 1f)
                                val bg = if (minutes == 0) MaterialTheme.colorScheme.surfaceVariant
                                else Coral.copy(alpha = 0.25f + 0.75f * level)
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(bg, RoundedCornerShape(8.dp))
                                        .then(
                                            if (d == today) Modifier.border(2.dp, Navy, RoundedCornerShape(8.dp))
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${d.dayOfMonth}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (d == today) FontWeight.Bold else null,
                                        color = if (level > 0.6f) Color.White else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Im ciemniejszy kolor, tym bliżej dziennego celu ($goal min).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Tracker nawyków w miesiącu
        PlannerCard("Tracker nawyków", Mint) {
            val daysSoFar = when {
                month < YearMonth.from(today) -> month.lengthOfMonth()
                month == YearMonth.from(today) -> today.dayOfMonth
                else -> 0
            }
            data.settings.habits.forEachIndexed { i, habit ->
                val count = entries.values.count { it != null && habit in it.habitsDone }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
                    Text(habit, modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { if (daysSoFar == 0) 0f else count / daysSoFar.toFloat() },
                        modifier = Modifier.weight(1f).height(10.dp),
                        color = Accents[i % Accents.size],
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {},
                        gapSize = 0.dp,
                    )
                    Text(
                        "$count",
                        modifier = Modifier.width(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        PlannerCard("Cele na ten miesiąc", Sun) {
            TaskList(
                tasks = plan.goals,
                placeholder = "Np. obejrzeć 2 filmy bez napisów",
                onAdd = { vm.addMonthGoal(month, it) },
                onToggle = { vm.toggleMonthGoal(month, it) },
                onDelete = { vm.deleteMonthGoal(month, it) },
            )
        }

        PlannerCard("Podsumowanie miesiąca", Lilac) {
            NoteField(
                value = plan.reflection,
                key = month,
                label = "Moje sukcesy, wnioski i plany na kolejny miesiąc",
                onChange = { vm.setMonthReflection(month, it) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

fun formatHours(minutes: Int): String =
    if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}m"
