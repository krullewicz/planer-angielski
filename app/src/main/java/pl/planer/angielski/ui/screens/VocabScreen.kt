package pl.planer.angielski.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.planer.angielski.PlannerViewModel
import pl.planer.angielski.data.AppData
import pl.planer.angielski.data.LEITNER_INTERVALS
import pl.planer.angielski.data.Word
import pl.planer.angielski.dueWords
import pl.planer.angielski.ui.StatTile
import pl.planer.angielski.ui.theme.Coral
import pl.planer.angielski.ui.theme.Mint
import pl.planer.angielski.ui.theme.Sky

@Composable
fun VocabScreen(data: AppData, vm: PlannerViewModel) {
    var reviewing by rememberSaveable { mutableStateOf(false) }
    if (reviewing) {
        ReviewSession(data, vm, onFinish = { reviewing = false })
    } else {
        WordList(data, vm, onStartReview = { reviewing = true })
    }
}

@Composable
private fun WordList(data: AppData, vm: PlannerViewModel, onStartReview: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val due = data.dueWords().size
    val filtered = data.words.filter {
        query.isBlank() || it.english.contains(query, true) || it.polish.contains(query, true)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Mój słowniczek", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${data.words.size}", "wszystkie", Sky, Modifier.weight(1f))
                StatTile("$due", "do powtórki", Coral, Modifier.weight(1f))
                StatTile("${data.words.count { it.box == LEITNER_INTERVALS.size }}", "opanowane", Mint, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onStartReview, enabled = due > 0, modifier = Modifier.weight(1f)) {
                    Text(if (due > 0) "Powtórka ($due)" else "Brak powtórek 🎉")
                }
                FilledTonalButton(onClick = { showAdd = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Text(" Dodaj słówko")
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Wyczyść") } }
                } else null,
                placeholder = { Text("Szukaj…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (data.words.isEmpty()) {
            item {
                Text(
                    "Słowniczek jest pusty. Dodawaj słówka ręcznie albo zapisuj „Słówko dnia” z zakładki Dziś.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        }
        items(filtered, key = { it.id }) { word -> WordRow(word, onDelete = { vm.deleteWord(word.id) }) }
    }

    if (showAdd) {
        AddWordDialog(
            onDismiss = { showAdd = false },
            onAdd = { en, pl, ex ->
                vm.addWord(en, pl, ex)
                showAdd = false
            },
        )
    }
}

@Composable
private fun WordRow(word: Word, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(word.english, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Text(word.polish, style = MaterialTheme.typography.bodyMedium)
                if (word.example.isNotBlank()) {
                    Text(
                        word.example,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BoxIndicator(word.box)
            IconButton(onClick = { confirm = true }) {
                Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Usunąć „${word.english}”?") },
            confirmButton = { TextButton(onClick = { confirm = false; onDelete() }) { Text("Usuń") } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Anuluj") } },
        )
    }
}

/** Pięć kropek – w którym pudełku Leitnera jest słówko. */
@Composable
private fun BoxIndicator(box: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 1..LEITNER_INTERVALS.size) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(if (i <= box) Mint else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
        }
    }
}

@Composable
private fun AddWordDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var en by rememberSaveable { mutableStateOf("") }
    var pl by rememberSaveable { mutableStateOf("") }
    var ex by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe słówko") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(en, { en = it }, label = { Text("Po angielsku") }, singleLine = true)
                OutlinedTextField(pl, { pl = it }, label = { Text("Po polsku") }, singleLine = true)
                OutlinedTextField(ex, { ex = it }, label = { Text("Przykład (opcjonalnie)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(en, pl, ex) }, enabled = en.isNotBlank() && pl.isNotBlank()) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
    )
}

@Composable
private fun ReviewSession(data: AppData, vm: PlannerViewModel, onFinish: () -> Unit) {
    // Kolejka sesji – słówka „nie wiem” wracają na koniec kolejki.
    val queue = remember { mutableStateListOf<Long>().apply { addAll(data.dueWords().map { it.id }) } }
    val total = remember { queue.size }
    var known by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var reversed by rememberSaveable { mutableStateOf(false) }
    val word = queue.firstOrNull()?.let { id -> data.words.find { it.id == id } }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onFinish) { Icon(Icons.Default.Close, "Zakończ") }
            LinearProgressIndicator(
                progress = { if (total == 0) 1f else known / total.toFloat() },
                modifier = Modifier.weight(1f).height(8.dp),
                drawStopIndicator = {},
            )
            Text("  $known / $total")
            IconButton(onClick = { reversed = !reversed }) { Icon(Icons.Default.SwapHoriz, "Odwróć kierunek") }
        }

        if (word == null) {
            Spacer(Modifier.weight(1f))
            Text("🎉", style = MaterialTheme.typography.displayLarge)
            Text("Powtórka skończona!", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Słówka wrócą do Ciebie w odpowiednim momencie.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onFinish) { Text("Wróć do słowniczka") }
            Spacer(Modifier.weight(1f))
            return@Column
        }

        Text(
            if (reversed) "Jak to powiedzieć po angielsku?" else "Co to znaczy?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            onClick = { revealed = !revealed },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 240.dp),
        ) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    if (reversed) word.polish else word.english,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                AnimatedVisibility(revealed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (reversed) word.english else word.polish,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (word.example.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(word.example, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
                        }
                    }
                }
                if (!revealed) {
                    Spacer(Modifier.height(16.dp))
                    Text("Dotknij, aby odkryć", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    vm.reviewWord(word.id, known = false)
                    queue.removeAt(0)
                    queue.add(word.id)
                    revealed = false
                },
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text("Jeszcze nie 🤔") }
            Button(
                onClick = {
                    vm.reviewWord(word.id, known = true)
                    queue.removeAt(0)
                    known++
                    revealed = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Mint),
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text("Wiem! ✓") }
        }
    }
}
