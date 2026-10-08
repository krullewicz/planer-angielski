package pl.planer.angielski.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Przechowuje wszystkie dane aplikacji w jednym pliku JSON w pamięci wewnętrznej.
 * Każda zmiana stanu jest zapisywana na dysk w tle.
 */
class Repository private constructor(context: Context) {

    private val file = File(context.filesDir, "planer.json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        scope.launch {
            _data.drop(1).collectLatest { save(it) }
        }
    }

    fun update(transform: (AppData) -> AppData) = _data.update(transform)

    fun exportJson(): String = json.encodeToString(AppData.serializer(), _data.value)

    /** Zwraca false, jeśli plik kopii zapasowej jest niepoprawny. */
    fun importJson(text: String): Boolean = runCatching {
        val imported = json.decodeFromString(AppData.serializer(), text)
        _data.value = imported
    }.isSuccess

    private fun load(): AppData = runCatching {
        if (file.exists()) json.decodeFromString(AppData.serializer(), file.readText()) else AppData()
    }.getOrElse { AppData() }

    private fun save(data: AppData) {
        val tmp = File(file.parentFile, "planer.json.tmp")
        tmp.writeText(json.encodeToString(AppData.serializer(), data))
        tmp.renameTo(file)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }

        @Volatile
        private var instance: Repository? = null

        fun get(context: Context): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(context.applicationContext).also { instance = it }
            }
    }
}
