package pl.planer.angielski.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.planer.angielski.PlannerViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.ui.PlannerCard
import pl.planer.angielski.ui.theme.Coral
import pl.planer.angielski.ui.theme.Lilac
import pl.planer.angielski.ui.theme.Mint
import pl.planer.angielski.ui.theme.Navy
import pl.planer.angielski.ui.theme.Sky
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(data: AppData, vm: PlannerViewModel) {
    val context = LocalContext.current
    val s = data.settings
    var showTimePicker by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.setReminder(true, s.reminderHour, s.reminderMinute)
        else Toast.makeText(context, "Bez zgody na powiadomienia przypomnienie nie zadziała", Toast.LENGTH_LONG).show()
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)!!.use { it.write(vm.exportJson().toByteArray()) }
        }.onSuccess { Toast.makeText(context, "Zapisano kopię zapasową", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, "Nie udało się zapisać pliku", Toast.LENGTH_LONG).show() }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
        }.getOrNull()
        val ok = text != null && vm.importJson(text)
        Toast.makeText(
            context,
            if (ok) "Przywrócono dane z kopii" else "To nie jest poprawna kopia zapasowa",
            Toast.LENGTH_LONG,
        ).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Ustawienia", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))

        PlannerCard("Dzienny cel", Coral) {
            var goal by remember(s.dailyGoalMinutes) { mutableFloatStateOf(s.dailyGoalMinutes.toFloat()) }
            Text("${goal.toInt()} minut dziennie", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = goal,
                onValueChange = { goal = it },
                onValueChangeFinished = { vm.setDailyGoal(goal.toInt()) },
                valueRange = 5f..120f,
                steps = 22,
            )
            Text(
                "Nawet 10–15 minut codziennie daje lepsze efekty niż długa nauka raz w tygodniu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PlannerCard("Przypomnienie", Sky) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Codzienne powiadomienie", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Godzina: %02d:%02d".format(s.reminderHour, s.reminderMinute),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = s.reminderEnabled,
                    onCheckedChange = { on ->
                        if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            vm.setReminder(on, s.reminderHour, s.reminderMinute)
                        }
                    },
                )
            }
            TextButton(onClick = { showTimePicker = true }) { Text("Zmień godzinę") }
        }

        PlannerCard("Moje nawyki", Mint) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                s.habits.forEach { habit ->
                    InputChip(
                        selected = false,
                        onClick = { vm.removeHabit(habit) },
                        label = { Text(habit) },
                        trailingIcon = { Icon(Icons.Default.Close, "Usuń", Modifier.size(16.dp)) },
                    )
                }
            }
            var newHabit by rememberSaveable { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newHabit,
                    onValueChange = { newHabit = it },
                    placeholder = { Text("Np. Podcast, Fiszki…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { vm.addHabit(newHabit); newHabit = "" },
                    enabled = newHabit.isNotBlank(),
                ) { Icon(Icons.Default.Add, "Dodaj") }
            }
        }

        PlannerCard("Kopia zapasowa", Lilac) {
            Text(
                "Dane są przechowywane tylko na telefonie. Zrób kopię przed odinstalowaniem aplikacji lub zmianą telefonu.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { exportLauncher.launch("planer-angielski-${LocalDate.now()}.json") },
                    modifier = Modifier.weight(1f),
                ) { Text("Eksportuj") }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) },
                    modifier = Modifier.weight(1f),
                ) { Text("Importuj") }
            }
        }

        PlannerCard("O aplikacji", Navy) {
            Text(
                "Planer Angielski – codzienny planer nauki angielskiego: słówko i zwrot dnia, mini-gramatyka, " +
                    "wyzwania, plan tygodnia i miesiąca, tracker nawyków oraz fiszki z powtórkami.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Projekt hobbystyczny inspirowany papierowymi planerami językowymi. Nie jest powiązany z żadnym wydawnictwem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(s.reminderHour, s.reminderMinute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Godzina przypomnienia") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    vm.setReminder(s.reminderEnabled, state.hour, state.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") } },
        )
    }
}
