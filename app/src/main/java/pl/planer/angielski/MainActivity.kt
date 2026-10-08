package pl.planer.angielski

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.planer.angielski.ui.screens.MonthScreen
import pl.planer.angielski.ui.screens.SettingsScreen
import pl.planer.angielski.ui.screens.TodayScreen
import pl.planer.angielski.ui.screens.VocabScreen
import pl.planer.angielski.ui.screens.WeekScreen
import pl.planer.angielski.ui.theme.PlannerTheme

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Dziś", Icons.Default.Today),
    Tab("Tydzień", Icons.Default.ViewWeek),
    Tab("Miesiąc", Icons.Default.CalendarMonth),
    Tab("Słówka", Icons.Default.Style),
    Tab("Ustawienia", Icons.Default.Settings),
)

class MainActivity : ComponentActivity() {
    private val vm: PlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlannerTheme {
                val data by vm.data.collectAsStateWithLifecycle()
                var tab by rememberSaveable { mutableIntStateOf(0) }
                val due = data.dueWords().size

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            tabs.forEachIndexed { i, t ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = { tab = i },
                                    icon = {
                                        if (i == 3 && due > 0) {
                                            BadgedBox(badge = { Badge { Text("$due") } }) { Icon(t.icon, null) }
                                        } else {
                                            Icon(t.icon, null)
                                        }
                                    },
                                    label = { Text(t.label, maxLines = 1) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .imePadding()
                    ) {
                        when (tab) {
                            0 -> TodayScreen(data, vm)
                            1 -> WeekScreen(data, vm)
                            2 -> MonthScreen(data, vm)
                            3 -> VocabScreen(data, vm)
                            else -> SettingsScreen(data, vm)
                        }
                    }
                }
            }
        }
    }
}
