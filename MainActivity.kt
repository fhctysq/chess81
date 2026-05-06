package com.boardgame.chess81

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs

// кольори та енуми
val boardLightColor = Color(0xFFE2F4C6) // світлі клітинки
val boardDarkColor = Color(0xFF4EAC5E)  // темні клітинки
val selectionColor = Color(0xB0CFCFFF)   // виділення вибраної клітинки (напівпрозорий блакитний)
val lastMoveColor = Color(0x82FFD800) // напівпрозорий золотий для найновішого ходу
val moveMoveColor = Color(0x82F09020) // напівпрозорий золотий для найновішого ходу
val legalMoveDotColor = Color.Black.copy(alpha = 0.3f) // колір крапки для легальних ходів
val captureGlowColor = Color.Black.copy(alpha = 0.54f) // для підсвітки фігур під взяття
val checkGlowColor = Color(0xECFF2030) // підсвітка для шаху
val hintGlowColor = Color.Blue.copy(alpha = 0.54f) // підсвітка для підказки
val buttonStyleColor = Color(0xFF204020)  // темний зелений для кнопок
val goldButtonColor = Color(0x50FFD800) // напівпрозорий золотий для кнопок
val backgroundColor = Color(0xFF161616)

data class BoardTheme(
    val name: String,
    val lightColor: Color,
    val darkColor: Color,
    // кольори підсвіток, специфічні для теми
    val selectionColor: Color = Color(0xB0CFCFFF),
    val lastMoveColor: Color = Color(0x82FFD800),
    val legalMoveDotColor: Color = Color.Black.copy(alpha = 0.32f),
    val captureGlowColor: Color = Color.Black.copy(alpha = 0.54f),
    val checkGlowColor: Color = Color(0xECFF2030),
    val hintGlowColor: Color = Color.Blue.copy(alpha = 0.52f)
)

object BoardThemes {
    val Green = BoardTheme("Зелена", boardLightColor, boardDarkColor)
    val Blue = BoardTheme("Блакитна", Color(0xFFD0E0FF), Color(0xFF82A0BD))
    val DeepBlue = BoardTheme("Синя", Color(0xFFBFE8FF), Color(0xFF5882AF))
    val Brown = BoardTheme("Коричнева", Color(0xFFF4D8C0), Color(0xFFB58864))
    val Orange = BoardTheme("Помаранчева", Color(0xFFFBE0C4), Color(0xFFE4924C))
    val Purple = BoardTheme("Фіолетова", Color(0xFFE6D8F2), Color(0xFFAE86D6))
    val Grayscale = BoardTheme(
        name = "Відтінки\nсірого",
        lightColor = Color(0xFFE0E0E0),
        darkColor = Color(0xFF808088),
        legalMoveDotColor = Color.White.copy(alpha = 0.5f), // світла підсвітка на темному
        captureGlowColor = Color.White.copy(alpha = 0.4f),
        hintGlowColor = Color.Cyan.copy(alpha = 0.52f)
    )
    // сюди можна додавати нові теми...

    val list = listOf(Green, Blue, DeepBlue, Brown, Orange, Purple, Grayscale)
    fun fromName(name: String) = list.find { it.name.equals(name, ignoreCase = true) } ?: Green
}

val LocalBoardTheme = staticCompositionLocalOf<BoardTheme> {
    error("BoardTheme not provided")
}

val LocalPieceTheme = staticCompositionLocalOf<String> {
    error("PieceTheme not provided")
}

enum class CellNameMode {
    ALL,    // показувати всі
    HIDDEN, // не показувати
    EDGES   // лише по краю дошки
}

enum class PlayerColor { WHITE, BLACK }
// розширення
val PlayerColor.opponent: PlayerColor
    get() = if (this == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING, GUARD }
enum class GameOutcome { WHITE_WINS, BLACK_WINS, DRAW }

//data class ChessPiece(val type: PieceType, val color: PlayerColor)
data class ChessPiece(val type: PieceType, val color: PlayerColor) {
    companion object {
        private val cache = Array(14) { code ->
            val pieceType = when (code % 7) {
                0 -> PieceType.PAWN
                1 -> PieceType.KNIGHT
                2 -> PieceType.BISHOP
                3 -> PieceType.GUARD
                4 -> PieceType.ROOK
                5 -> PieceType.QUEEN
                else -> PieceType.KING
            }
            val color = if (code < 7) PlayerColor.WHITE else PlayerColor.BLACK
            ChessPiece(pieceType, color)
        }

        // Повертає null для порожніх клітинок (код 14)
        fun fromCode(code: Int): ChessPiece? {
            if (code == 14) return null
            return cache[code]
        }

        // Для панелі взятих фігур, де ми на 100% знаємо, що фігура існує
        fun fromCodeNotNull(code: Int): ChessPiece {
            return cache[code]
        }
    }
}
data class MoveRecord(val from: Pair<Int, Int>, val to: Pair<Int, Int>, val notation: String)

val squareNamesAll = arrayOf(
    "a9", "b9", "c9", "d9", "e9", "f9", "g9", "h9", "i9",
    "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8", "i8",
    "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7", "i7",
    "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", "i6",
    "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5", "i5",
    "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "i4",
    "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", "i3",
    "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "i2",
    "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", "i1"
)

// для перевернутої дошки
val squareNamesAllFlipped = squareNamesAll.reversedArray()

val squareNamesEdges = arrayOf(
    "", "", "", "", "", "", "", "", "9",
    "", "", "", "", "", "", "", "", "8",
    "", "", "", "", "", "", "", "", "7",
    "", "", "", "", "", "", "", "", "6",
    "", "", "", "", "", "", "", "", "5",
    "", "", "", "", "", "", "", "", "4",
    "", "", "", "", "", "", "", "", "3",
    "", "", "", "", "", "", "", "", "2",
    "a", "b", "c", "d", "e", "f", "g", "h", "i1"
)

val squareNamesEdgesForBlack = arrayOf(
    "a9", "b", "c", "d", "e", "f", "g", "h", "i",
    "8", "", "", "", "", "", "", "", "",
    "7", "", "", "", "", "", "", "", "",
    "6", "", "", "", "", "", "", "", "",
    "5", "", "", "", "", "", "", "", "",
    "4", "", "", "", "", "", "", "", "",
    "3", "", "", "", "", "", "", "", "",
    "2", "", "", "", "", "", "", "", "",
    "1", "", "", "", "", "", "", "", ""
)

// об'єкт для адаптивних відступів
object OrientationPaddings {
    val startPadding: Dp
        @Composable
        get() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 6.dp else 12.dp

    val endPadding: Dp
        @Composable
        get() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 48.dp else 16.dp

    val topPadding: Dp
        @Composable
        get() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 16.dp else 42.dp

    val secondaryTopPadding: Dp
        @Composable
        get() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 16.dp else 2.dp
}

data class UiSettings(
    val boardTheme: BoardTheme,
    val pieceTheme: String,
    val gameMode: String,
    val cellNameMode: CellNameMode,
    val allowUndo: Boolean,
    val animationsEnabled: Boolean
)

val LocalUiSettings = staticCompositionLocalOf<UiSettings> {
    error("UiSettings not provided")
}

// клас для керування збереженням та завантаженням налаштувань гри
class SettingsManager(context: Context) {
    // створюємо приватний екземпляр SharedPreferences
    private val prefs = context.getSharedPreferences("chess81_settings", Context.MODE_PRIVATE)

    // об'єкт з ключами налаштувань
    companion object {
        const val KEY_GAME_MODE = "game_mode"
        const val KEY_PIECE_THEME = "piece_theme"
        const val KEY_BOARD_THEME = "board_theme"
        const val KEY_SHOW_CELL_NAMES = "show_cell_names"
        const val KEY_TIMER_DURATION = "timer_duration" // глобальний таймер
        const val KEY_BOT_TIMER_DURATION = "bot_timer_duration" // таймер бота
        const val KEY_BOT_DEPTH = "bot_depth" // ключ для глибини рушія
        const val KEY_BOT_UNDO = "bot_undo" // дозволити undo в грі з ботом
        const val KEY_ALLOW_UNDO = "allow_undo" // для онлайн і офлайн
        const val KEY_RATED_GAME = "rated_game"
        const val KEY_VARIANT = "variant" // "standard", "chess960" або "pre-chess"
        const val KEY_ANIMATION = "animation" //
    }

    // функція для збереження налаштування (рядка)
    fun saveSetting(key: String, value: String) {
        prefs.edit(commit = true) { // commit = true для миттєвого збереження
            putString(key, value)
        }
    }

    // функція для отримання налаштування, з можливістю вказати значення за замовчуванням
    fun getSetting(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}

// контейнер для статистики пошуку рушія
data class EngineStats(
    val score: String = "0",   // оцінка (в сантипішаках або мат)
    val nodes: String = "0",   // кількість відвіданих вузлів
    val timeMs: String = "0",  // час пошуку
    val nps: String = "0",     // вузлів на секунду
    val depth: Int = 0         // глибина пошуку
)

// головна активність
class MainActivity : ComponentActivity() {

    // створюємо Handler для таймера вимкнення екрану
    private val timeoutHandler = Handler(Looper.getMainLooper())

    // час в мілісекундах (5 хвилин)
    private val CHESS_TIMEOUT_MS = 5 * 60 * 1000L

    // виконається, коли таймер спливе
    private val removeKeepScreenOnRunnable = Runnable {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // "прогріваємо" плеєр звуків у фоні ще до початку гри
        AudioController.getPlayer(this.applicationContext)
        
        // робить status bar прозорим і дозволяє додатку малюватися на всю висоту екрану.
        enableEdgeToEdge()

        Engine81Bridge.initTablesJNI(assets)

        setContent {
            // NavController керуватиме переходами
            val navController = rememberNavController()
            // отримуємо поточний контекст та орієнтацію екрану
            val context = LocalContext.current
            val orientation = LocalConfiguration.current.orientation
            // щоразу читаємо налаштування при виході з екрану налаштувань
            val settingsManager = remember { SettingsManager(context) }
            var uiSettings by remember {
                mutableStateOf(
                    UiSettings(
                        boardTheme = BoardThemes.fromName(settingsManager.getSetting(SettingsManager.KEY_BOARD_THEME, BoardThemes.Green.name)),
                        pieceTheme = settingsManager.getSetting(SettingsManager.KEY_PIECE_THEME, "default"),
                        gameMode = settingsManager.getSetting(SettingsManager.KEY_GAME_MODE, "normal"),
                        cellNameMode = CellNameMode.valueOf(settingsManager.getSetting(SettingsManager.KEY_SHOW_CELL_NAMES, CellNameMode.ALL.name)),
                        allowUndo = settingsManager.getSetting(SettingsManager.KEY_ALLOW_UNDO, "true").toBoolean(),
                        animationsEnabled = settingsManager.getSetting(SettingsManager.KEY_ANIMATION, "true").toBoolean() // читаємо налаштування анімації
                    )
                )
            }
            // функція оновлення стану налаштувань
            fun updateSettings() {
                uiSettings = UiSettings(
                    boardTheme = BoardThemes.fromName(settingsManager.getSetting(SettingsManager.KEY_BOARD_THEME, BoardThemes.Green.name)),
                    pieceTheme = settingsManager.getSetting(SettingsManager.KEY_PIECE_THEME, "default"),
                    gameMode = settingsManager.getSetting(SettingsManager.KEY_GAME_MODE, "normal"),
                    cellNameMode = CellNameMode.valueOf(settingsManager.getSetting(SettingsManager.KEY_SHOW_CELL_NAMES, CellNameMode.ALL.name)),
                    allowUndo = settingsManager.getSetting(SettingsManager.KEY_ALLOW_UNDO, "true").toBoolean(),
                    animationsEnabled = settingsManager.getSetting(SettingsManager.KEY_ANIMATION, "true").toBoolean() // читаємо налаштування анімації
                )
            }
            val activePieceTheme = settingsManager.getSetting(SettingsManager.KEY_PIECE_THEME, "default")

            // отримуємо конфігурацію екрану
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp.dp
            val screenHeightDp = configuration.screenHeightDp.dp

            // обчислюємо розмір дошки за меншим розміром екрану. remember гарантує, що це значення не буде перераховуватись без потреби
            val boardSizeDp = remember(screenWidthDp, screenHeightDp) {
                minOf(screenWidthDp, screenHeightDp)
            }

            // цей ефект буде виконуватися щоразу, коли змінюється орієнтація
            LaunchedEffect(orientation) {
                val window = (context as? Activity)?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        // в альбомному режимі: ховаємо status bar
                        insetsController.hide(WindowInsetsCompat.Type.statusBars())
                        // поводимося як ютюб: панелі з'являються по свайпу
                        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        // в портретному режимі: повертаємо status bar
                        insetsController.show(WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
            // отримуємо поточну щільність екрану
            val density = LocalDensity.current

            // обчислюємо новий коефіцієнт масштабування шрифту
            val newFontScale = density.fontScale.coerceIn(0.8f, 1.2f) // затискаємо масштаб між 80% і 120%

            // створюємо новий CompositionLocalProvider, щоб перевизначити масштаб шрифту
            CompositionLocalProvider(
                // надаємо нову щільність, де fontScale ЗАВЖДИ дорівнює 1.0f, ігноруючи системні налаштування.
                // при цьому щільність для dp залишається без змін.
                LocalDensity provides Density(density = density.density, fontScale = newFontScale),
                LocalRippleConfiguration provides null // глобальне вимкнення анімацій ripple
            ) {
                // надаємо тему та налаштування всім дочірнім компонентам
                CompositionLocalProvider(
                    LocalUiSettings provides uiSettings,
                    LocalBoardTheme provides uiSettings.boardTheme,
                    LocalPieceTheme provides activePieceTheme
                ) {
                    // NavHost - це контейнер, що відображає поточний екран
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        // Робимо переходи миттєвими:
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        // оголошуємо перший екран з адресою "home"
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController, onSettingsChanged = ::updateSettings)
                        }
                        // оголошуємо другий екран для кнопок локальної гри та самоаналізу.
                        composable("game?mode={mode}&isTraining={isTraining}") { backStackEntry ->
                            val gameMode = backStackEntry.arguments?.getString("mode") ?: "dual"
                            val isTraining = backStackEntry.arguments?.getString("isTraining")?.toBoolean() ?: false
                            LocalGameScreen(
                                navController = navController,
                                isTrainingMode = isTraining, // передаємо прапор
                                gameMode = gameMode // передаємо режим гри
                            )
                        }
                        composable("online_game/{gameId}") { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId")
                            if (gameId != null) {
                                OnlineGameScreen(gameId = gameId, navController = navController)
                            } else {
                                // повернутися, якщо ID гри не знайдено
                                navController.popBackStack()
                            }
                        }
                        composable("bot_game") {
                            BotGameScreen(navController = navController)
                        }
                        composable("puzzles") {
                            PuzzlesScreen(navController = navController)
                        }
                        composable("trainingGame") {
                            TrainingGameScreen(navController = navController)
                        }
                        composable("review/{timestamp}") { backStackEntry ->
                            val timestampStr = backStackEntry.arguments?.getString("timestamp")
                            val timestamp = timestampStr?.toLongOrNull()
                            if (timestamp != null) {
                                ReviewScreen(timestamp = timestamp, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }

    // метод ловить дотик до екрану
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        resetScreenTimeout() // отримали дотик - скидаємо таймер
        return super.dispatchTouchEvent(ev)
    }

    private fun resetScreenTimeout() {
        // вмикаємо утримання екрану (якщо воно було вимкнене)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // скасовуємо попередній таймер
        timeoutHandler.removeCallbacks(removeKeepScreenOnRunnable)

        // запускаємо новий таймер на 5 хвилин
        timeoutHandler.postDelayed(removeKeepScreenOnRunnable, CHESS_TIMEOUT_MS)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // для звільнення пам'яті можна створити JNI-функцію cleanup_magic_tables()

        // викликаємо C++ функцію для очищення всіх ресурсів, зокрема для коректного завершення роботи пулу потоків
        Engine81Bridge.destroyEngineJNI()
    }
}

@Composable
fun SettingsScreen(navController: NavController, onSettingsChanged: () -> Unit) {
    // отримуємо context для ініціалізації SettingsManager
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    // ефект виконає дію, коли користувач піде з цього екрану
    DisposableEffect(Unit) {
        onDispose {
            // коли екран закривається, викликаємо оновлення налаштувань один раз
            onSettingsChanged()
        }
    }

    // створюємо локальні стани, що читають збережені значення
    var currentPieceTheme by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_PIECE_THEME, "Staunty (default)"))
    }
    var pieceMenuExpanded by remember { mutableStateOf(false) } // стан для меню сетів
    
    val pieceThemes = listOf(  // сети фігур
        "default" to "Staunty (default)",
        "custom1" to "Staunton"
    )
    
    var currentGameMode by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_GAME_MODE, "normal"))
    }
    // для вимикання назв клітинок
    var showCellNames by remember {
        mutableStateOf(
            CellNameMode.valueOf(
                settingsManager.getSetting(SettingsManager.KEY_SHOW_CELL_NAMES, CellNameMode.ALL.name)
            )
        )
    }
    val cellNameModes = listOf(
        CellNameMode.ALL to "Показувати всі",
        CellNameMode.EDGES to "Лише по краю",
        CellNameMode.HIDDEN to "Не показувати"
    )

    var currentBoardTheme by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_BOARD_THEME, BoardThemes.Green.name))
    }

    // список режимів гри
    val gameModes = listOf(
        "normal" to "Звичайний",
        "forgot_my_glasses" to "Без окулярів",
        "blindfold" to "Всліпу"
    )

    var allowUndo by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_ALLOW_UNDO, "true").toBoolean())
    }

    // стан для анімацій
    var animationEnabled by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_ANIMATION, "true").toBoolean())
    }

    // обгортка для центрування вмісту та обмеження його ширини
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp) // обмежуємо максимальну ширину
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()) // запам'ятовуємо вертикальну прокрутку
                .padding(top = OrientationPaddings.topPadding, start = 12.dp, end = 18.dp)
        ) {
            // заголовок і кнопка "Назад"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Налаштування",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // вибір режиму гри
            Text("Режим гри", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gameModes.forEach { (modeKey, modeName) ->
                    Button(
                        onClick = {
                            currentGameMode = modeKey
                            settingsManager.saveSetting(SettingsManager.KEY_GAME_MODE, modeKey)
                        },
                        modifier = Modifier.weight(1f).height(60.dp), // уніфікуємо висоту кнопок
                        shape = RoundedCornerShape(10),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp), // зменшуємо внутрішні відступи
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentGameMode == modeKey) boardDarkColor else buttonStyleColor
                        )
                    ) {
                        Text(text = modeName, color = Color.White, fontSize = 18.sp) // і шрифт для консистентності
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // вибір теми дошки
            Text("Кольори дошки", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))

            // горизонтальний ряд кнопок, що прокручується
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(BoardThemes.list) { theme ->
                    Button(
                        onClick = {
                            currentBoardTheme = theme.name
                            settingsManager.saveSetting(SettingsManager.KEY_BOARD_THEME, theme.name)
                        },
                        modifier = Modifier.width(110.dp).height(60.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10),
                        colors = ButtonDefaults.buttonColors(
                            // використовуємо колір теми для підсвітки, а не стандартний зелений
                            containerColor = if (currentBoardTheme == theme.name) theme.darkColor else buttonStyleColor
                        )
                    ) {
                        Text(text = theme.name, color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // налаштування назв клітинок
            Text("Назви клітинок", color = Color.White, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            // горизонтальний ряд кнопок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // відстань між кнопками
            ) {
                cellNameModes.forEach { (mode, label) ->
                    Button(
                        onClick = {
                            showCellNames = mode
                            settingsManager.saveSetting(SettingsManager.KEY_SHOW_CELL_NAMES, mode.name)
                        },
                        modifier = Modifier.weight(0.32f).height(60.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10),
                        colors = ButtonDefaults.buttonColors(
                            // змінюємо колір залежно від того, чи вибрана кнопка
                            containerColor = if (showCellNames == mode) boardDarkColor else buttonStyleColor
                        )
                    ) {
                        Text(text = label, color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Text("Сет фігур", color = Color.White, fontSize = 24.sp)
            // вибір теми фігур
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)) // робить анімацію кліку (ripple) охайно заокругленою
                    .clickable { pieceMenuExpanded = true } // відкриваємо меню
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Поточний сет:", color = Color.LightGray, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                
                // Box потрібен як якір (anchor), щоб DropdownMenu з'явилося рівно під цим рядком
                Box(modifier = Modifier.fillMaxWidth(0.58f), contentAlignment = Alignment.Center) {

                    // знаходимо назву для відображення або показуємо ключ, якщо щось пішло не так
                    val displayName = pieceThemes.find { it.first == currentPieceTheme }?.second ?: currentPieceTheme
                    Text(displayName, color = Color.White, fontSize = 18.sp,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { pieceMenuExpanded = true }
                            .background(buttonStyleColor).padding(horizontal = 12.dp, vertical = 6.dp) // легкий фон, щоб бокс виглядав клікабельним
                    )
                    DropdownMenu( // випадне меню
                        expanded = pieceMenuExpanded,
                        onDismissRequest = { pieceMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF2E2E2E)) // темний фон, щоб зливався з UI
                    ) {
                        pieceThemes.forEach { (themeKey, themeName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = themeName,
                                        fontSize = 18.sp, // робимо текст більшим
                                        color = if (currentPieceTheme == themeKey) Color.White else boardDarkColor // підсвічуємо вибраний варіант золотим кольором
                                    )
                                },
                                onClick = {
                                    // оновлюємо стан, зберігаємо в налаштування і закриваємо меню
                                    currentPieceTheme = themeKey
                                    settingsManager.saveSetting(SettingsManager.KEY_PIECE_THEME, themeKey)
                                    pieceMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // налаштування takeback
            Text("Повертати ходи", color = Color.White, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        allowUndo = true
                        settingsManager.saveSetting(SettingsManager.KEY_ALLOW_UNDO, "true")
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allowUndo) boardDarkColor else buttonStyleColor
                    )
                ) {
                    Text(text = "Дозволено", color = Color.White, fontSize = 18.sp)
                }
                Button(
                    onClick = {
                        allowUndo = false
                        settingsManager.saveSetting(SettingsManager.KEY_ALLOW_UNDO, "false")
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!allowUndo) boardDarkColor else buttonStyleColor
                    )
                ) {
                    Text(text = "Не дозволено", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // налаштування анімації (заготовка)
            Text("Анімація переміщення фігур", color = Color.White, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { animationEnabled = true
                        settingsManager.saveSetting(SettingsManager.KEY_ANIMATION, "true") },
                    modifier = Modifier.weight(1f).height(60.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (animationEnabled) boardDarkColor else buttonStyleColor
                    )
                ) {
                    Text(text = "Увімкнена", color = Color.White, fontSize = 18.sp)
                }
                Button(
                    onClick = { animationEnabled = false
                        settingsManager.saveSetting(SettingsManager.KEY_ANIMATION, "false") },
                    modifier = Modifier.weight(1f).height(60.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!animationEnabled) boardDarkColor else buttonStyleColor
                    )
                ) {
                    Text(text = "Вимкнена", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(36.dp)) // відступ для альбомного повороту
        }
    }
}

data class JniGameStatus(
    val status: String,
    val whiteTimeMs: Long,
    val blackTimeMs: Long
)

// data-клас отримання оновленого стану з C++. рушій повертатиме цей об'єкт після кожного ходу.
data class JniGameStateUpdate(
    val mailbox: IntArray,
    val pieceBitboards: Array<LongArray>, // можна залишити, - або прибрати, якщо UI працюватиме тільки з mailbox
    val sideToMove: Int,
    val castlingRights: Int,
    val enPassantSquare: Int
) {
    // contentDeepEquals потрібен для коректного порівняння масивів
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JniGameStateUpdate
        if (sideToMove != other.sideToMove) return false
        if (castlingRights != other.castlingRights) return false
        if (enPassantSquare != other.enPassantSquare) return false
        if (!mailbox.contentEquals(other.mailbox)) return false
        if (!pieceBitboards.contentDeepEquals(other.pieceBitboards)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sideToMove
        result = 31 * result + castlingRights
        result = 31 * result + enPassantSquare
        result = 31 * result + mailbox.contentHashCode()
        result = 31 * result + pieceBitboards.contentDeepHashCode()
        return result
    }
}

class ChessSoundPlayer(context: Context) {
    // типи звуків
    enum class SoundType { MOVE, TAKE, CASTLE, EN, CHECK, CHECKMATE, STALEMATE }

    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private val soundIds = mutableMapOf<SoundType, Int>()

    init {
        // завантажуємо звуки в пам'ять і зберігаємо їхні ID
        soundIds[SoundType.MOVE] = soundPool.load(context, R.raw.move, 1)
        soundIds[SoundType.TAKE] = soundPool.load(context, R.raw.take, 1)
        soundIds[SoundType.CASTLE] = soundPool.load(context, R.raw.cast, 1)
        soundIds[SoundType.EN] = soundPool.load(context, R.raw.en, 1)
        soundIds[SoundType.CHECK] = soundPool.load(context, R.raw.check, 1)
        soundIds[SoundType.CHECKMATE] = soundPool.load(context, R.raw.checkmate, 1)
        soundIds[SoundType.STALEMATE] = soundPool.load(context, R.raw.stalemate, 1)
    }

    fun playSound(type: SoundType) {
        soundIds[type]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    // звільняємо ресурси, коли плеєр не потрібен
    fun release() {
        soundPool.release()
    }
}

object AudioController {
    private var player: ChessSoundPlayer? = null

    fun getPlayer(context: Context): ChessSoundPlayer {
        if (player == null) {
            player = ChessSoundPlayer(context.applicationContext)
        }
        return player!!
    }
}

// клас для отримання результату ходу з C++
data class JniMoveResult(
    val uiUpdate: IntArray,
    val notation: String,
    val gameStatus: String, // результат гри
    val whiteCaptured: IntArray, // взяті білі фігури
    val blackCaptured: IntArray,  // взяті чорні фігури
    val materialAdvantage: Int, // перевага по матеріалу
    val whiteTimeRemainingMs: Long, // таймери
    val blackTimeRemainingMs: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JniMoveResult
        if (!uiUpdate.contentEquals(other.uiUpdate)) return false
        if (notation != other.notation) return false
        if (gameStatus != other.gameStatus) return false // результат гри
        if (!whiteCaptured.contentEquals(other.whiteCaptured)) return false
        if (!blackCaptured.contentEquals(other.blackCaptured)) return false
        if (materialAdvantage != other.materialAdvantage) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uiUpdate.contentHashCode()
        result = 31 * result + notation.hashCode()
        result = 31 * result + gameStatus.hashCode()
        result = 31 * result + whiteCaptured.contentHashCode()
        result = 31 * result + blackCaptured.contentHashCode()
        result = 31 * result + materialAdvantage
        return result
    }
}

// клас для передачі даних в PlayerInfoPanel
data class PlayerDisplayData(
    val capturedPieces: List<Int> = emptyList(),
    val advantage: Int = 0,
    val timeRemainingMs: Long? = null, // поле для таймера
    val isTimerActive: Boolean = false // для анімації лише одного з таймерів
)

@SuppressLint("DefaultLocale")
@Composable
fun TimerDisplay(timeMs: Long, isActive: Boolean) {
    // не показуємо таймер, якщо він вимкнений (час < 0)
    if (timeMs < 0) {
        // замість нього вставляємо спейсер, щоб зберегти верстку
        Spacer(modifier = Modifier.height(32.dp))
        return
    }

    // форматуємо час з мілісекунд у MM:SS
    val totalSeconds = timeMs / 1000
    val displayText = if (totalSeconds < 10 && timeMs > 0) {
        // показуємо десяті
        String.format("%.1f", timeMs.toDouble() / 1000.0)
    } else {
        // звичайний формат MM:SS
        val minutes = timeMs / 60000
        val seconds = (timeMs / 1000) % 60
        String.format("%d:%02d", minutes, seconds)
    }

    // визначаємо кольори на основі стану isActive залежно від часу, що залишився
    val textColor: Color
    val backgroundColor: Color

    if (isActive) {
        backgroundColor = Color(0xFFF0F0F0) // "Майже білий" фон для активного таймера
        textColor = if (totalSeconds < 10 && timeMs > 0 && (timeMs / 500) % 2 == 0L) {
            Color.Red // миготіння червоним кожні 500 мс на світлому фоні, коли часу мало
        } else if (totalSeconds < 60) {
            Color(0xFFFFA600) // помаранчевий, коли часу менш як 60 секунд
        } else {
            Color.Black // чорний текст для активного таймера
        }
    } else {
        backgroundColor = Color.Transparent // прозорий фон для неактивного
        textColor = Color.White
    }

    Text(
        text = displayText,
        color = textColor,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold, // жирніший для кращої читності
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

// функція панелі гравця (захоплені фігури + перевага)
@Composable
private fun PlayerInfoPanel(
    modifier: Modifier = Modifier,
    data: PlayerDisplayData,
    isTopPanel: Boolean, // параметр розрізнення верхньої та нижньої панелей
    isTrainingMode: Boolean = false // параметр для розрізнення режиму самоаналізу
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // визначаємо порядок таймера щодо рядка дисплеїв матеріалу
    val isTimerFirst = if (isLandscape) !isTopPanel else isTopPanel

    // визначаємо вирівнювання для рядка з матеріалом
    val materialArrangement = when {
        isLandscape && isTopPanel -> Arrangement.End // в альбомному для верхньої панелі - праворуч
        else -> Arrangement.Start                  // в решті випадків - ліворуч
    }
    // оголошуємо рядок, щоб уникнути повторення коду
    val materialAndAdvantageRow = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth().height(28.dp), // fillMaxWidth потрібен для роботи Arrangement
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = materialArrangement // використовуємо динамічну змінну
        ) {
            // конвертуємо коди фігур в ChessPiece для відображення
            CapturedPiecesDisplay(capturedPieces = data.capturedPieces.map { ChessPiece.fromCodeNotNull(it) })
            Spacer(modifier = Modifier.width(2.dp))
            AdvantageDisplay(advantage = data.advantage)
        }
    }
    // визначаємо вирівнювання для таймера
    val timerArrangement = when {
        isLandscape && !isTopPanel -> Arrangement.Start // в альбомному для нижньої панелі - ліворуч
        else -> Arrangement.End                       // в решті випадків - праворуч
    }
    // оголошуємо рядок таймера, щоб уникнути повторення коду
    val timerRow = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = timerArrangement // використовуємо динамічну змінну вирівнювання таймера
        ) {
            data.timeRemainingMs?.let { ms ->
                TimerDisplay(ms, isActive = data.isTimerActive)
            }
        }
    }
    // компонуємо панель у потрібному порядку
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalArrangement = if (isTimerFirst) Arrangement.Top else Arrangement.Bottom
    ) {
        if (isTimerFirst) {
            if (!isTrainingMode) { // умова для екрану аналізу
                timerRow()
            }
            materialAndAdvantageRow()
        } else {
            materialAndAdvantageRow()
            if (!isTrainingMode) { // умова для екрану аналізу
                timerRow()
            }
        }
    }
}

@Composable
fun LocalGameSettingsDialog(
    showDialog: Boolean,
    currentTimer: Int,
    isGameStarted: Boolean,
    onDismiss: () -> Unit,
    onTimerSelected: (Int) -> Unit
) {
    if (!showDialog) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(28.dp),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Час на гру (хвилини):", color = Color.LightGray, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                val timerOptions = listOf(0, 3, 5, 10, 15, 30, 60)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(timerOptions.size) { index ->
                        val duration = timerOptions[index]
                        val isSelected = currentTimer == duration

                        Button(
                            onClick = { onTimerSelected(duration) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) boardDarkColor else buttonStyleColor
                            )
                        ) {
                            Text(if (duration == 0) "∞" else "$duration", fontSize = 16.sp)
                        }
                    }
                }

                // попередження, якщо гра вже триває
                if (isGameStarted) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Зміни таймера застосуються з наступної гри.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // TODO: Місце для майбутніх налаштувань (скасування ходів, фора тощо)

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
                ) {
                    Text("Закрити")
                }
            }
        }
    }
}

// composable для нижньої панелі кнопок
@Composable
private fun GameControlsPanel(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    gameMode: String,
    autoFlipEnabled: Boolean,
    onFlipBoard: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onResign: () -> Unit,
    onDrawOffer: () -> Unit,
    isUndoEnabled: Boolean,
    isRedoEnabled: Boolean,
    allowTakeback: Boolean,
    onSetupGame: () -> Unit // колбек для кнопки налаштувань
) {
    if (isLandscape) {
        // вертикальне розташування для альбомного повороту
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // кнопка "Здатися"
            Button(
                onClick = onResign,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF663232), contentColor = Color.White)
            ) {
                Icon(painter = painterResource(id = R.drawable.resign), contentDescription = "Resign", modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "Нічия"
            Button(
                onClick = onDrawOffer,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor, contentColor = Color.White)
            ) {
                Icon(painter = painterResource(id = R.drawable.draw), contentDescription = "Draw", modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка повороту дошки
            Button(
                onClick = onFlipBoard,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameMode == "hotseat" && autoFlipEnabled) boardDarkColor else Color(0xFF424242),
                    contentColor = Color.White
                )
            ) {
                Icon(painter = painterResource(id = R.drawable.flip), contentDescription = "Flip board", modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "хід назад" (Undo)
            Button(
                onClick = onUndo,
                enabled = isUndoEnabled,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626),
                    disabledContentColor = Color(0xFF808080)
                )
            ) {
                Icon(
                    painter = painterResource(id = if (allowTakeback) R.drawable.undo else R.drawable.left),
                    contentDescription = if (allowTakeback) "Відмінити хід" else "Попередній хід",
                    modifier = Modifier.size(if (isLandscape) 34.dp else 32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "хід вперед" (Redo)
            Button(
                onClick = onRedo,
                enabled = isRedoEnabled,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626),
                    disabledContentColor = Color(0xFF808080)
                )
            ) {
                Icon(
                    painter = painterResource(id = if (allowTakeback) R.drawable.redo else R.drawable.right),
                    contentDescription = if (allowTakeback) "Повернути відмінене" else "Наступний хід",
                    modifier = Modifier.size(if (isLandscape) 34.dp else 32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            // кнопка "Налаштування гри"
            Button(
                onClick = onSetupGame,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor, contentColor = Color.White)
            ) {
                Icon(painter = painterResource(id = R.drawable.settings), contentDescription = "Setup", modifier = Modifier.size(32.dp))
            }
        }
    } else {
        // горизонтальне розташування для портретного режиму
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // кнопка "Здатися"
            Button(
                onClick = onResign,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF663232), contentColor = Color.White),
                contentPadding = PaddingValues(2.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.resign), contentDescription = "Resign", modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "Нічия"
            Button(
                onClick = onDrawOffer,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor, contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.draw), contentDescription = "Draw", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка повороту дошки
            Button(
                onClick = onFlipBoard,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameMode == "hotseat" && autoFlipEnabled) boardDarkColor else Color(0xFF424242),
                    contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.flip), contentDescription = "Flip board", modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "хід назад" (Undo)
            Button(
                onClick = onUndo,
                enabled = isUndoEnabled,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626),
                    disabledContentColor = Color(0xFF808080)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (allowTakeback) R.drawable.undo else R.drawable.left),
                    contentDescription = if (allowTakeback) "Відмінити хід" else "Попередній хід",
                    modifier = Modifier.size(if (isLandscape) 34.dp else 32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // кнопка "хід вперед" (Redo)
            Button(
                onClick = onRedo,
                enabled = isRedoEnabled,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626),
                    disabledContentColor = Color(0xFF808080)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (allowTakeback) R.drawable.redo else R.drawable.right),
                    contentDescription = if (allowTakeback) "Повернути відмінене" else "Наступний хід",
                    modifier = Modifier.size(if (isLandscape) 34.dp else 32.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // налаштування
            Button(
                onClick = onSetupGame,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor, contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.settings), contentDescription = "Setup", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun pieceToDrawableResource(piece: ChessPiece): Int {
    // отримуємо режим гри з CompositionLocal
    val gameMode = LocalUiSettings.current.gameMode
    // якщо режим "без окулярів", повертаємо універсальні іконки
    if (gameMode == "forgot_my_glasses") {
        return if (piece.color == PlayerColor.WHITE) R.drawable.w0 else R.drawable.b0
    }

    // інакше працює звичайна логіка
    return when (piece.type) {
        PieceType.PAWN -> if (piece.color == PlayerColor.WHITE) R.drawable.wp else R.drawable.bp
        PieceType.ROOK -> if (piece.color == PlayerColor.WHITE) R.drawable.wr else R.drawable.br
        PieceType.KNIGHT -> if (piece.color == PlayerColor.WHITE) R.drawable.wn else R.drawable.bn
        PieceType.BISHOP -> if (piece.color == PlayerColor.WHITE) R.drawable.wb else R.drawable.bb
        PieceType.QUEEN -> if (piece.color == PlayerColor.WHITE) R.drawable.wq else R.drawable.bq
        PieceType.KING -> if (piece.color == PlayerColor.WHITE) R.drawable.wk else R.drawable.bk
        PieceType.GUARD -> if (piece.color == PlayerColor.WHITE) R.drawable.wg else R.drawable.bg
    }
}

data class AnimatedMove(val piece: ChessPiece, val from: Int, val to: Int)

// допоміжна функція для конвертації індексу клітинки в піксельні координати
private fun indexToOffset(index: Int, cellSizePx: Float, isFlipped: Boolean): Offset {
    val effectiveRow = if (isFlipped) 8 - (index / 9) else index / 9
    val effectiveCol = if (isFlipped) 8 - (index % 9) else index % 9
    return Offset(effectiveCol * cellSizePx, effectiveRow * cellSizePx)
}

// конвертер, що "вчить" аніматор працювати з типом Offset
val OffsetConverter = TwoWayConverter<Offset, AnimationVector2D>(
    convertToVector = { offset -> AnimationVector2D(offset.x, offset.y) },
    convertFromVector = { vector -> Offset(vector.v1, vector.v2) }
)

@Composable
fun FlyingPiece(
    animatedMove: AnimatedMove,
    boardSizePx: Float,
    isFlipped: Boolean,
    onFinished: () -> Unit
) {
    // якщо розмір дошки ще не відомий — нічого не малюємо і не анімуємо.
    // це запобігає помилці та "стартовим" артефактам.
    if (boardSizePx <= 0f) return

    // розмір однієї клітинки в пікселях
    val cellSizePx = boardSizePx / 9f

    // початкова та кінцева позиції фігури в абсолютних пікселях.
    val fromOffset = indexToOffset(animatedMove.from, cellSizePx, isFlipped)
    val toOffset = indexToOffset(animatedMove.to, cellSizePx, isFlipped)

    // стан для рендерингу

    // стан позиції, що оновлюється на кожен кадр анімації, застосовуємо через graphicsLayer (translationX/Y)
    var currentTranslation by remember {
        mutableStateOf(fromOffset)
    }

    // тривалість анімації в мілісекундах. це логічний час, незалежний від fps
    val animationDurationMs = 160f

    // прапорець, що визначає анімуємо чи "телепортуємо".
    val animationsEnabled = LocalUiSettings.current.animationsEnabled

    // петля анімації запускається коли з’являється новий logical move
    // автоматично скасовується Compose при recomposition з новим ключем або виході Composable з дерева
    LaunchedEffect(animatedMove, boardSizePx) {

        // якщо анімації вимкнені (user preference) - одразу ставимо фінальну позицію
        if (!animationsEnabled) {
            currentTranslation = toOffset
            onFinished()
            return@LaunchedEffect
        }

        // отримуємо timestamp першого кадру з frame clock
        val startFrameTimeNanos = withFrameNanos { it }

        // дельта переміщення по кожній осі.
        val deltaX = toOffset.x - fromOffset.x
        val deltaY = toOffset.y - fromOffset.y

        // цикл анімації оновлюється строго по кадрах та автоматично переривається при cancel корутини
        while (true) {

            // якщо корутина скасована, не викликаємо onFinished(), бо рух не був логічно завершений
            if (!kotlinx.coroutines.currentCoroutineContext().isActive) {
                return@LaunchedEffect
            }

            // час поточного кадру з frame clock
            val frameTimeNanos = withFrameNanos { it }

            // скільки мілісекунд минуло з початку анімації.
            val elapsedMs =
                (frameTimeNanos - startFrameTimeNanos) / 1_000_000f

            // нормалізована частка [0..1]
            val rawFraction = elapsedMs / animationDurationMs
            val fraction = rawFraction.coerceIn(0f, 1f)

            // згладження

            // можна підставити будь-яку криву.
            // наприклад класичний smoothstep: f(t) = t² · (3 − 2t)
            val easedFraction =
                fraction * fraction * (3f - 2f * fraction)

            // обчислюємо проміжну позицію
            currentTranslation = Offset(
                x = fromOffset.x + deltaX * easedFraction,
                y = fromOffset.y + deltaY * easedFraction
            )

            // якщо дійшли до кінця — фіксуємо фінальний стан і виходимо.
            if (fraction >= 1f) {
                currentTranslation = toOffset
                break
            }
        }

        // onFinished() викликається тільки якщо: анімація виконана повністю і не скасована
        onFinished()
    }

    // рендер
    Image(
        painter = painterResource(
            id = pieceToDrawableResource(animatedMove.piece)
        ),
        contentDescription = null,
        modifier = Modifier
            // фізичний розмір фігури — рівно одна клітинка.
            .size(
                with(LocalDensity.current) {
                    cellSizePx.toDp()
                }
            )
            .graphicsLayer {
                translationX = currentTranslation.x
                translationY = currentTranslation.y
            }
    )
}

@Immutable // позначаємо клас як незмінний для оптимізації Compose
data class SquareUiState(
    val index: Int,
    val squareName: String,
    val piece: ChessPiece?,
    val isSelected: Boolean = false,
    val isEmptySquareSelected: Boolean = false,
    val isLastMove: Boolean = false,
    val isMoveMove: Boolean = false, // для передостаннього ходу та атакерів
    val isLegalMoveTarget: Boolean = false,
    val isChecked: Boolean = false,
    val isHintSquare: Boolean = false,
    val isHighlightedByBitboard: Boolean = false,
    val isVisitedSquare: Boolean = false, // для пазлів
    val shouldHidePiece: Boolean = false // ключ для плавної анімації
)

@Composable
fun RowScope.ChessSquare(
    state: SquareUiState,
    onSquareClick: (Int, Int) -> Unit
) {
    val theme = LocalUiSettings.current.boardTheme
    val row = state.index / 9
    val col = state.index % 9
    val cellColor = if ((row + col) % 2 == 0) theme.darkColor else theme.lightColor
    // Box із ChessBoard
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(cellColor)
            .clickable { onSquareClick(row, col) }
            .drawWithCache {
                // Кешуємо пензлі (Brushes) для градієнтів, щоб не створювати їх на кожному кадрі
                val radius = size.minDimension / 2f
                val hintBrush = Brush.radialGradient(colors = listOf(hintGlowColor, Color.Transparent), radius = radius)
                val captureBrush = Brush.radialGradient(colors = listOf(captureGlowColor, Color.Transparent), radius = radius)
                val checkBrush = Brush.radialGradient(colors = listOf(checkGlowColor, Color.Transparent), radius = radius)

                // Радіус для крапки легального ходу (приблизно відповідає твоєму fillMaxSize(0.34f))
                val dotRadius = size.minDimension * 0.17f

                onDrawBehind {
                    // малюємо шар за шаром знизу вгору
                    if (state.isLastMove) drawRect(lastMoveColor) // підсвічування останнього ходу (найнижчий шар)
                    if (state.isMoveMove) drawRect(moveMoveColor) // підсвічування передостаннього ходу і атакерів (другий шар)
                    if (state.isVisitedSquare) drawRect(Color.Black.copy(alpha = 0.5f)) // затемнення відвіданої клітинки
                    if (state.isHintSquare) drawRect(brush = hintBrush) // підсвітка підказки
                    if (state.isEmptySquareSelected) drawRect(Color.Black.copy(alpha = 0.28f)) // підсвічування порожньої клітинки
                    if (state.isLegalMoveTarget || state.isHighlightedByBitboard) { // підсвічування легальних ходів
                        if (state.piece != null) {
                            drawRect(brush = captureBrush) // підсвітка взяття темним градієнтом
                        } else {
                            drawCircle(color = legalMoveDotColor, radius = dotRadius) // крапка для порожньої клітинки
                        }
                    }
                    if (state.isSelected) drawRect(selectionColor) // підсвічування вибраної клітинки
                    if (state.isChecked) drawRect(brush = checkBrush) // підсвічування короля під шахом
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (state.squareName.isNotEmpty()) { // логіка відображення назв клітинок
            Text(
                text = state.squareName,
                color = if (cellColor == theme.darkColor) theme.lightColor.copy(alpha = 0.74f) else theme.darkColor.copy(alpha = 0.74f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp)
            )
        }
        if (LocalUiSettings.current.gameMode != "blindfold") { // відображення фігури (якщо вона наявна)
            val pieceToShow = if (state.shouldHidePiece) null else state.piece
            pieceToShow?.let {
                Image(
                    painter = painterResource(id = pieceToDrawableResource(it)),
                    contentDescription = "${it.color} ${it.type}",
                    modifier = Modifier.fillMaxSize(0.96f).align(Alignment.Center)
                )
            }
        }
    }
}

// функція ChessBoard відповідає виключно за шахову дошку, фігури, назви клітинок та підсвічування.
// дані для відображення вона отримує через свої параметри.
@Composable
fun ChessBoard(
    modifier: Modifier = Modifier, // параметр для розміру дошки
    boardState: List<SquareUiState>, // використовуємо імм'ютабл клас
    isFlipped: Boolean,
    onSquareClick: (Int, Int) -> Unit
) {
    val rows = if (isFlipped) 8 downTo 0 else 0 until 9
    // cols не потрібні, бо використовуємо index, але залишимо для ясності
    val cols = if (isFlipped) 8 downTo 0 else 0 until 9

    Column(
        modifier = modifier // застосовуємо зовнішній модифікатор розміру дошки
            .aspectRatio(1f)
    ) {
        for (row in rows) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in cols) {
                    val index = row * 9 + col // індекс дошки
                    key(index) {
                        ChessSquare(
                            state = boardState[index],
                            onSquareClick = onSquareClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedChessBoard(
    modifier: Modifier = Modifier,
    boardState: List<SquareUiState>,
    isFlipped: Boolean,
    animatedMoves: List<AnimatedMove>, // або single AnimatedMove
    onAnimationFinished: () -> Unit,
    onSquareClick: (Int, Int) -> Unit
) {
    var boardSizePx by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier.aspectRatio(1f).onGloballyPositioned {
        // оновлюємо тільки коли розмір реально змінився (наприклад ніколи)
        val newWidth = it.size.width.toFloat()
        if (boardSizePx != newWidth) {
            boardSizePx = newWidth
        }
    }) {
        ChessBoard(
            modifier = Modifier.fillMaxSize(),
            boardState = boardState,
            isFlipped = isFlipped,
            onSquareClick = onSquareClick
        )

        // не дозволяємо анімації виконуватися, доки екран не виміряно
        if (boardSizePx > 0f) {
            // рендер переліку анімацій (може бути 0,1 або >1 для рокірування)
            animatedMoves.forEach { anim ->
                FlyingPiece(
                    animatedMove = anim,
                    boardSizePx = boardSizePx,
                    isFlipped = isFlipped,
                    onFinished = onAnimationFinished
                )
            }
        }
    }
}


@Composable
private fun TrainingControlsPanel(
    onHintClick: () -> Unit,
    onAttackMapClick: () -> Unit,
    onShowThreats: () -> Unit,
    isShowingAttackers: Boolean // чи активна кнопка
    // сюди слід додати решту обробників, коли вони будуть готові
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // вертикальне розташування для альбомного режиму
        Column(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // кнопка підказки
            Button(
                onClick = onHintClick,
                modifier = Modifier
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF666632), // темний жовтий колір для підказки
                    contentColor = Color.White
                )
            ) {
                Text("💡", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка 2 "квадрати"
            Button(
                onClick = { /* TODO: Логіка для кнопки */ },
                modifier = Modifier
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonStyleColor, // темний зелений колір
                    contentColor = Color.White
                )
            ) {
                Text("#", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка 3
            Button(
                onClick = onShowThreats,
                modifier = Modifier
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242), // світлий сірий колір
                    contentColor = Color.White
                )
            ) {
                Text("#", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка мап атак
            Button(
                onClick = onAttackMapClick,
                modifier = Modifier
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242), // світлий сірий
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626), // темніший сірий
                    disabledContentColor = Color(0xFF808080)   // темний сірий для іконки
                )
            ) { Text("⚔️", fontSize = 24.sp) }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка 5
            Button(
                onClick = {/* TODO: Логіка для кнопки */ },
                modifier = Modifier
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242), // світліший сірий
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626), // темніший сірий
                    disabledContentColor = Color(0xFF808080)   // темний сірий для іконки
                )
            ) {
                Text("#", fontSize = 24.sp)
            }
        }
    } else {
        // Горизонтальне розташування для портретного режиму (стара логіка)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // кнопка підказки
            Button(
                onClick = onHintClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF666632), // темний жовтий колір для підказки
                    contentColor = Color.White
                )
            ) {
                Text("💡", fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка 2 "квадрати"
            Button(
                onClick = { /* TODO: Логіка для кнопки */ },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonStyleColor, // темний зелений колір
                    contentColor = Color.White
                )
            ) {
                Text("#", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка підсвітки загроз
            Button(
                onClick = onShowThreats,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isShowingAttackers) goldButtonColor else Color(0xFF424242), // світлий сірий колір
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626), // темніший сірий для неактивного стану
                    disabledContentColor = Color(0xFF808080)
                )
            ) {
                Text("🎯", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка мап атак
            Button(
                onClick = onAttackMapClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242), // світлий сірий
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626), // темніший сірий
                    disabledContentColor = Color(0xFF808080)   // темний сірий для іконки
                )
            ) { Text("⚔️", fontSize = 26.sp) }

            Spacer(modifier = Modifier.width(4.dp))

            // кнопка 5
            Button(
                onClick = {/* TODO: Логіка для кнопки */ },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF424242), // світліший сірий
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF262626), // темніший сірий
                    disabledContentColor = Color(0xFF808080)   // темний сірий для іконки
                )
            ) {
                Text("#", fontSize = 26.sp)
            }
        }
    }
}

data class SavedGameData(
    val historyMoves: List<Int>,
    val notation: List<String>,
    val whiteTimeMs: Long,
    val blackTimeMs: Long,
    val outcome: String?,
    val timestamp: Long
)

object GameArchiveCache {
    var selectedGame: SavedGameData? = null
}

// клас для керування життєвим циклом C++ сесії
class GameSessionManager(initialTimerDuration: Int) {
    // вказівник суворо приватний. ззовні доступу бути не повинно
    private var sessionPtr: Long = Engine81Bridge.createGameSessionJNI(initialTimerDuration)

    // перезапуск гри (перестворення сесії)
    fun restartSession(timerDuration: Int) {
        destroy() // обов'язково знищуємо стару
        sessionPtr = Engine81Bridge.createGameSessionJNI(timerDuration)
    }

    // безпечне знищення
    fun destroy() {
        if (sessionPtr != 0L) {
            Engine81Bridge.destroyGameSessionJNI(sessionPtr)
            sessionPtr = 0L
        }
    }

    // тест валідності сесії (корисно для уникнення крашів)
    fun isValid(): Boolean = sessionPtr != 0L

    // ігрова логіка (основні дії)

    // зробити хід
    fun makeMove(from: Int, to: Int, flags: Int): JniMoveResult {
        val encodedMove = (from shl 16) or (to shl 8) or flags
        return Engine81Bridge.makeMoveIncrementalJNI(sessionPtr, encodedMove)
    }
    // скасувати хід
    fun undoMove(): JniGameStateUpdate = Engine81Bridge.undoMoveJNI(sessionPtr)
    // повернути хід (Redo)
    fun redoMove(): JniGameStateUpdate = Engine81Bridge.redoMoveJNI(sessionPtr)
    // отримати легальні ходи для конкретної клітинки
    fun getLegalDestinations(fromSquare: Int): IntArray {
        return Engine81Bridge.getLegalDestinationsForSquareJNI(sessionPtr, fromSquare)
    }

    // отримання даних і стану гри - поточного або історичного - (Getters) // historyIndex = -1 для поточного
    fun getGameState(historyIndex: Int = -1): JniGameStateUpdate {
        return Engine81Bridge.getStateForHistoryMoveJNI(sessionPtr, historyIndex)
    }
   
    fun getHistoryMoves(): IntArray = Engine81Bridge.getHistoryMovesJNI(sessionPtr) // отримання історії ходів
    fun getHistoryInfo(): IntArray = Engine81Bridge.getHistoryInfoJNI(sessionPtr) // отримання інформації про історію (індексів)
    
    // функція аналізує хід в історії та повертає відповідний тип звуку
    fun getSoundTypeForPly(ply: Int): ChessSoundPlayer.SoundType? {
        val historyMoves = getHistoryMoves()
        
        if (ply < 0 || ply >= historyMoves.size) return null // якщо індекс поза межами (наприклад, стартова позиція -1), звук відсутній

        val encodedMove = historyMoves[ply]
        val flags = encodedMove and 0xFF

        // отримуємо стан після цього ходу, щоб перевірити на шах
        val stateAfterMove = getGameState(ply)
        val isCheck = findCheckedKing(stateAfterMove) != null

        return when {
            isCheck -> ChessSoundPlayer.SoundType.CHECK
            flags == 1 -> ChessSoundPlayer.SoundType.TAKE   // взяття
            flags == 4 -> ChessSoundPlayer.SoundType.CASTLE // рокірування
            flags == 3 -> ChessSoundPlayer.SoundType.EN     // взяття на проході
            else -> ChessSoundPlayer.SoundType.MOVE
        }
    }

    // відновлення збереженої гри
    fun loadSession(historyMoves: IntArray, whiteTimeMs: Long, blackTimeMs: Long) {
        destroy() // обов'язково знищуємо поточну сесію перед завантаженням нової
        sessionPtr = Engine81Bridge.loadGameSessionJNI(historyMoves, whiteTimeMs, blackTimeMs)
    }

    // перевірка статусу (таймер, перемога)
    fun checkStatus(): JniGameStatus? = Engine81Bridge.checkGameStatusJNI(sessionPtr)

    fun findCheckedKing(state: JniGameStateUpdate): Int? {
        val kingColorToCheck = state.sideToMove
        val opponentColor = if (kingColorToCheck == 0) 1 else 0
        val opponentAttackMap = Engine81Bridge.getAttackMapJNI(state.pieceBitboards, opponentColor)
        val kingPieceIndex = if (kingColorToCheck == 0) 6 else 13 // WHITE_KING or BLACK_KING
        val kingBitboard = state.pieceBitboards[kingPieceIndex]
        
        val kingSquare = (0..80).find { isBitSet(kingBitboard, it) } // знаходимо клітинку короля

        // перевіряємо, чи клітинка короля під атакою
        return if (kingSquare != null && isBitSet(opponentAttackMap, kingSquare)) {
            kingSquare
        } else {
            null
        }
    }

    // позапланові завершення (здача, нічия)
    fun resign(playerColor: Int): JniMoveResult? = Engine81Bridge.resignJNI(sessionPtr, playerColor)
    fun drawByAgreement(): JniMoveResult? = Engine81Bridge.drawByAgreementJNI(sessionPtr)

    // комп'ютер та аналіз

    fun findBestMove(depth: Int): String {
        return Engine81Bridge.findBestMoveJNI(sessionPtr, depth)
    }

    fun generateAllMoves(
        pieceBitboards: Array<LongArray>,
        sideToMove: Int,
        castlingRights: Int,
        enPassantSquare: Int
    ): IntArray {
        return Engine81Bridge.generateAllMovesJNI(pieceBitboards, sideToMove, castlingRights, enPassantSquare)
    }

    fun stopSearch() { Engine81Bridge.stopSearchJNI() }

    // отримання мапи атак
    fun getAttackMap(pieceBitboards: Array<LongArray>, color: Int): LongArray {
        return Engine81Bridge.getAttackMapJNI(pieceBitboards, color)
    }

    // самоаналіз (Training Mode) - використовує статичні методи Bridge, але можна додати сюди для зручності

    // аналіз та візуалізація (Attack Maps, Bitboards)
}

class ChessViewModelFactory(private val context: Context, private val isBotGame: Boolean = false) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChessViewModel::class.java)) {
            val settingsManager = SettingsManager(context)
            val timerKey = if (isBotGame) SettingsManager.KEY_BOT_TIMER_DURATION else SettingsManager.KEY_TIMER_DURATION // ключ залежить від екрану, що викликав фабрику
            val timerDuration = settingsManager.getSetting( timerKey, "0" ).toIntOrNull() ?: 0 // читаємо налаштування таймера
            // визначаємо ключ режиму гри
            val modeKey = if (isBotGame) "bot" else "local"
            
            @Suppress("UNCHECKED_CAST")
            return ChessViewModel(timerDuration, settingsManager, modeKey) as T // передаємо параметри у ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChessViewModel(initialTimerDurationMinutes: Int, private val settingsManager: SettingsManager, private val gameModeKey: String) : ViewModel() {
    // ініціюємо менеджер сесії
    private val sessionManager = GameSessionManager(initialTimerDurationMinutes)

//    // використовуємо його вказівник (тимчасово, поки не перенесемо всі методи в Manager)
//    internal val sessionPtr: Long
//        get() = sessionManager.sessionPtr    // sessionPtr можна змінювати при перезапуску гри

    // потік даних (StateFlow) з поточним станом гри, на який підпишемо UI
    private val _gameState = MutableStateFlow(sessionManager.getGameState(-1)) // отримуємо початковий стан

    val gameState: StateFlow<JniGameStateUpdate> = _gameState

    // окремий потік mailbox для швидкого оновлення екрану
    private val _mailbox = MutableStateFlow(_gameState.value.mailbox)
    val mailbox: StateFlow<IntArray> = _mailbox

    // потік для відображення UI стану
    private val _uiState = MutableStateFlow(EngineUiState())
    val uiState: StateFlow<EngineUiState> = _uiState

    // локальні налаштування для логіки анімації
    private var isAnimationEnabled: Boolean = true
    private var visualGameMode: String = "normal"
    
    private var searchJob: Job? = null // посилання на поточну корутину пошуку
    private var latestValidEngineId: Long = -1L // змінна для відстеження ID пошуку з рушія та ігнорування запізнілих результатів
    private fun invalidateBotSearch() {
//        latestValidEngineId++ // збільшуємо покоління, інвалідуючи активний пошук
        searchJob?.cancel() // скасовуємо корутину. це відкине будь-який результат з рушія
        sessionManager.stopSearch() // зупиняємо рушій, звільняючи потік
    }
    
    // функція для синхронізації налаштувань з UI
    fun updateRuntimeSettings(animationEnabled: Boolean, gameMode: String, cellNameMode: CellNameMode) {
        this.isAnimationEnabled = animationEnabled
        this.visualGameMode = gameMode

        // оновлюємо налаштування в uiState
        _uiState.update { it.copy(cellNameMode = cellNameMode) }

        // викликаємо перерахунок дошки, щоб застосувати нові імена
        updateFullBoardState()
    }

    // допоміжна функція для конвертації коду фігури з C++ в об'єкт ChessPiece
    // створюємо глобальний кеш фігур
    object PieceCache {
        private val cache = Array(14) { code ->
            val pieceType = when (code % 7) {
                0 -> PieceType.PAWN
                1 -> PieceType.KNIGHT
                2 -> PieceType.BISHOP
                3 -> PieceType.GUARD
                4 -> PieceType.ROOK
                5 -> PieceType.QUEEN
                else -> PieceType.KING
            }
            val color = if (code < 7) PlayerColor.WHITE else PlayerColor.BLACK
            ChessPiece(pieceType, color)
        }

        fun get(code: Int): ChessPiece? {
            if (code == 14) return null // NO_PIECE
            return cache[code]
        }
    }

    // єдиний стан UI для всіх екранів, що працюють з рушієм.
    data class EngineUiState(
        val humanPlayerColor: Int = 0, // 0-white, 1-black
        val isBoardFlipped: Boolean = false,
        val autoFlipEnabled: Boolean = true,
        val outcome: String? = null,
        val lastMove: Triple<Int, Int, Int>? = null, // from, to, flags
        val penultMove: Triple<Int, Int, Int>? = null, // передостанній хід
        val selectedSquare: Int? = null, // клітинка, вибрана гравцем
        val legalMovesForSelected: List<Int> = emptyList(), // легальні ходи для неї
        val selectedEmptySquare: Int? = null, // для підсвітки порожньої клітинки
        val legalMoves: List<Triple<Int, Int, Int>> = emptyList(), // from, to, flags
        val promotionCandidate: Pair<Int, Int>? = null, // from, to
        val premove: Pair<Int, Int>? = null, // зберігає хід (from, to)
        val premoveEnabled: Boolean = true,  // за замовчуванням увімкнено
        val hintFromSquare: Int? = null, // для підказки
        val highlightedAttackMap: LongArray? = null,
        val highlightedAttackersBitboard: LongArray? = null, // підсвітка фігур, що атакують клітинку
        val isShowingAttackers: Boolean = false,
        val attackMapShownFor: Int? = null, // 0-white, 1-black
        val benchmarkResult: String? = null, // для виводу результатів бенчмарку
        val benchmarkHistory: List<String> = emptyList(), // для бенчмарку - історія результатів для порівняння
        val engineSearchDepth: Int = 4,
        val checkedKingSquare: Int? = null,
        val isSearchRunning: Boolean = false, // прапор для індикатора завантаження
        val notationHistory: List<String> = emptyList(),
        val whiteCaptured: List<Int> = emptyList(), // переліки взятих фігур
        val blackCaptured: List<Int> = emptyList(),
        val materialAdvantage: Int = 0, // для дисплеїв переваги
        val whiteTimeRemainingMs: Long? = null,
        val blackTimeRemainingMs: Long? = null,
        val hintToSquare: Int? = null, // стани для підказки кращого ходу
        val isViewingHistory: Boolean = false,
        val currentHistoryViewIndex: Int = -1,
        val searchStatsHistory: List<EngineStats> = emptyList(), // статистика останнього ходу бота
        val isStatsPanelVisible: Boolean = false,  // чи показувати панель статистики
        val showPreGameDialog: Boolean = false, // діалог налаштувань у грі з ботом
        val isTrainingGameMode: Boolean = false, // прапор, що вказує на режим самоаналізу
        val cellNameMode: CellNameMode = CellNameMode.ALL, // імена клітинок
        val placedPieces: Map<Int, ChessPiece> = emptyMap(),
        val activePieceToPlace: ChessPiece? = null,
        val trainingGameSideToMove: Int = 0, // чий хід симулюємо
        val animatedMove: AnimatedMove? = null, // анімація переміщення фігур
        val squaresToHideWhileAnimating: Set<Int> = emptySet() // приховуємо фігуру під анімацією
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EngineUiState

            if (humanPlayerColor != other.humanPlayerColor) return false
            if (isBoardFlipped != other.isBoardFlipped) return false
            if (autoFlipEnabled != other.autoFlipEnabled) return false
            if (selectedSquare != other.selectedSquare) return false
            if (selectedEmptySquare != other.selectedEmptySquare) return false
            if (premoveEnabled != other.premoveEnabled) return false
            if (hintFromSquare != other.hintFromSquare) return false
            if (isShowingAttackers != other.isShowingAttackers) return false
            if (attackMapShownFor != other.attackMapShownFor) return false
            if (engineSearchDepth != other.engineSearchDepth) return false
            if (checkedKingSquare != other.checkedKingSquare) return false
            if (isSearchRunning != other.isSearchRunning) return false
            if (materialAdvantage != other.materialAdvantage) return false
            if (whiteTimeRemainingMs != other.whiteTimeRemainingMs) return false
            if (blackTimeRemainingMs != other.blackTimeRemainingMs) return false
            if (hintToSquare != other.hintToSquare) return false
            if (isViewingHistory != other.isViewingHistory) return false
            if (currentHistoryViewIndex != other.currentHistoryViewIndex) return false
            if (isStatsPanelVisible != other.isStatsPanelVisible) return false
            if (showPreGameDialog != other.showPreGameDialog) return false
            if (isTrainingGameMode != other.isTrainingGameMode) return false
            if (trainingGameSideToMove != other.trainingGameSideToMove) return false
            if (outcome != other.outcome) return false
            if (lastMove != other.lastMove) return false
            if (penultMove != other.penultMove) return false
            if (legalMovesForSelected != other.legalMovesForSelected) return false
            if (legalMoves != other.legalMoves) return false
            if (promotionCandidate != other.promotionCandidate) return false
            if (premove != other.premove) return false
            if (!highlightedAttackMap.contentEquals(other.highlightedAttackMap)) return false
            if (!highlightedAttackersBitboard.contentEquals(other.highlightedAttackersBitboard)) return false
            if (benchmarkResult != other.benchmarkResult) return false
            if (benchmarkHistory != other.benchmarkHistory) return false
            if (notationHistory != other.notationHistory) return false
            if (whiteCaptured != other.whiteCaptured) return false
            if (blackCaptured != other.blackCaptured) return false
            if (searchStatsHistory != other.searchStatsHistory) return false
            if (cellNameMode != other.cellNameMode) return false
            if (placedPieces != other.placedPieces) return false
            if (activePieceToPlace != other.activePieceToPlace) return false
            if (animatedMove != other.animatedMove) return false
            if (squaresToHideWhileAnimating != other.squaresToHideWhileAnimating) return false

            return true
        }

        override fun hashCode(): Int {
            var result = humanPlayerColor
            result = 31 * result + isBoardFlipped.hashCode()
            result = 31 * result + autoFlipEnabled.hashCode()
            result = 31 * result + (selectedSquare ?: 0)
            result = 31 * result + (selectedEmptySquare ?: 0)
            result = 31 * result + premoveEnabled.hashCode()
            result = 31 * result + (hintFromSquare ?: 0)
            result = 31 * result + isShowingAttackers.hashCode()
            result = 31 * result + (attackMapShownFor ?: 0)
            result = 31 * result + engineSearchDepth
            result = 31 * result + (checkedKingSquare ?: 0)
            result = 31 * result + isSearchRunning.hashCode()
            result = 31 * result + materialAdvantage
            result = 31 * result + (whiteTimeRemainingMs?.hashCode() ?: 0)
            result = 31 * result + (blackTimeRemainingMs?.hashCode() ?: 0)
            result = 31 * result + (hintToSquare ?: 0)
            result = 31 * result + isViewingHistory.hashCode()
            result = 31 * result + currentHistoryViewIndex
            result = 31 * result + isStatsPanelVisible.hashCode()
            result = 31 * result + showPreGameDialog.hashCode()
            result = 31 * result + isTrainingGameMode.hashCode()
            result = 31 * result + trainingGameSideToMove
            result = 31 * result + (outcome?.hashCode() ?: 0)
            result = 31 * result + (lastMove?.hashCode() ?: 0)
            result = 31 * result + (penultMove?.hashCode() ?: 0)
            result = 31 * result + legalMovesForSelected.hashCode()
            result = 31 * result + legalMoves.hashCode()
            result = 31 * result + (promotionCandidate?.hashCode() ?: 0)
            result = 31 * result + (premove?.hashCode() ?: 0)
            result = 31 * result + (highlightedAttackMap?.contentHashCode() ?: 0)
            result = 31 * result + (highlightedAttackersBitboard?.contentHashCode() ?: 0)
            result = 31 * result + (benchmarkResult?.hashCode() ?: 0)
            result = 31 * result + benchmarkHistory.hashCode()
            result = 31 * result + notationHistory.hashCode()
            result = 31 * result + whiteCaptured.hashCode()
            result = 31 * result + blackCaptured.hashCode()
            result = 31 * result + searchStatsHistory.hashCode()
            result = 31 * result + cellNameMode.hashCode()
            result = 31 * result + placedPieces.hashCode()
            result = 31 * result + (activePieceToPlace?.hashCode() ?: 0)
            result = 31 * result + (animatedMove?.hashCode() ?: 0)
            result = 31 * result + squaresToHideWhileAnimating.hashCode()
            return result
        }
    }

    // compose відстежує зміни в елементах публічного змінного переліку стану
    val boardUiState = mutableStateListOf<SquareUiState>()

    // функція оновлення стану дошки
    private fun updateFullBoardState() {
        val gameState = _gameState.value
        val uiState = _uiState.value
        val mailbox = _mailbox.value // використовуємо оновлений _mailbox
        val isFlipped = uiState.isBoardFlipped
        val cellNameMode = uiState.cellNameMode

        // попередньо розраховуємо lookup масив для швидкої перевірки легальних ходів
        val legalMoveDestinations = BooleanArray(81)
        uiState.legalMovesForSelected.forEach { encodedMove ->
            val to = (encodedMove shr 8) and 0xFF
            if (to in 0..80) legalMoveDestinations[to] = true
        }

        // розпаковуємо бітборд атак швидкою бітовою функцією
        val attackMapIndices = uiState.highlightedAttackMap?.let { extractIndicesFromBitboard(it) } ?: emptySet()
        val attackersIndices = uiState.highlightedAttackersBitboard?.let { extractIndicesFromBitboard(it) } ?: emptySet()

        // відкриваємо транзакцію стану
        Snapshot.withMutableSnapshot {
            for (index in 0..80) { // один цикл для оновлення всіх 81 клітинок
                val piece = ChessPiece.fromCode(mailbox[index])
                val isSelected = uiState.selectedSquare == index
                val lastMoveFrom = uiState.lastMove?.first
                val lastMoveTo = uiState.lastMove?.second
                val isLastMove = lastMoveFrom == index || lastMoveTo == index

                val isLegalMoveTarget = legalMoveDestinations[index]
                val isChecked = uiState.checkedKingSquare == index

                val squareName = when (cellNameMode) {
                    CellNameMode.HIDDEN -> ""
                    CellNameMode.ALL -> squareNamesAll[index]
                    CellNameMode.EDGES -> if (isFlipped) squareNamesEdgesForBlack[index] else squareNamesEdges[index]
                }

                val isHighlighted = attackMapIndices.contains(index) || attackersIndices.contains(index)

                // оновлюємо елемент у списку за індексом. compose відстежить цю зміну та оновить лише окремі клітинки. ці 81 присвоєння тепер відбуваються одним снепшотом
                boardUiState[index] = SquareUiState(
                    index = index,
                    squareName = squareName,
                    piece = piece,
                    isSelected = isSelected,
                    isLastMove = isLastMove,
                    isLegalMoveTarget = isLegalMoveTarget,
                    isChecked = isChecked,
                    shouldHidePiece = uiState.squaresToHideWhileAnimating.contains(index),
                    isEmptySquareSelected = uiState.selectedEmptySquare == index,
                    isMoveMove = false, // поки що не реалізовано
                    isHintSquare = uiState.hintFromSquare == index || uiState.hintToSquare == index,
                    isHighlightedByBitboard = isHighlighted,
                    isVisitedSquare = false // для пазлів
                )
            }
        }
        // Compose дізнається про зміну переліку лише раз, коли блок закривається
    }

    private val gson = Gson()
    private var sessionStartTimestamp: Long = System.currentTimeMillis()

    init {
        // заповнюємо перелік початковими порожніми значеннями, щоб звертатися до елементів за індексом
        repeat(81) { index ->
            boardUiState.add(SquareUiState(index = index, squareName = "", piece = null))
        }

        // відновлюємо колір гравця з налаштувань
        val savedColor = settingsManager.getSetting("human_color_$gameModeKey", "0").toIntOrNull() ?: 0
        _uiState.update { it.copy(
            humanPlayerColor = savedColor,
            isBoardFlipped = savedColor == 1
        )}

        var isRestoredGameTimed: Boolean? = null // змінна стану таймера відновленої гри

        // спроба відновити незавершену гру
        val unfinishedGameJson = settingsManager.getSetting("unfinished_$gameModeKey", "")
        if (unfinishedGameJson.isNotEmpty()) {
            try {
                val savedGame = gson.fromJson(unfinishedGameJson, SavedGameData::class.java)
                sessionStartTimestamp = savedGame.timestamp // відновлюємо оригінальний ID
               
                isRestoredGameTimed = savedGame.whiteTimeMs >= 0 || savedGame.blackTimeMs >= 0 // перевіряємо, чи був у цієї гри таймер
               
                sessionManager.loadSession( // відновлюємо сесію в C++
                    savedGame.historyMoves.toIntArray(),
                    savedGame.whiteTimeMs,
                    savedGame.blackTimeMs
                )

                // отримуємо стан на останній хід
                val lastPly = savedGame.historyMoves.size - 1
                val restoredState = sessionManager.getGameState(lastPly)

                _gameState.value = restoredState
                _mailbox.value = restoredState.mailbox

                // витягуємо координати останнього ходу для підсвітки
                val lastMoveEncoded = savedGame.historyMoves.lastOrNull()
                val lastMoveTriple = lastMoveEncoded?.let {
                    Triple(it shr 16, (it shr 8) and 0xFF, it and 0xFF)
                }

                _uiState.update { it.copy(
                    notationHistory = savedGame.notation,
                    whiteTimeRemainingMs = if (savedGame.whiteTimeMs >= 0) savedGame.whiteTimeMs else null,
                    blackTimeRemainingMs = if (savedGame.blackTimeMs >= 0) savedGame.blackTimeMs else null,
                    lastMove = lastMoveTriple,
                    checkedKingSquare = sessionManager.findCheckedKing(restoredState),
                    showPreGameDialog = false // ховаємо діалог
                )}
            } catch (e: Exception) {
                // якщо формат старий або пошкоджений, ігноруємо і почнеться нова гра
            }
        }

        // викликаємо повне оновлення, щоб заповнити дошку початковою позицією
        updateFullBoardState()
        
        // якщо гру відновлено, беремо її стан таймера. якщо це нова гра - перевіряємо налаштування.
        if (isRestoredGameTimed ?: (initialTimerDurationMinutes > 0)) {
            startStatusPolling()
        }
    }

    private fun autoSaveGame(outcome: String? = null) {
        // запускаємо у фоновому потоці, щоб не гальмувати UI
        viewModelScope.launch(Dispatchers.IO) {
           
            val historyMovesArray = sessionManager.getHistoryMoves() ?: intArrayOf() // отримуємо сирий масив IntArray від рушія
            val historyMovesList = historyMovesArray.toList() // перетворюємо масив у список (List<Int>)

            // не зберігаємо порожні незавершені ігри (де не зроблено жодного ходу)
            if (historyMovesList.isEmpty() && outcome == null) return@launch

            val status = sessionManager.checkStatus()

            val gameData = SavedGameData( // об'єкт для збереження
                historyMoves = historyMovesList,
                notation = _uiState.value.notationHistory,
                whiteTimeMs = status?.whiteTimeMs ?: -1L,
                blackTimeMs = status?.blackTimeMs ?: -1L,
                outcome = outcome,
                timestamp = sessionStartTimestamp
            )

            val jsonString = gson.toJson(gameData)

            if (outcome == null) {
                // гра триває - оновлюємо слот
                settingsManager.saveSetting("unfinished_$gameModeKey", jsonString)
            } else {
                // гра завершена - переносимо в архів і чистимо слот
                archiveGame(jsonString)
                settingsManager.saveSetting("unfinished_$gameModeKey", "")
            }
        }
    }

    // допоміжна функція для архіву
    private fun archiveGame(jsonString: String) {
        val archiveStr = settingsManager.getSetting("games_archive", "")
        val archive = if (archiveStr.isNotEmpty()) archiveStr.split("|||").toMutableList() else mutableListOf()
        
        val newGameData = gson.fromJson(jsonString, SavedGameData::class.java) // читаємо timestamp з нової гри
        // видаляємо старий запис цієї ж партії (якщо ми переграли кінцівку через Undo)
        archive.removeAll { it.contains("\"timestamp\":${newGameData.timestamp}") }

        archive.add(0, jsonString) // додаємо на початок переліку

        settingsManager.saveSetting("games_archive", archive.joinToString("|||"))
    }

    // звукові ефекти
    private val _soundEffect = MutableSharedFlow<ChessSoundPlayer.SoundType>()
    val soundEffect = _soundEffect.asSharedFlow()

    init {
        // запускаємо опитування таймера для сесії, якщо вона створена з таймером.
        if (initialTimerDurationMinutes > 0) {
            startStatusPolling()
        }
    }

    private var pollingJob: Job? = null // зберігаємо посилання на корутину

    // слухач стану гри
    private fun startStatusPolling() {
        // зупиняємо попереднє опитування, якщо воно було
        pollingJob?.cancel()
        // запускаємо нове і зберігаємо посилання на нього
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) { // цикл працює, поки ViewModel жива
                // робимо паузу
                delay(250) // 4-ьох разів на секунду достатньо
                
                if (_uiState.value.outcome != null) break // якщо гра завершена, зупиняємо цикл
               
                val statusUpdate = sessionManager.checkStatus() // викликаємо JNI-функцію через менеджер

                if (statusUpdate != null) {
                    withContext(Dispatchers.Main) { // оновлюємо стан в головному потоці
                       
                        _uiState.update { it.copy( // оновлюємо час, що його показує UI
                            whiteTimeRemainingMs = statusUpdate.whiteTimeMs,
                            blackTimeRemainingMs = statusUpdate.blackTimeMs
                        )}

                        // перевіряємо, чи гра не завершилась по часу і зберігаємо, якщо так
                        when (statusUpdate.status) {
                            "TIMEOUT_WHITE" -> {
                                val msg = "Чорні перемогли по часу"
                                _uiState.update { it.copy(outcome = msg) }
                                autoSaveGame(msg)
                            }
                            "TIMEOUT_BLACK" -> {
                                val msg = "Білі перемогли по часу"
                                _uiState.update { it.copy(outcome = msg) }
                                autoSaveGame(msg)
                            }
                            "TIMEOUT_MATERIAL" -> {
                                val msg = "Нічия через вичерпання часу і нестачу матеріалу"
                                _uiState.update { it.copy(outcome = msg) }
                                autoSaveGame(msg)
                            }
                        }
                    }
                }
            }
        }
    }

//    // метод для ручного старту таймера (в грі онлайн чи наживо)
//    fun manualStartTimer() {
//        Engine81Bridge.startTimerManualJNI(sessionPtr)
//    }

    /** оновлює лише виділення  і лише змінені клітинки */
    private fun updateSelectionState(oldIndex: Int?, newIndex: Int?, isEmptySquare: Boolean = false) {
        // скидаємо підсвітку зі старої клітинки, якщо вона була
        oldIndex?.let {
            boardUiState[it] = boardUiState[it].copy(isSelected = false, isEmptySquareSelected = false)
        }
        // встановлюємо підсвітку на нову клітинку
        newIndex?.let {
            boardUiState[it] = boardUiState[it].copy(
                isSelected = !isEmptySquare,
                isEmptySquareSelected = isEmptySquare
            )
        }
    }

    /** оновлює лише підсвітку легальних ходів і лише змінені клітинки */
    private fun updateLegalMovesState(oldMoves: List<Int>, newMoves: List<Int>) {
        // прибираємо стару підсвітку
        oldMoves.forEach { encodedMove ->
            val to = (encodedMove shr 8) and 0xFF
            if (to in 0..80) {
                boardUiState[to] = boardUiState[to].copy(isLegalMoveTarget = false)
            }
        }
        // додаємо нову підсвітку
        newMoves.forEach { encodedMove ->
            val to = (encodedMove shr 8) and 0xFF
            if (to in 0..80) {
                boardUiState[to] = boardUiState[to].copy(isLegalMoveTarget = true)
            }
        }
    }

    /** застосовує інкрементальне оновлення з JNI до boardUiState. оновлює лише клітинки, що змінилися під час ходу */
    private fun applyIncrementalUpdate(uiUpdate: IntArray, squaresToHide: Set<Int> = emptySet()) {
        when (uiUpdate[0]) {
            0 -> { // звичайний хід, взяття, перетворення
                val fromSq = uiUpdate[1]
                val toSq = uiUpdate[2]
                val pieceCode = uiUpdate[3]
                // оновлюємо лише дві клітинки, враховуючи необхідність приховання
                boardUiState[fromSq] = boardUiState[fromSq].copy(
                    piece = null,
                    shouldHidePiece = squaresToHide.contains(fromSq)
                )
                boardUiState[toSq] = boardUiState[toSq].copy(
                    piece = ChessPiece.fromCode(pieceCode),
                    shouldHidePiece = squaresToHide.contains(toSq)
                )
            }
            1 -> { // взяття на проході (en passant)
                val fromSq = uiUpdate[1]
                val toSq = uiUpdate[2]
                val movedPawnCode = uiUpdate[3]
                val capturedPawnSq = uiUpdate[4]
                // оновлюємо три клітинки
                boardUiState[fromSq] = boardUiState[fromSq].copy(piece = null)
                boardUiState[toSq] = boardUiState[toSq].copy(piece = ChessPiece.fromCode(movedPawnCode))
                boardUiState[capturedPawnSq] = boardUiState[capturedPawnSq].copy(piece = null)
            }
            2 -> { // рокірування (castle)
                val kingFrom = uiUpdate[1]
                val kingTo = uiUpdate[2]
                val kingPieceCode = uiUpdate[3]
                val rookFrom = uiUpdate[4]
                val rookTo = uiUpdate[5]
                val rookPieceCode = uiUpdate[6]
                // оновлюємо чотири клітинки
                // король приховується, якщо він анімується
                boardUiState[kingFrom] = boardUiState[kingFrom].copy(
                    piece = null,
                    shouldHidePiece = squaresToHide.contains(kingFrom)
                )
                boardUiState[kingTo] = boardUiState[kingTo].copy(
                    piece = ChessPiece.fromCode(kingPieceCode),
                    shouldHidePiece = squaresToHide.contains(kingTo)
                )
                // тура переміщується миттєво, тому тут прапор false (або треба додати другу анімацію)
                boardUiState[rookFrom] = boardUiState[rookFrom].copy(piece = null)
                boardUiState[rookTo] = boardUiState[rookTo].copy(piece = ChessPiece.fromCode(rookPieceCode))
            }
        }
    }

    /** оновлює підсвітку останнього ходу */
    private fun updateLastMoveHighlight(oldMove: Triple<Int, Int, Int>?, newMove: Triple<Int, Int, Int>?) {
        // прибираємо підсвітку з клітинок попереднього ходу
        oldMove?.let { (from, to, _) ->
            if (from in boardUiState.indices) boardUiState[from] = boardUiState[from].copy(isLastMove = false)
            if (to in boardUiState.indices) boardUiState[to] = boardUiState[to].copy(isLastMove = false)
        }
        // встановлюємо підсвітку на клітинки нового ходу
        newMove?.let { (from, to, _) ->
            if (from in boardUiState.indices) boardUiState[from] = boardUiState[from].copy(isLastMove = true)
            if (to in boardUiState.indices) boardUiState[to] = boardUiState[to].copy(isLastMove = true)
        }
    }

    /** оновлює підсвітку шаху */
    private fun updateCheckHighlight(oldKingSquare: Int?, newKingSquare: Int?) {
        // якщо позиція короля не змінилася, нічого не робимо
        if (oldKingSquare == newKingSquare) return

        // прибираємо стару підсвітку
        oldKingSquare?.let {
            if (it in boardUiState.indices) boardUiState[it] = boardUiState[it].copy(isChecked = false)
        }
        // встановлюємо нову
        newKingSquare?.let {
            if (it in boardUiState.indices) boardUiState[it] = boardUiState[it].copy(isChecked = true)
        }
    }

    // обробник кліків по дошці
    fun handleSquareClick(clickedSquareIndex: Int, gameMode: String) {
        // виходимо, якщо гра завершена або зараз хід не гравця (для гри з ботом)
        if (_uiState.value.outcome != null) return
        if (gameMode == "bot" && _gameState.value.sideToMove != _uiState.value.humanPlayerColor) return

        val currentUiState = _uiState.value
        val currentSelectedSquare = _uiState.value.selectedSquare
        // визначаємо, чи наявна фігура на клітинці та якого вона кольору
        val pieceOnClickedSquare = ChessPiece.fromCode(_mailbox.value[clickedSquareIndex])
        val pieceColor = pieceOnClickedSquare?.color?.toJniValue()

        // логіка, коли фігура вже вибрана
        if (currentSelectedSquare != null) {
            val legalMoves = currentUiState.legalMovesForSelected
            // шукаємо клікнуту клітинку серед легальних ходів для вибраної фігури
            val move = legalMoves.find { ((it shr 8) and 0xFF) == clickedSquareIndex }

            // випадок 1: клікнули на легальний хід
            if (move != null) {
                val flags = move and 0xFF
//                val movingPieceIndex = (0..13).first { isBitSet(_gameState.value.pieceBitboards[it], from) }
//                val isPawn = movingPieceIndex == 0 || movingPieceIndex == 7 // WHITE_PAWN or BLACK_PAWN
//                val promotionRank = if (_gameState.value.sideToMove == 0) 0 else 8

                // перевірка на промоцію
                if (flags >= 8) { // спрощена логіка перевірки, всі прапори перетворення після 8
                // if (isPawn && (to / 9) == promotionRank) { // перевіряємо, чи це хід пішака на останню горизонталь
                    _uiState.update { it.copy(promotionCandidate = currentSelectedSquare to clickedSquareIndex) }
                } else {
                    makeMove(currentSelectedSquare, clickedSquareIndex, flags) // це звичайний хід, взяття або рокірування. викликаємо метод ViewModel
                }
                // скидаємо виділення після будь-якого ходу
                updateSelectionState(currentSelectedSquare, null)
                updateLegalMovesState(legalMoves, emptyList())
                _uiState.update { it.copy(selectedSquare = null, legalMovesForSelected = emptyList()) }
                return // завершуємо обробку
            }
            // випадок 2: клікнули на іншу свою фігуру -> перевибираємо
            else if (pieceColor == _gameState.value.sideToMove) {
                val newLegalMoves = getLegalDestinationsForSquare(clickedSquareIndex)
                // оновлюємо візуальні стани
                updateSelectionState(currentSelectedSquare, clickedSquareIndex)
                updateLegalMovesState(legalMoves, newLegalMoves)
                // оновлюємо логічний стан
                _uiState.update { it.copy(
                    selectedSquare = clickedSquareIndex,
                    legalMovesForSelected = newLegalMoves, // отримуємо перелік клітинок для вибраної фігури
                    selectedEmptySquare = null // скидаємо виділення порожньої клітинки
                )}
            }
            // випадок 3: клікнули на нелегальний хід - порожню клітинку або чужу фігуру -> скидаємо вибір
            else {
                updateSelectionState(currentSelectedSquare, null)
                updateLegalMovesState(legalMoves, emptyList())
                _uiState.update { it.copy(
                    selectedSquare = null,
                    legalMovesForSelected = emptyList(),
                    selectedEmptySquare = null // скидаємо виділення порожньої клітинки
                )}
            }
        }
        // логіка, коли жодна фігура не вибрана
        else {
            // випадок 4: клікнули на свою фігуру -> вибираємо її
            if (pieceColor == _gameState.value.sideToMove) {
                val legalMoves = getLegalDestinationsForSquare(clickedSquareIndex)
                updateSelectionState(currentUiState.selectedEmptySquare, clickedSquareIndex)
                updateLegalMovesState(emptyList(), legalMoves)
                _uiState.update { it.copy(
                    selectedSquare = clickedSquareIndex,
                    legalMovesForSelected = legalMoves, // отримуємо перелік клітинок для вибраної фігури
                    selectedEmptySquare = null // скидаємо виділення порожньої клітинки
                )}
            }
            // випадок 5: клікнули на порожню клітинку або чужу фігуру
            else {
                updateSelectionState(currentUiState.selectedEmptySquare, clickedSquareIndex, isEmptySquare = true)
                _uiState.update { it.copy(selectedEmptySquare = clickedSquareIndex) } // встановлюємо виділення порожньої клітинки
            }
        }
    }

    fun resolvePromotion(chosenPieceType: PieceType) {
        val candidate = _uiState.value.promotionCandidate ?: return
        val (from, to) = candidate
        val promotionFlag = promotionPieceTypeToFlag(chosenPieceType)
        makeMove(from, to, promotionFlag)

        // скидаємо стани після ходу
        _uiState.update { it.copy(
            promotionCandidate = null,
            selectedSquare = null,
            legalMovesForSelected = emptyList()
        )}
    }

    // потік даних для верхнього дисплея. `combine` для одночасного доступу до `_uiState` і `_gameState`
    val topPlayerDisplayData: StateFlow<PlayerDisplayData> = combine(_uiState, _gameState) { ui, game ->
        val isFlipped = ui.isBoardFlipped
        val advantage = ui.materialAdvantage

        // визначаємо, чиї взяті фігури показувати вгорі
        val captured = if (isFlipped) ui.blackCaptured else ui.whiteCaptured
        // визначаємо, чий таймер показувати вгорі
        val timeMs = if (isFlipped) ui.whiteTimeRemainingMs else ui.blackTimeRemainingMs

        // визначаємо, чи активний таймер
        val isWhitesTurn = game.sideToMove == 0
        val isTimerActive = (isFlipped && isWhitesTurn) || (!isFlipped && !isWhitesTurn)

        // розраховуємо перевагу для верхнього гравця
        val advantageDisplay = when {
            !isFlipped && advantage < 0 -> abs(advantage) // вгорі білі фігури (не перевернуто), і перевага у чорних
            isFlipped && advantage > 0 -> advantage    // вгорі чорні (перевернуто), і перевага у білих
            else -> 0
        }
        PlayerDisplayData(captured, advantageDisplay, timeMs, isTimerActive)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDisplayData())

    // потік даних для нижнього дисплея
    val bottomPlayerDisplayData: StateFlow<PlayerDisplayData> = combine(_uiState, _gameState) { ui, game ->
        val isFlipped = ui.isBoardFlipped
        val advantage = ui.materialAdvantage

        // визначаємо, чиї взяті фігури показувати внизу
        val captured = if (isFlipped) ui.whiteCaptured else ui.blackCaptured
        // визначаємо, чий таймер показувати вгорі
        val timeMs = if (isFlipped) ui.blackTimeRemainingMs else ui.whiteTimeRemainingMs

        // визначаємо, чи активний таймер
        val isWhitesTurn = game.sideToMove == 0
        val isBottomPlayerWhite = !isFlipped
        val isTimerActive = (isBottomPlayerWhite && isWhitesTurn) || (!isBottomPlayerWhite && !isWhitesTurn)

        // розраховуємо перевагу для нижнього гравця
        val advantageDisplay = when {
            !isFlipped && advantage > 0 -> advantage    // внизу чорні фігури (не перевернуто), і перевага у білих
            isFlipped && advantage < 0 -> abs(advantage) // внизу білі (перевернуто), і перевага у чорних
            else -> 0
        }
        PlayerDisplayData(captured, advantageDisplay, timeMs, isTimerActive)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDisplayData())

//    // плеєр звуків для кліків по історії
//    fun playSoundForHistoryMove(moveIndex: Int) {
//        // отримуємо масив всіх закодованих ходів
//        val historyMoves = sessionManager.getHistoryMoves()
//        if (moveIndex < 0 || moveIndex >= historyMoves.size) return
//
//        val encodedMove = historyMoves[moveIndex]
//        val flags = encodedMove and 0xFF // прапори ходу
//        val toSquare = (encodedMove shr 8) and 0xFF
//
//        // отримуємо стан після цього ходу, щоб перевірити на шах/мат
//        val stateAfterMove = sessionManager.getGameState(moveIndex)
//
//        // перевіряємо, чи був шах після цього ходу
//        val isCheck = sessionManager.findCheckedKing(stateAfterMove) != null
//
//        // визначаємо тип звуку (логіка, схожа на makeMove)
//        val soundType = when {
//            // TODO: потрібно витягти інформацію про мат, пат і інші нічиї та здачу з нотації історичного ходу
//            isCheck -> ChessSoundPlayer.SoundType.CHECK
//            flags == 1 -> ChessSoundPlayer.SoundType.TAKE // MoveFlags::CAPTURE
//            flags == 4 -> ChessSoundPlayer.SoundType.CASTLE // MoveFlags::CASTLE
//            flags == 3 -> ChessSoundPlayer.SoundType.EN // MoveFlags::EN_PASSANT
//            else -> ChessSoundPlayer.SoundType.MOVE
//        }
//
//        viewModelScope.launch { _soundEffect.emit(soundType) }
//    }

    // плеєр звуків для кліків по історії
    fun playSoundForHistoryMove(moveIndex: Int) {
        val soundType = sessionManager.getSoundTypeForPly(moveIndex) // запитуємо тип звуку у менеджера
        
        soundType?.let { // якщо отримали звук для цього ходу - відправляємо подію в UI
            viewModelScope.launch { _soundEffect.emit(it) }
        }
    }

    fun resign() {
        // отримуємо колір гравця, котрий натискає кнопку "Здатися"
        val humanColor = _uiState.value.humanPlayerColor

        // передаємо рушію колір того, хто здається, через менеджер
        val moveResult = sessionManager.resign(humanColor) ?: return

        // формуємо повідомлення для UI на основі статусу, повернутого рушієм
        val outcomeMessage = when (moveResult.gameStatus) {
            "RESIGNATION" -> {
                // визначаємо колір переможця
                val defeated = if (humanColor == 0) "Білі" else "Чорні"
                "$defeated здалися"
            }
            "INSUFFICIENT_MATERIAL_ON_RESIGN" -> "Нічия (суперник не має матеріалу для мату)"
            else -> "Гра завершена"
        }

        // оновлюємо стан UI з фінальними даними
        _uiState.update { state ->
            val currentHistory = state.notationHistory // дістаємо поточну історію
            // додаємо результат до останнього ходу через пробіл
            val updatedNotation = if (currentHistory.isNotEmpty()) {
                currentHistory.dropLast(1) + "${currentHistory.last()} ${moveResult.notation}" // фінальна нотація (1-0, 0-1 або ½-½) до історії
            } else {
                listOf(moveResult.notation) // якщо здалися до першого ходу
            }

            state.copy(
                notationHistory = updatedNotation,
                outcome = outcomeMessage
            )
        }

        // відтворюємо звук
        val soundType = if (moveResult.gameStatus == "RESIGNATION") {
            ChessSoundPlayer.SoundType.CHECKMATE // звук поразки
        } else {
            ChessSoundPlayer.SoundType.STALEMATE // звук нічиєї
        }
        viewModelScope.launch { _soundEffect.emit(soundType) }
        // зберігаємо гру
        autoSaveGame(outcomeMessage)
    }

    fun offerDraw() {
        // виклик через менеджер JNI функції, що обробляє нічию за згодою,
        val moveResult = sessionManager.drawByAgreement() ?: return

        // оновлюємо UI на основі даних, що повернув рушій, як і обробку мату чи пату
        // для гри з ботом тимчасово вважаємо, що він завжди згоден на нічию. пізніше потрібно додати логіку здачі/не-здачі через оцінку позиції
        _uiState.update { state ->
            val currentHistory = state.notationHistory // дістаємо поточну історію
            val updatedNotation = if (currentHistory.isNotEmpty()) {
                currentHistory.dropLast(1) + "${currentHistory.last()} ${moveResult.notation}" // додаємо фінальну нотацію "½-½" до історії
            } else {
                listOf(moveResult.notation)
            }

            state.copy(
                notationHistory = updatedNotation,
                outcome = "Нічия за згодою"  // встановлюємо результат гри для виводу діалогу
            )
        }
        
        viewModelScope.launch { _soundEffect.emit(ChessSoundPlayer.SoundType.STALEMATE) } // відтворюємо звук
        
        autoSaveGame("Нічия за згодою") // зберігаємо результат гри
    }

    fun dismissOutcome() {
        _uiState.update { it.copy(outcome = null) }
    }

    // методи керування грою, що викликають C++

    fun makeMove(from: Int, to: Int, flags: Int) {
        val encodedMove = (from shl 16) or (to shl 8) or flags

        // перевіряємо, чи не є цей хід спробою повторити скасований хід
        val historyInfo = sessionManager.getHistoryInfo()
        val currentHistoryIndex = historyInfo[0]
        val historyMoves = sessionManager.getHistoryMoves()

        if (currentHistoryIndex + 1 < historyMoves.size) {
            val nextMoveInHistory = historyMoves[currentHistoryIndex + 1]
            if (encodedMove == nextMoveInHistory) {
                // користувач вручну зробив той самий хід, що йде наступним в історії - просто виконуємо redo
                redoMove()
                return // виходимо, щоб не виконувати решту коду
            }
        }

        // запам'ятовуємо попередній стан для крапкового оновлення UI
        val oldUiState = _uiState.value
        val oldGameState = _gameState.value
        val movingPlayer = if (oldGameState.sideToMove == 0) "Білі" else "Чорні"
        val movingPiece = ChessPiece.fromCode(_mailbox.value[from]) ?: return // виходимо, якщо фігури немає
        // запам'ятовуємо індекс історії до (!) виконання нового ходу
        val activeIndexBeforeMove = sessionManager.getHistoryInfo()[0]

        // логіка анімації
        val animationData = AnimatedMove(movingPiece, from, to)
        // визначаємо, чи потрібно запускати анімацію, - чи увімкнено в налаштуваннях і чи режим не "наосліп"
        val shouldAnimate = isAnimationEnabled && visualGameMode != "blindfold"

        // викликаємо рушій через менеджер
        val moveResult = sessionManager.makeMove(from, to, flags)

        // крапково оновлюємо UI на основі відповіді рушія
        // якщо анімації не буде, передаємо порожній set, щоб фігури на дошці не зникали
        val squaresToHide = if (shouldAnimate) setOf(from, to) else emptySet()
        // оновлюємо позиції фігур, передаючи клітинки для приховування під час анімації
        applyIncrementalUpdate(moveResult.uiUpdate, squaresToHide)

        // оновлюємо підсвітку останнього ходу
        updateLastMoveHighlight(oldUiState.lastMove, Triple(from, to, flags))

        // отримуємо оновлений масив історії після того, як хід відбувся
        val updatedHistoryMoves = sessionManager.getHistoryMoves()

        // оновлюємо повний стан (mailbox, права рокірування і т.д.), це необхідно для коректної роботи наступних викликів JNI
        val lastMoveIndex = updatedHistoryMoves.size - 1
        val newState = sessionManager.getGameState(lastMoveIndex)
        _gameState.value = newState
        _mailbox.value = newState.mailbox

        // оновлюємо підсвітку шаху
        updateCheckHighlight(oldUiState.checkedKingSquare, sessionManager.findCheckedKing(newState))

        // аналізуємо результат ходу з C++
        val gameOutcome = when (moveResult.gameStatus) {
            "CHECKMATE" -> "$movingPlayer перемогли"
            "STALEMATE", "50_MOVES", "REPETITION", "MATERIAL" -> "Нічия"
            "TIMEOUT_WHITE" -> "Чорні перемогли по часу"
            "TIMEOUT_BLACK" -> "Білі перемогли по часу"
            "TIMEOUT_MATERIAL" -> "Нічия через нестачу матеріалу і вичерпання часу"

            else -> null // гра триває
        }

        // оновлюємо UI State (підсвітка останнього ходу, шах і т.д.)
        _uiState.update {
            it.copy(
                lastMove = Triple(from, to, flags),
                penultMove = oldUiState.lastMove, // попередній lastMove стає penultMove
                highlightedAttackMap = null,
                attackMapShownFor = null,
                checkedKingSquare = sessionManager.findCheckedKing(newState), // findCheckedKing працює з `newState`
                notationHistory = oldUiState.notationHistory.take(activeIndexBeforeMove + 1) + moveResult.notation, // готова нотація
                whiteCaptured = moveResult.whiteCaptured.toList(), // взятий матеріал
                blackCaptured = moveResult.blackCaptured.toList(),
                materialAdvantage = moveResult.materialAdvantage, // перевага
                whiteTimeRemainingMs = moveResult.whiteTimeRemainingMs,
                blackTimeRemainingMs = moveResult.blackTimeRemainingMs,
                outcome = gameOutcome ?: it.outcome, // оновлюємо результат гри, якщо вона завершилась
                hintFromSquare = null, // для підказки
                hintToSquare = null,  // скидаємо підказку після виконання ходу
                animatedMove = if (shouldAnimate) animationData else null, // дані анімації
                squaresToHideWhileAnimating = squaresToHide // фігури на from і to
            )
        }

        // логіка вибору звуку на основі прапорів ходу (не певен чи варто додавати окремий звук для промоції)
        val soundType = when {
            // пріоритет у звуків результату гри
            moveResult.gameStatus == "CHECKMATE" -> ChessSoundPlayer.SoundType.CHECKMATE
            moveResult.gameStatus == "STALEMATE" || moveResult.gameStatus == "50_MOVES" || moveResult.gameStatus == "REPETITION" || moveResult.gameStatus == "MATERIAL" -> ChessSoundPlayer.SoundType.STALEMATE
            moveResult.gameStatus == "CHECK" -> ChessSoundPlayer.SoundType.CHECK
            flags == 1 -> ChessSoundPlayer.SoundType.TAKE // MoveFlags::CAPTURE = 1
            flags == 4 -> ChessSoundPlayer.SoundType.CASTLE // MoveFlags::CASTLE = 4
            flags == 3 -> ChessSoundPlayer.SoundType.EN // MoveFlags::EN_PASSANT = 3
            flags >= 8 -> ChessSoundPlayer.SoundType.MOVE // MoveFlags::PROMOTION_... >= 8 // або можна створити новий SoundType.PROMOTION
            // для решти ходів
            else -> ChessSoundPlayer.SoundType.MOVE
        }
        viewModelScope.launch { _soundEffect.emit(soundType) }
       
        autoSaveGame(gameOutcome) // зберігаємо гру на кожному ході
    }

    // функція очищення стану анімації та прихованих клітинок
    fun clearAnimation() {
        // скидаємо глобальний стан анімації в _uiState
        _uiState.update { it.copy(
            animatedMove = null,
            squaresToHideWhileAnimating = emptySet()
        )}

        // повне скидання: проходимо по всіх клітинках
        // це гарантує, що жодна фігура не "застрягне" в невидимому стані
        for (i in boardUiState.indices) {
            if (boardUiState[i].shouldHidePiece) {
                boardUiState[i] = boardUiState[i].copy(shouldHidePiece = false)
            }
        }
    }

    fun findAndMakeBestMove() {
        // переконуємось, що не виконується інший пошук
        if (_uiState.value.isSearchRunning) return
        
        _uiState.update { it.copy(isSearchRunning = true) }  // встановлюємо прапор (щоб показати індикатор)в головному потоці, щоб уникнути гонитви

        searchJob =viewModelScope.launch(Dispatchers.Default) { // запускаємо корутину в фоновому потоці, оптимізованому для CPU-операцій
            try {
                val depth = _uiState.value.engineSearchDepth
                val resultString = sessionManager.findBestMove(depth)

                // після розрахунків повертаємось в основний потік для оновлення UI
                withContext(Dispatchers.Main) {
                    // якщо пошук зупинили, findBestMoveJNI поверне найкращий хід, знайдений до зупинки
                    // UI обробить його як звичайний хід
                    if (resultString.startsWith("result:")) {
                        val outcomeMessage = when (resultString) {
                            "result:checkmate" -> if (gameState.value.sideToMove == uiState.value.humanPlayerColor) "Бот переміг (Мат)" else "Ви перемогли (Мат)"
                            "result:stalemate" -> "Нічия. Пат."
                            "result:repetition" -> "Нічия через повторення."
                            else -> resultString
                        }
                        _uiState.update { it.copy(outcome = outcomeMessage) }
                    } else {
                        // парсимо рядок
                        val moveData = resultString.split(",").associate { val (k, v) = it.split(":"); k to v }
                        
                        val engineId = moveData["id"]?.toLongOrNull() ?: -1L // читаємо ID з рушія
                        if (engineId <= latestValidEngineId) {
                            return@withContext // якщо це запізнілий хід з попереднього пошуку - відкидаємо
                        }
                        latestValidEngineId = engineId // оновлюємо ID новим значенням
                        
                        // зберігаємо статистику
                        val newStats = EngineStats(
                            score = moveData["score"] ?: "?",
                            nodes = moveData["nodes"] ?: "0",
                            timeMs = moveData["time_ms"] ?: "0",
                            nps = moveData["nps"] ?: "0",
                            depth = depth
                        )
                        // оновлюємо UI, зберігаючи stats, але не скидаючи інші поля
                        _uiState.update { it.copy( searchStatsHistory = (listOf(newStats) + it.searchStatsHistory).take(3) ) }
                        
                        val encodedMove = moveData["move"]?.toIntOrNull() ?: return@withContext // витягаємо хід і виконуємо його

                        // розпаковуємо дані з `encodedMove` і передаємо їх у `makeMove`
                        val from = encodedMove shr 16
                        val to = (encodedMove shr 8) and 0xFF
                        val flags = encodedMove and 0xFF

                        // якщо прийшов хід поза межами дошки (0..80) -> ігноруємо
                        if (from !in 0..80 || to !in 0..80) {
                            return@withContext
                        }

                        makeMove(from, to, flags) // makeMove вже оновлює UI, він має викликатись з Main потоку
                    }
                }
            } finally {
                // NonCancellable, щоб прапорець гарантовано скидався, навіть якщо сталася помилка або зупинка через Undo або вихід з екрану
                withContext(NonCancellable + Dispatchers.Main) {
                    _uiState.update { it.copy(isSearchRunning = false) }
                }
            }
        }
    }
    
    fun stopSearch() { // функція зупинки пошуку з UI просто викликає JNI
        sessionManager.stopSearch()
    }

    // функція для отримання підказки від рушія
    fun getBestMoveHint() {
        // перевіряємо, чи не виконується вже інший фоновий процес
        if (_uiState.value.isSearchRunning) return

        viewModelScope.launch(Dispatchers.Default) { // фоновий потік
            // тимчасово показуємо індикатор завантаження, поки рушій думає
            _uiState.update { it.copy(isSearchRunning = true) }
            try {
                val depth = _uiState.value.engineSearchDepth
                val resultString = sessionManager.findBestMove(depth)
                withContext(Dispatchers.Main) { // повернення в потік UI
                    if (!resultString.startsWith("result:")) {
                        val moveData = resultString.split(",").associate { val (k,v) = it.split(":"); k to v }
                        val encodedMove = moveData["move"]?.toIntOrNull()
                        if (encodedMove != null) {
                            val from = encodedMove shr 16
                            val to = (encodedMove shr 8) and 0xFF
                            _uiState.update { it.copy(hintFromSquare = from, hintToSquare = to) }
                        }
                    }
                }
            } finally {
                // ховаємо індикатор завантаження після завершення
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSearchRunning = false) }
                }
            }
        }
    }

    fun cycleAttackMapDisplay(customBitboards: Array<LongArray>? = null) {
        val currentGameState = _gameState.value
        // використовуємо кастомні бітборди, якщо їх передали (для самоаналізу),
        // інакше беремо з поточного стану гри (для екрану самоаналізу).
        val bitboardsToUse = customBitboards ?: currentGameState.pieceBitboards

        // визначаємо наступний стан (вимкнено -> білі -> чорні -> вимкнено),
        // базуючись на тому, що показано зараз.
        val nextColorToShow = when (_uiState.value.attackMapShownFor) {
            null -> 0 // якщо вимкнено, наступними показуємо білих
            0 -> 1    // якщо показані білі, наступними показуємо чорних
            1 -> null // якщо показані чорні, вимикаємо
            else -> null
        }

        if (nextColorToShow == null) {
            // вимикаємо мапу, скидаючи відповідні поля стану
            _uiState.update { it.copy(highlightedAttackMap = null, attackMapShownFor = null) }
        } else {
            // вмикаємо мапу для наступного кольору
            val newAttackMap = sessionManager.getAttackMap(bitboardsToUse, nextColorToShow)
            _uiState.update { it.copy(highlightedAttackMap = newAttackMap, attackMapShownFor = nextColorToShow) }
        }
        
        updateFullBoardState() // примусово оновлюємо стан кожної клітинки для UI
    }

    // перемикає видимість панелі статистики
    fun toggleStatsVisibility() {
        _uiState.update { it.copy(isStatsPanelVisible = !it.isStatsPanelVisible) }
    }

    fun undoMove() {
        // не дозволяємо undo в грі з активним таймером (час >= 0)
        val isTimerGame = (_uiState.value.whiteTimeRemainingMs ?: -1L) >= 0L || (_uiState.value.blackTimeRemainingMs ?: -1L) >= 0L
        if (isTimerGame) {
            return
        }
        val oldMailbox = _mailbox.value.clone()
        val oldUiState = _uiState.value

        // гасимо підсвітку легальних ходів перед скасуванням
        updateLegalMovesState(oldUiState.legalMovesForSelected, emptyList())
        
        val undoneState = sessionManager.undoMove() // скасовуємо хід у рушії
        // оновлюємо геймстейт
        _gameState.value = undoneState
        _mailbox.value = undoneState.mailbox // якщо Undo повертає об'єкт з mailbox

        // оновлюємо фігури, порівнюючи старий і новий mailbox (undoneState.mailbox)
        val newMailbox = undoneState.mailbox
        for (i in oldMailbox.indices) {
            if (oldMailbox[i] != newMailbox[i]) {
                boardUiState[i] = boardUiState[i].copy(
                    piece = ChessPiece.fromCode(newMailbox[i])
                )
            }
        }

        // отримуємо попередній хід з історії для правильної підсвітки
        val currentHistoryIndex = sessionManager.getHistoryInfo()[0]
        val historyMoves = sessionManager.getHistoryMoves()
        val newLastMove = if (currentHistoryIndex >= 0 && currentHistoryIndex < historyMoves.size) {
            val encodedMove = historyMoves[currentHistoryIndex]
            Triple(encodedMove shr 16, (encodedMove shr 8) and 0xFF, encodedMove and 0xFF)
        } else null

        // оновлюємо підсвітку останнього ходу
        updateLastMoveHighlight(oldUiState.lastMove, null) // просто прибираємо стару

        // оновлюємо підсвітку шаху
        updateCheckHighlight(oldUiState.checkedKingSquare, sessionManager.findCheckedKing(_gameState.value))

        // оновлюємо UI, щоб прибрати підсвітку останнього ходу та запис нотації
        _uiState.update {
            it.copy(
                lastMove = newLastMove,
                penultMove = null,
                checkedKingSquare = sessionManager.findCheckedKing(_gameState.value),
                // видаляємо останній запис з історії нотацій
                notationHistory = it.notationHistory,
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                selectedEmptySquare = null,
                // TODO: потрібно оновити captured pieces та матеріальну перевагу. JNI має повертати JniMoveResult
            )
        }
        autoSaveGame(null) // скидаємо збереження
    }

    fun undoBotGameMove() {
        // зупиняємо пошук бота, щоб уникнути ходів від запізнілих потоків
        invalidateBotSearch()

        val currentState = _gameState.value
        val humanColor = _uiState.value.humanPlayerColor

        // визначаємо, скільки напівходів треба відкотити, щоб повернути чергу гравцю
        if (currentState.sideToMove == humanColor) {
            // зараз черга гравця (останнім походив бот)
            undoMove() // скасовуємо хід бота, а потім хід гравця
            undoMove()
        } else {
            // зараз черга бота (останнім походив гравець, а бот ще думає).
            undoMove() // відкочуємо лише хід гравця (1)
        }
    }

    fun redoMove() {
        // викликаємо через менеджер C++ функцію, що виконує хід і повертає новий стан бітбордів
        val newState = sessionManager.redoMove()
        _gameState.value = newState
        _mailbox.value = newState.mailbox // оновлюємо mailbox!

        val oldUiState = _uiState.value
        // гасимо підсвітку легальних ходів перед повторенням ходу
        updateLegalMovesState(oldUiState.legalMovesForSelected, emptyList())

        // отримуємо повний масив історії, щоб знайти останній хід.
        val historyMoves = sessionManager.getHistoryMoves()
        val currentHistoryIndex = sessionManager.getHistoryInfo()[0]
        val lastMoveEncoded = if (currentHistoryIndex >= 0 && currentHistoryIndex < historyMoves.size) {
            historyMoves[currentHistoryIndex]
        } else return

        // розкодовуємо останній хід для підсвітки.
        val from = lastMoveEncoded shr 16
        val to = (lastMoveEncoded shr 8) and 0xFF
        val flags = lastMoveEncoded and 0xFF

        // оновлюємо фігури на дошці
        for (i in 0..80) {
            val piece = ChessPiece.fromCode(_mailbox.value[i])
            boardUiState[i] = boardUiState[i].copy(piece = piece)
        }

        // оновлюємо підсвітку останнього ходу
        updateLastMoveHighlight(_uiState.value.lastMove, Triple(from, to, flags))
        updateCheckHighlight(_uiState.value.checkedKingSquare, sessionManager.findCheckedKing(newState))

        // оновлюємо UI
        _uiState.update {
            it.copy(
                lastMove = Triple(from, to, flags),
                checkedKingSquare = sessionManager.findCheckedKing(newState),
                isViewingHistory = false,
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                selectedEmptySquare = null,
                // notationHistory = it.notationHistory + newNotation
                // TODO: додати оновлення нотації, взяті фігури, перевагу коли JNI буде повертати JniMoveResult
            )
        }
        autoSaveGame(null) // автозбереження мабуть має бути не null тут
    }

    fun redoTwoHalfMoves() {
        redoMove() // повертаємо два півходи у грі з ботом
        redoMove()
    }

    // функція `startNewGame` знищує стару сесію і створює нову
    fun startNewGame(timerDurationMinutes: Int, playerColor: Int? = null) {
        // зупиняємо (якщо був) активний пошук, пов'язаний зі старою сесією
        sessionManager.stopSearch()
        sessionStartTimestamp = System.currentTimeMillis() // записуємо час старту для збереження гри

        // зберігаємо поточні налаштування перед скиданням стану
        val currentUiState = _uiState.value
        val savedCellNameMode = currentUiState.cellNameMode  // вибране відображення назв клітинок
        val savedSearchDepth = currentUiState.engineSearchDepth // глибина пошуку
        val savedAutoFlip = currentUiState.autoFlipEnabled    // автоповорот

        // визначаємо наступний колір гравця, якщо (playerColor == null), то перемикаємо його автоматично
        // якщо колір передано з діалогу, використовуємо його
        val nextHumanColor = playerColor ?: if (_uiState.value.humanPlayerColor == 0) 1 else 0 // 0-white, 1-black

        // зберігаємо колір гравця у поточній грі
        settingsManager.saveSetting("human_color_$gameModeKey", nextHumanColor.toString())

        // використовуємо менеджер для перезапуску
        sessionManager.restartSession(timerDurationMinutes)

        // визначаємо, чи потрібно повертати дошку
        val isFlipped = (nextHumanColor == 1) // true, якщо гравець гратиме чорними

//        // отримуємо стан вже з нової сесії
//        _gameState.value = Engine81Bridge.getStateForHistoryMoveJNI(sessionManager.sessionPtr, -1)

        // отримуємо початковий стан з нової сесії
        _gameState.value = sessionManager.getGameState(-1)
        // синхронізуємо mailbox з новим станом гри
        _mailbox.value = _gameState.value.mailbox

//        // скидаємо індекс історії на 0 для нової гри
//        currentHistoryIndex = 0

        pollingJob?.cancel() // завжди зупиняємо старе опитування
        if (timerDurationMinutes > 0) {
            startStatusPolling() // запускаємо нове, тільки якщо гра з таймером
        }

        // скидаємо UI, але зі збереженими параметрами
        _uiState.value = EngineUiState(
            humanPlayerColor = nextHumanColor,
            isBoardFlipped = isFlipped,
            showPreGameDialog = false, // ховаємо діалог після старту
            cellNameMode = savedCellNameMode,
            engineSearchDepth = savedSearchDepth,
            autoFlipEnabled = savedAutoFlip
        )

        updateFullBoardState() // викликаємо функцію повного оновлення стану дошки
    }
    
    fun showGameSetup() {    // відкриваємо GameSetupDialog
        _uiState.update { it.copy(showPreGameDialog = true) }
    }
    fun dismissPreGameDialog() {    // закриваємо GameSetupDialog
        _uiState.update { it.copy(showPreGameDialog = false) }
    }

    // безпечна функція для повернення до поточної гри
    fun returnToCurrentGame() {
        if (!_uiState.value.isViewingHistory) return // нічого не робимо, якщо вже в грі

        val lastMoveIndex = _uiState.value.notationHistory.size - 1
        val currentState = sessionManager.getGameState(lastMoveIndex)

        _gameState.value = currentState

        // повертаємо актуальний mailbox
        _mailbox.value = currentState.mailbox

        // відновлюємо реальний останній хід гри для підсвітки
        val historyMoves = sessionManager.getHistoryMoves()
        val actualLastMove = if (historyMoves.isNotEmpty()) {
            val encodedMove = historyMoves.last()
            val from = encodedMove shr 16
            val to = (encodedMove shr 8) and 0xFF
            val flags = encodedMove and 0xFF
            Triple(from, to, flags)
        } else {
            null
        }
        _uiState.update {
            it.copy(
                isViewingHistory = false,
                currentHistoryViewIndex = lastMoveIndex,
                lastMove = actualLastMove, // прибираємо підсвітку старого ходу, що переглядався
                checkedKingSquare = sessionManager.findCheckedKing(_gameState.value)
            )
        }

        updateFullBoardState() // викликаємо функцію повного оновлення стану дошки
    }

    fun goToHistoryState(moveIndex: Int) {
        // отримуємо стан для вибраного ходу. індекс -1 для початкової позиції.
        val historicState = sessionManager.getGameState(moveIndex)
        _gameState.value = historicState

        // оновлюємо mailbox, щоб Compose перемалював фігури
        _mailbox.value = historicState.mailbox

        // дістаємо хід з історії, щоб показати підсвітку "останнього ходу" для цієї історичної позиції
        val historyMoves = sessionManager.getHistoryMoves()
        val historicalLastMove = if (moveIndex >= 0 && moveIndex < historyMoves.size) {
            val encodedMove = historyMoves[moveIndex]
            val from = encodedMove shr 16
            val to = (encodedMove shr 8) and 0xFF
            val flags = encodedMove and 0xFF
            Triple(from, to, flags)
        } else {
            null // якщо це початкова позиція (moveIndex == -1), підсвітки немає
        }
        // оновлюємо UI
        _uiState.update {
            it.copy(
                lastMove = historicalLastMove,
                checkedKingSquare = sessionManager.findCheckedKing(historicState),
                isViewingHistory = true,
                currentHistoryViewIndex = moveIndex,
                legalMovesForSelected = emptyList(),
                selectedEmptySquare = null,
                hintFromSquare = null,
                hintToSquare = null
            )
        }

        updateFullBoardState() // викликаємо функцію повного оновлення стану дошки
    }

    fun flipBoard() = _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    fun toggleAutoFlip() {
        _uiState.update { state ->
            val currentVisualFlip = if (state.autoFlipEnabled) { // без автоповороту фіксуємо поточну орієнтацію дошки
                _gameState.value.sideToMove == 1 // 1 - це хід чорних
            } else { state.isBoardFlipped }

            state.copy(
                autoFlipEnabled = !state.autoFlipEnabled,
                isBoardFlipped = currentVisualFlip
            )
        }
    }

    init {
        val savedDepth = settingsManager.getSetting(SettingsManager.KEY_BOT_DEPTH, "4").toIntOrNull() ?: 4 // читаємо збережену глибину під час створення ViewModel
        _uiState.update { it.copy(engineSearchDepth = savedDepth) } // встановлюємо її в початковий стан UI
    }
    fun changeSearchDepth(newDepth: Int) {
        // оновлюємо стан для гри та UI
        _uiState.update { it.copy(engineSearchDepth = newDepth) }

        // зберігаємо в налаштування
        settingsManager.saveSetting(SettingsManager.KEY_BOT_DEPTH, newDepth.toString())
    }

    fun toggleAttackMap(color: PlayerColor) {
        val jniColor = color.toJniValue()
        if (_uiState.value.attackMapShownFor == jniColor) {
            _uiState.update { it.copy(highlightedAttackMap = null, attackMapShownFor = null) }
        } else {
            // отримуємо бітборди з актуального стану гри
            val attackMap = sessionManager.getAttackMap(_gameState.value.pieceBitboards, jniColor)
            _uiState.update { it.copy(highlightedAttackMap = attackMap, attackMapShownFor = jniColor) }
        }
    }

    fun runPerft(isMultiThreaded: Boolean) {
        if (_uiState.value.isSearchRunning) return  // якщо тест вже виконується - виходимо
        viewModelScope.launch(Dispatchers.Default) { // фоновий потік
           
            _uiState.update { it.copy(isSearchRunning = true) } // вмикаємо індикатор пошуку - кнопку зупинки
            val depth = _uiState.value.engineSearchDepth
            val currentGameState = _gameState.value
            try {
                // викликаємо потрібну JNI-функцію залежно від параметра
                val result = if (isMultiThreaded) {
                    Engine81Bridge.runPerftMultiThreadedJNI(
                        depth,
                        currentGameState.pieceBitboards,
                        currentGameState.sideToMove,
                        currentGameState.enPassantSquare,
                        currentGameState.castlingRights
                    )
                } else {
                    Engine81Bridge.runPerftJNIBenchmark(
                        depth,
                        currentGameState.pieceBitboards,
                        currentGameState.sideToMove,
                        currentGameState.enPassantSquare,
                        currentGameState.castlingRights
                    )
                }
                // addBenchmarkToHistory оновлює UI, тому її теж треба викликати з основного потоку
                withContext(Dispatchers.Main) { // повернення в потік UI
                    addBenchmarkToHistory(result)
                }
            } finally {
                // вимикаємо індикатор, навіть якщо сталася помилка
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSearchRunning = false) }
                }
            }
        }
    }

    private fun addBenchmarkToHistory(result: String) {
        // парсимо рядок "move:0,score:0,nodes:X,time_ms:Y,nps:Z" у мапу
        val moveData = result.split(",").associate {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else "" to ""
        }

        val nodes = (moveData["nodes"] ?: "0")
        // якщо вузлів 0 (тест викликано помилково разом зі статистикою пошуку), нічого не робимо
        if (nodes == "0") return

        // створюємо об'єкт статистики (уніфікований формат)
        val newStats = EngineStats(
            score = moveData["score"] ?: "0", // бенчмарк не дає оцінки, тому тут виводить глибину
            nodes = moveData["nodes"] ?: "0",
            timeMs = moveData["time_ms"] ?: "0",
            nps = moveData["nps"] ?: "0"
        )

        // оновлюємо UI стан
        _uiState.update {
            it.copy(
                // додаємо в історію пошуку, щоб StatsPanel побачила ці дані
                searchStatsHistory = (listOf(newStats) + it.searchStatsHistory).take(3),
                // робимо панель видимою, якщо вона була прихована
                isStatsPanelVisible = true,
                // (опційно) оновлюємо старий рядок, якщо він десь ще використовується
                benchmarkResult = "D | Вузли: ${newStats.nodes} | NPS: ${newStats.nps}"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.destroy() // делегуємо очищення
    }

    // це для швидкого виправлення помилок, слід оптимізувати
    fun getHistoryMoves(): IntArray = sessionManager.getHistoryMoves()
    fun getHistoryInfo(): IntArray = sessionManager.getHistoryInfo()

    /**
     * отримує перелік легальних клітинок призначення для фігури.
     * використовується екраном BotGameScreen.
     */
    fun getLegalDestinationsForSquare(fromSquare: Int): List<Int> {
        // виклик через менеджер
        return sessionManager.getLegalDestinations(fromSquare).toList()
    }

    // функція для активації самоаналізу (викликаємо при вході на екран)
    fun enterTrainingGameMode() {
        _uiState.update { it.copy(isTrainingGameMode = true) }
    }

    // вибір фігури з панелі для створення позиції
    fun setActivePieceToPlace(piece: ChessPiece?) {
        // працює лише в режимі самоаналізу
        if (!_uiState.value.isTrainingGameMode) return
        _uiState.update { it.copy(activePieceToPlace = piece, selectedSquare = null) }
    }

    // очищення всієї дошки в тренуванні
    fun clearTrainingGameBoard() {
        if (!_uiState.value.isTrainingGameMode) return
        _uiState.update { it.copy(
            placedPieces = emptyMap(),
            activePieceToPlace = null,
            selectedSquare = null,
            legalMovesForSelected = emptyList() // очищуємо легальні ходи
        )}
    }

    // видалення фігури з вибраної клітинки
    fun removePieceFromSelectedSquare() {
        if (!_uiState.value.isTrainingGameMode) return
        val selected = _uiState.value.selectedSquare ?: return
        _uiState.update {
            it.copy(
                placedPieces = it.placedPieces - selected,
                selectedSquare = null,
                legalMovesForSelected = emptyList()
            )
        }
    }

    // головний обробник кліків по дошці в режимі тренувань
    fun handleTrainingGameSquareClick(clickedSquare: Int) {
        if (!_uiState.value.isTrainingGameMode) return
        val ui = _uiState.value // отримуємо поточний стан

        val pieceToPlace = ui.activePieceToPlace
        val pieceOnSquare = ui.placedPieces[clickedSquare]

        if (pieceToPlace != null) {
            // режим розстановки: ставимо фігуру і очищуємо "руку"
            _uiState.update { it.copy(
                placedPieces = it.placedPieces + (clickedSquare to pieceToPlace),
                activePieceToPlace = null, // можна не скидати виділення для повторюваного встановлення
                selectedSquare = null // скидаємо виділення для аналізу
            )}
        } else {
            // режим аналізу / переміщення
            if (pieceOnSquare != null) {
                if (ui.selectedSquare == clickedSquare) {
                    // якщо клікнули на вже вибрану фігуру, "беремо її в руку" для переміщення
                    _uiState.update { it.copy(
                        activePieceToPlace = pieceOnSquare,
                        placedPieces = it.placedPieces - clickedSquare,
                        selectedSquare = null,
                        legalMovesForSelected = emptyList()
                    )}
                } else {
                    // вибрали нову фігуру для аналізу ходів
                    // TODO: після додавання звичайної гри вибір фігури будь-якого кольору треба буде реалізувати окремо
                    val legalMoves = getLegalMovesForTrainingGame(ui.placedPieces, clickedSquare)
                    _uiState.update { it.copy(
                        selectedSquare = clickedSquare,
                        trainingGameSideToMove = pieceOnSquare.color.toJniValue(), // аналізуємо хід для кольору фігури
                        legalMovesForSelected = legalMoves // оновлюємо легальні ходи
                    )}
                }
            } else {
                // клік по порожній клітинці - скидаємо вибір
                _uiState.update { it.copy(selectedSquare = null, legalMovesForSelected = emptyList()) }
            }
        }
    }

    // приватна функція, що інкапсулює виклик JNI для самоаналізу
    private fun getLegalMovesForTrainingGame(placedPieces: Map<Int, ChessPiece>, fromSquare: Int): List<Int> {
        val piece = placedPieces[fromSquare] ?: return emptyList()

        // конвертуємо placedPieces в bitboards
        val pieceBitboards = Array(14) { LongArray(2) }
        placedPieces.forEach { (square, p) ->
            val pieceJniIndex = p.type.toJniValue() + p.color.toJniValue() * 7
            setBit(pieceBitboards[pieceJniIndex], square)
        }

        // викликаємо JNI для генерації ВСІХ легальних ходів
        val allMovesEncoded = sessionManager.generateAllMoves(
            pieceBitboards = pieceBitboards,
            sideToMove = piece.color.toJniValue(),
            castlingRights = 15, // поки що максимальні права
            enPassantSquare = -1 // взяття на проході поки не ввімкнуте
        )

        // фільтруємо і повертаємо лише ті, що починаються з нашої клітинки
        return allMovesEncoded.filter { (it shr 16) == fromSquare }
    }
}

// стан онлайн-екрану
sealed class OnlineState {
    object Idle : OnlineState() // початковий стан
    object Searching : OnlineState() // шукаємо гру
    data class GameFound(val gameId: String) : OnlineState() // гру знайдено
    data class Error(val message: String) : OnlineState() // помилка
}

// конвертер для збереження дошки в Firebase
private fun Array<Array<ChessPiece?>>.toFirebaseCompatibleList(): List<List<Map<String, String>?>> {
    return this.map { row ->
        row.map { piece ->
            piece?.let { mapOf("type" to it.type.name, "color" to it.color.name) }
        }
    }
}

class OnlineViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database = Firebase.database.reference
    private val userLobbyRef get() = auth.currentUser?.uid?.let { database.child("lobby").child(it) }

    private val _onlineState = MutableStateFlow<OnlineState>(OnlineState.Idle)
    val onlineState: StateFlow<OnlineState> = _onlineState

    private var lobbyListener: ValueEventListener? = null

    // головний метод, що запускає весь онлайн-процес
    fun startOnlineMatchmaking() {
        viewModelScope.launch {
            try {
                // спершу входимо в систему
                val userId = signInAnonymously()
                if (userId != null) {
                    // вхід успішний, починаємо пошук гри
                    findOrCreateGame(userId)
                } else {
                    _onlineState.value = OnlineState.Error("Не вдалося отримати ID користувача.")
                }
            } catch (e: Exception) {
                _onlineState.value = OnlineState.Error("Помилка з'єднання: ${e.message}")
            }
        }
    }

    // функція, що повертає ID або null
    private suspend fun signInAnonymously(): String? {
        return if (auth.currentUser != null) {
            auth.currentUser?.uid
        } else {
            auth.signInAnonymously().await().user?.uid
        }
    }

    // метод матчмейкінгу приймає userId як параметр
    private fun findOrCreateGame(currentUserId: String) {
        _onlineState.value = OnlineState.Searching
        val lobbyRef = database.child("lobby")

        // транзакція для безпечної роботи з лобі
        lobbyRef.runTransaction(object : Transaction.Handler {
            // або знаходимо суперника і створюємо гру, або додаємо себе до лобі.
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // шукаємо суперника, котрий чекає (у нього ще немає gameId)
                val opponentNode = currentData.children.firstOrNull {
                    it.key != currentUserId && !it.hasChild("gameId")
                }

                if (opponentNode == null) {
                    // лобі порожнє або всі зайняті. додаємо себе, щоб чекати.
                    currentData.child(currentUserId).value = mapOf("timestamp" to ServerValue.TIMESTAMP)
                } else {
                    // знайшли суперника. створюємо ID гри.
                    val newGameId = database.child("games").push().key
                        ?: return Transaction.abort() // Якщо не вдалося створити ID, скасовуємо транзакцію

                    // записуємо gameId обом гравцям одночасно. це "резервує" їх і сигналізує обом про початок гри.
                    val opponentId = opponentNode.key!!
                    currentData.child(currentUserId).child("gameId").value = newGameId
                    currentData.child(opponentId).child("gameId").value = newGameId

                    // гра створюється до завершення транзакції.
                    // якщо ми знайшли суперника, то ми чорні і створюємо гру.
                    viewModelScope.launch {
                        createNewGame(myId = currentUserId, opponentId = opponentId, gameId = newGameId)
                    }
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    _onlineState.value = OnlineState.Error("Помилка матчмейкінгу: ${error.message}")
                    return
                }

                if (!committed) {
                    // транзакція не пройшла (наприклад, суперника "перехопив" хтось інший),
                    // інформуємо користувача спробувати ще раз.
                    _onlineState.value = OnlineState.Error("Не вдалося знайти гру. Спробуйте ще раз.")
                    return
                }

                // починаємо слухати зміни у власному вузлі в лобі.
                // якщо `gameId` з'явиться - значить гру знайдено (не важливо, хто її створив).
                listenForGameInvitation(currentUserId)
            }
        })
    }


    // Починаємо "слухати" запрошення в гру
    private fun listenForGameInvitation(myId: String) {
        // використовуємо ліниву ініціалізацію, щоб уникнути null для userLobbyRef
        val ref = userLobbyRef ?: return
        // видаляємо старий слухач, у випадку якщо він залишився
        lobbyListener?.let { ref.removeEventListener(it) }

        lobbyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // якщо наш вузол існує і в ньому з'явився gameId
                val gameId = snapshot.child("gameId").value as? String
                if (!gameId.isNullOrBlank()) {
                    // гру знайдено, переходимо на екран гри
                    _onlineState.value = OnlineState.GameFound(gameId)
                    // важливо видалити прослуховувач перед видаленням даних з лобі.
                    cleanUpLobby()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _onlineState.value = OnlineState.Error("Помилка очікування: ${error.message}")
                cleanUpLobby() // прибираємо за собою навіть у випадку помилки
            }
        }
        ref.addValueEventListener(lobbyListener!!)
    }

    // генерує чисту структуру даних для Firebase (тимчасова функція, щоб відв'язати OnlineViewModel від застарілого коду)
    private fun getInitialBoardForFirebase(): List<List<Map<String, String>?>> {
        // Створюємо порожню дошку 9x9
        val rows = MutableList(9) { MutableList<Map<String, String>?>(9) { null } }

        // Порядок фігур на першій лінії (Rook, Bishop, Knight, Queen, King, Guard, Bishop, Knight, Rook)
        val pieceOrder = listOf(
            "ROOK", "BISHOP", "KNIGHT", "QUEEN",
            "KING", "GUARD", "BISHOP", "KNIGHT", "ROOK"
        )

        // Заповнюємо Чорних (рядки 0 та 1)
        for (col in 0..8) {
            rows[0][col] = mapOf("type" to pieceOrder[col], "color" to "BLACK")
            rows[1][col] = mapOf("type" to "PAWN", "color" to "BLACK")
        }

        // Заповнюємо Білих (рядки 7 та 8 - зверніть увагу на індекси для дошки 9x9)
        for (col in 0..8) {
            rows[7][col] = mapOf("type" to "PAWN", "color" to "WHITE")
            rows[8][col] = mapOf("type" to pieceOrder[col], "color" to "WHITE")
        }

        return rows
    }

    // створюємо нову гру в базі даних
    private suspend fun createNewGame(myId: String, opponentId: String, gameId: String) {
        val gamesRef = database.child("games").child(gameId)

        // створюємо початковий стан // готуємо дані для запису в Firebase
        // той, хто чекав у лобі (opponentId) - грає білими
        val gameData = mapOf(
            "players" to listOf(opponentId, myId),
            "whitePlayerId" to opponentId,
            "blackPlayerId" to myId,

            // використовуємо тимчасовий метод
            "boardState" to getInitialBoardForFirebase(),

            // хардкодимо початкові значення (білі завжди ходять перші)
            "currentPlayer" to "WHITE",
            "outcome" to null,
            "lastMove" to null,
            "notationHistory" to emptyList<String>(),

            // замість initialBoardState.castlingRights просто передаємо повні права
            "castlingRights" to mapOf(
                "whiteGuardSide" to true, "whiteQueenSide" to true,
                "blackGuardSide" to true, "blackQueenSide" to true
            ),

            "enPassantTarget" to null,
            "whiteCaptured" to emptyList<Map<String, String>>(),
            "blackCaptured" to emptyList<Map<String, String>>()
        )

        // записуємо дані гри в базу
        gamesRef.setValue(gameData).await()
    }

    // централізована функція для прибирання слухача і даних з лобі.
    private fun cleanUpLobby() {
        val ref = userLobbyRef
        lobbyListener?.let { listener ->
            ref?.removeEventListener(listener)
            lobbyListener = null
        }
        ref?.removeValue()
    }

    // скасовуємо пошук гри, якщо користувач виходить з екрану
    fun cancelSearch() {
        // використовуємо централізовану функцію прибирання.
        cleanUpLobby()
        _onlineState.value = OnlineState.Idle
    }
}

@Composable
fun TimerSelectionDialog(
    showDialog: Boolean,
    currentDuration: Int, // поточний збережений час
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit // callback для збереження вибору
) {
    if (!showDialog) return
    val timerOptions = listOf(
        0 to "Без\nтаймера",
        3 to "3 хв",
        5 to "5 хв",
        10 to "10 хв",
        15 to "15 хв",
        30 to "30 хв",
        60 to "60 хв"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 260.dp).height(320.dp), // обмежуємо ширину, встановлюємо висоту
            shape = RoundedCornerShape(28.dp),
            color = backgroundColor
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timerOptions) { (duration, label) ->
                    Button(
                        onClick = { onDurationSelected(duration) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(10),
                        colors = ButtonDefaults.buttonColors(
                            // підсвічуємо кнопку, що відповідає поточному збереженому значенню
                            containerColor = if (currentDuration == duration) boardDarkColor else buttonStyleColor
                        )
                    ) {
                        Text(text = label, color = Color.White, fontSize = 18.sp)
                    }
                }
            }
                // TODO: сюди можна додати інші налаштування, як-от "дозволити відміну ходів"
        }
    }
}

@Composable
fun StatsPanel(history: List<EngineStats>, isVisible: Boolean) {
    // alpha для прозорості: !isVisible -> alpha = 0f (прозоре), але місце займає.
    val alpha = if (isVisible) 1f else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .height(102.dp) // фіксована висота
            .padding(horizontal = 4.dp)
            .alpha(alpha) // застосовуємо прозорість до всього контейнера
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)) // напівпрозорий фон
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // виводимо до 3 елементів
        history.forEachIndexed { index, stats ->
            // трохи змінюємо прозорість для старих записів
            val alphaValue = 1f - (index * 0.12f)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .graphicsLayer(alpha = alphaValue),
                horizontalAlignment = Alignment.Start // вирівнювання елементів по горизонталі (вліво)
            ) {
                // оцінка та глибина
                Text(
                    text = "Оцінка [гл.${stats.depth}]: ${stats.score}",
                    // червоний якщо плюс (бот виграє у гравця), зелений якщо мінус (спрощено, бо це рядок)
                    color = if (stats.score.startsWith("-")) Color(0xFF69F0AE) else Color(0xFFFF6B6B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                // технічні дані
                Text(
                    text = "Вузли: ${stats.nodes}",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
                Text(
                    text = "Час: ${stats.timeMs} мс",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
                Text(
                    text = "${stats.nps} в/с",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }
        }
    }
}

//* FIXME тимчасовий код для тесту
@Composable
fun GuardTypeToggle(
    activeType: Int,                  // Приймаємо лише число
    onTypeSelected: (Int) -> Unit,     // Приймаємо функцію, що робити при кліку
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val alpha1 = if (activeType == 1) 1f else 0.3f
        Image(
            painter = painterResource(id = R.drawable.guard_pic_1),
            contentDescription = "Type 1",
            modifier = Modifier
                .size(108.dp)
                .alpha(alpha1)
                .clickable { onTypeSelected(1) } // Викликаємо лямбду
        )

        Spacer(modifier = Modifier.width(40.dp))

        val alpha2 = if (activeType == 2) 1f else 0.3f
        Image(
            painter = painterResource(id = R.drawable.guard_pic_2),
            contentDescription = "Type 2",
            modifier = Modifier
                .size(108.dp)
                .alpha(alpha2)
                .clickable { onTypeSelected(2) } // Викликаємо лямбду
        )
    }
}

@Composable
fun HomeScreen(navController: NavController, onlineViewModel: OnlineViewModel = viewModel()) { // головний екран
    // підписуємося на зміни стану з ViewModel
    val state by onlineViewModel.onlineState.collectAsState()

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) } // отримуємо налаштування з SettingsManager
    var showTimerDialog by remember { mutableStateOf(false) } // стан для керування діалогом таймера
    // отримуємо поточний таймер перед показом діалогу
    val savedTimerValue = remember(settingsManager) {
        settingsManager.getSetting(SettingsManager.KEY_TIMER_DURATION, "0").toIntOrNull() ?: 0
    }
    val archiveStr = remember(settingsManager) { settingsManager.getSetting("games_archive", "") }
    val archive = remember(archiveStr) {
        if (archiveStr.isNotEmpty()) archiveStr.split("|||") else emptyList()
    }
    val gson = remember { Gson() } // створюємо екземпляр Gson для парсингу JSON
   
    TimerSelectionDialog( // діалог варіантів таймінгу
        showDialog = showTimerDialog,
        currentDuration = savedTimerValue, // передаємо поточне значення
        onDismiss = { showTimerDialog = false },
        // передаємо лямбду, що зберігає налаштування і закриває діалог
        onDurationSelected = { newDuration ->
            settingsManager.saveSetting(SettingsManager.KEY_TIMER_DURATION, newDuration.toString())
            showTimerDialog = false
        }
    )

    // якщо користувач у стані пошуку і натискає системну кнопку "Назад", скасовуємо пошук
    if (state is OnlineState.Searching) {
        BackHandler {
            onlineViewModel.cancelSearch()
        }
    }
    // навігація, коли гру знайдено
    LaunchedEffect(state) {
        if (state is OnlineState.GameFound) {
            val gameId = (state as OnlineState.GameFound).gameId
            // передаємо ID в маршрут
            navController.navigate("online_game/$gameId") {
                // видаляємо HomeScreen з історії навігації, щоб не можна було повернутися назад
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 1. Стан скролу (залишаємо як було)
        val scrollState = rememberScrollState()
        val isCollapsed by remember {
            // Краще поставити невеликий поріг (наприклад, 20), 
            // щоб від легкого дотику текст не блимав
            derivedStateOf { scrollState.value > 60 }
        }
        when (state) {
            is OnlineState.Searching -> {
                // показуємо індикатор завантаження під час пошуку
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 42.dp)
                ) {
                    Text(
                        text = "Пошук гри...",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = boardDarkColor,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            is OnlineState.Error -> {
                // показуємо повідомлення про помилку
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text((state as OnlineState.Error).message, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp).padding(horizontal = 48.dp))
                    Button(onClick = { onlineViewModel.cancelSearch() }) {
                        Text("OK")
                    }
                }
            }
            else -> {
                // показуємо кнопки в початковому стані
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // займає всю ширину
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    Spacer(modifier = Modifier.height(30.dp))
//                    Spacer(modifier = Modifier.requiredHeight(72.dp))
                    // FIXME тимчасовий код для тесту
                    GuardTypeToggle(
                        activeType = Engine81Bridge.activeGuardMoveType,
                        onTypeSelected = { newType ->
                            Engine81Bridge.updateGuardType(newType) // оновлюємо тип гвардійця в рушії
                            
                            settingsManager.saveSetting("unfinished_local", "") // глобально скидаємо поточні незавершені ігри
                            settingsManager.saveSetting("unfinished_bot", "")
                        },
                        modifier = Modifier.padding(top = 152.dp) // падінг для рівномірності колонки
                    )
                    Spacer(modifier = Modifier.height(8.dp))
//                    Button(
//                        onClick = { onlineViewModel.startOnlineMatchmaking() }, // запускаємо пошук гри
//                        modifier = Modifier.size(width = 292.dp, height = 54.dp),
//                        shape = RoundedCornerShape(14), // заокруглення кнопки
//                        colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
//                    ) {
//                        Text("Грати онлайн", fontSize = 22.sp)
//                    }
                    Row(
                        modifier = Modifier.height(54.dp).clip(RoundedCornerShape(14)) // заокруглюємо всю групу
                    ) {
                        Button(
                            onClick = { navController.navigate("game?mode=hotseat") }, // режим "hotseat"
                            modifier = Modifier.size(width = 146.dp, height = 54.dp),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
                        ) {
                            Text("по черзі", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.Gray.copy(alpha = 0.7f)))
                        Button(
                            onClick = { navController.navigate("game?mode=dual") }, // режим "горизонтальної" гри
                            modifier = Modifier.size(width = 146.dp, height = 54.dp),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
                        ) {
                            Text("удвох", fontSize = 22.sp)
                        }
                    }
//                    Button(
//                        onClick = { navController.navigate("game?isTraining=true") },
//                        modifier = Modifier.size(width = 292.dp, height = 54.dp),
//                        shape = RoundedCornerShape(14), // заокруглення кнопки
//                        colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
//                    ) {
//                        Text("Самоаналіз (old)", fontSize = 22.sp)
//                    }
                    Button(
                        onClick = { navController.navigate("bot_game") },
                        modifier = Modifier.size(width = 292.dp, height = 54.dp),
                        shape = RoundedCornerShape(14), // заокруглення кнопки
                        colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
                    ) {
                        Text("Грати з ботом", fontSize = 22.sp)
                    }
                    Button(
                        onClick = { navController.navigate("puzzles") },
                        modifier = Modifier.size(width = 292.dp, height = 54.dp),
                        shape = RoundedCornerShape(14), // заокруглення кнопки
                        colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)
                    ) {
                        Text("Пазл", fontSize = 22.sp)
                    }
//                    Button(
//                        onClick = { navController.navigate("trainingGame") },
//                        modifier = Modifier.size(width = 292.dp, height = 54.dp),
//                        shape = RoundedCornerShape(14), // заокруглення кнопки
//                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.5f))
//                    ) {
//                        Text("Самоаналіз", fontSize = 22.sp)
//                    }

                    // відображення завершених партій
                    if (archive.isNotEmpty()) {
                        Text(
                            text = "Останні партії",
                            color = Color.LightGray,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                        )

                        // відображаємо перші 10 елементів
                        val parsedGames = remember(archive) {
                            archive.take(10).mapNotNull { gameDataStr ->
                                try {
                                    gson.fromJson(gameDataStr, SavedGameData::class.java)
                                } catch (e: Exception) {
                                    null // якщо якийсь рядок пошкоджений, просто ігноруємо його
                                }
                            }
                        }

                        // малюємо UI на основі переліку // парсимо JSON-рядок назад в об'єкт
                        parsedGames.forEach { gameData ->
                            val movesCount = gameData.historyMoves.size
                            val dateStr = android.text.format.DateFormat.format( 
                                "dd.MM.yyyy HH:mm",
                                java.util.Date(gameData.timestamp)
                            )
                            val outcome = gameData.outcome ?: "Невідомо"

                            Row(  // малюємо картки збережених партій
                                modifier = Modifier
                                    .width(292.dp)
                                    .padding(vertical = 4.dp) // невеликий відступ між картками
                                    .background(Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
                                    .clickable {
                                        GameArchiveCache.selectedGame = gameData // кешуємо вже прочитаний об'єкт в оперативку
                                        navController.navigate("review/${gameData.timestamp}") // переходимо на екран
                                    }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = outcome,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$dateStr • Ходів: ${(movesCount + 1) / 2}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // кнопка налаштувань
//        TextButton(
//            onClick = { navController.navigate("settings") },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .safeDrawingPadding()
//                .padding(top = OrientationPaddings.secondaryTopPadding, end = 16.dp)
//                .height(52.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp) // відступ між текстом та іконкою
//            ) {
//                Text(
//                    text = "Chess 81",
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//                Icon(
//                    painter = painterResource(id = R.drawable.menu),
//                    contentDescription = "Settings",
//                    tint = Color.White,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//        }
//        IconButton(
//            onClick = { navController.navigate("settings") },
//            modifier = Modifier
//                .align(Alignment.TopEnd) // розміщуємо справа вгорі
//                .safeDrawingPadding()
//                .padding(top = OrientationPaddings.secondaryTopPadding, end = 16.dp)
//                .size(52.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.menu),
//                contentDescription = "Налаштування",
//                tint = Color.White,
//                modifier = Modifier.size(32.dp) // розмір іконки
//            )
//        }
        TextButton(
            onClick = { navController.navigate("settings") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(top = OrientationPaddings.secondaryTopPadding, end = 16.dp)
                .height(52.dp)
            // animateContentSize() тут не обов'язковий, бо AnimatedVisibility сама керує розміром,
            // але можна залишити для плавності самої кнопки
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // коли тексту не буде - відступ сам анулюється
            ) {
                AnimatedVisibility(
                    visible = !isCollapsed,
                    // логіка появи (коли повертаємось донизу)
                    enter = expandHorizontally(
                        animationSpec = tween(durationMillis = 10),
                        expandFrom = Alignment.End // кнопка розширюється справа наліво (від іконки)
                    ) + fadeIn(
                        // текст починає з'являтися тільки ПІСЛЯ того, як кнопка розширилась
                        animationSpec = tween(durationMillis = 180, delayMillis = 10)
                    ),

                    // логіка зникнення (коли скролимо вгору)
                    exit = fadeOut(
                        // текст зникає відразу
                        animationSpec = tween(durationMillis = 180)
                    ) + shrinkHorizontally(
                        // кнопка чекає поки текст зникне, і тільки тоді починає звужуватися
                        animationSpec = tween(durationMillis = 10, delayMillis = 180),
                        shrinkTowards = Alignment.End // звужується вправо, ховаючись за іконку
                    )
                ) {
                    Text(
                        text = "Chess 81",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // іконка залишається завжди видимою і доступною для кліку
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        // кнопка вибору таймера
        Button(
            onClick = { showTimerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd) // вирівнюємо праворуч вгорі
                .safeDrawingPadding()
                // відступ, щоб кнопка не накладалася на кнопку меню
                .padding(end = 16.dp, bottom = 48.dp)
                .size(64.dp), // робимо кнопку квадратною
            shape = RoundedCornerShape(percent = 10), // заокруглення 10%
            colors = ButtonDefaults.buttonColors(
                // колір темної клітинки з поточної теми
                containerColor = LocalBoardTheme.current.darkColor
            ),
            contentPadding = PaddingValues(0.dp) // скидаємо внутрішні відступи
        ) {
            Icon(
                painterResource(id = R.drawable.timer),
                contentDescription = "Вибрати час",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

//@Composable
//fun ScreenBackButton( // кнопка назад у вигляді стандартної стрілки
//    modifier: Modifier = Modifier,
//    onBackClick: () -> Unit
//) {
//    IconButton(onClick = onBackClick,
//        modifier = Modifier.padding(top = OrientationPaddings.topPadding, start = 8.dp),
//    ) {
//        Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
//    }
//}

class OnlineGameViewModelFactory(private val gameId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnlineGameViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ViewModel для екрану гри онлайн
class OnlineGameViewModel(private val gameId: String) : ViewModel() {
    // посилання на конкретну гру в Firebase
    private val database = Firebase.database.reference.child("games").child(gameId)
    // отримуємо ID для визначення кольору, котрим граємо
    private val auth: FirebaseAuth = Firebase.auth
    private val myUserId = auth.currentUser?.uid

    // стани UI
    val boardUiState = mutableStateListOf<SquareUiState>()   // стан дошки. Compose оновлює UI по цьому переліку
    // інформація про гравців та гру. використовуємо той самий клас EngineUiState, що і в локальній грі
    private val _uiState = MutableStateFlow(ChessViewModel.EngineUiState())
    val uiState: StateFlow<ChessViewModel.EngineUiState> = _uiState

    private val _myPlayerColor = MutableStateFlow<PlayerColor?>(null)
    val myPlayerColor: StateFlow<PlayerColor?> = _myPlayerColor

    // потоки даних для панелей взятих фігур
    private val _whiteCaptured = MutableStateFlow<List<Int>>(emptyList())
    private val _blackCaptured = MutableStateFlow<List<Int>>(emptyList())

    private var myColor: PlayerColor = PlayerColor.WHITE // за замовчуванням білі
    private var isMyTurn: Boolean = false

    private var premove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null

    // combine для підготовки даних панелей
    val topPlayerDisplayData: StateFlow<PlayerDisplayData> = combine(_uiState, _whiteCaptured, _blackCaptured) { ui, whiteCap, blackCap ->
        val isFlipped = ui.isBoardFlipped
        PlayerDisplayData(
            capturedPieces = if (isFlipped) blackCap else whiteCap,
            advantage = 0, // TODO: заглушка. оцінка позиції недоступна в грі онлайн
            timeRemainingMs = null // таймери можна додати з Firebase пізніше
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDisplayData())

    val bottomPlayerDisplayData: StateFlow<PlayerDisplayData> = combine(_uiState, _whiteCaptured, _blackCaptured) { ui, whiteCap, blackCap ->
        val isFlipped = ui.isBoardFlipped
        PlayerDisplayData(
            capturedPieces = if (isFlipped) whiteCap else blackCap,
            advantage = 0,
            timeRemainingMs = null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDisplayData())


    private val gameEventListener: ValueEventListener   // слухач Firebase

    // слухач оновлює стан гри
    init {
        // ініціюємо порожню дошку
        repeat(81) { index ->
            boardUiState.add(SquareUiState(index = index, squareName = "", piece = null))
        }
        // підписуємося на оновлення гри в реальному часі
        gameEventListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                @Suppress("UNCHECKED_CAST")
                val data = snapshot.value as? Map<String, Any> ?: return

                updateStateFromFirebase(data) // викликаємо функцію парсингу
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlineGameVM", "Error: ${error.message}")
            }
        })
    }

    // головна логіка парсингу

    // перетворює Map з Firebase на UI State
    private fun updateStateFromFirebase(data: Map<String, Any>) {
        // 1. визначаємо колір гравця та відстежуємо чергу ходу
        val whitePlayerId = data["whitePlayerId"] as? String
        val myColor = if (whitePlayerId == myUserId) PlayerColor.WHITE else PlayerColor.BLACK
        val currentPlayerStr = data["currentPlayer"] as? String ?: "WHITE"
        val currentPlayerColor = PlayerColor.valueOf(currentPlayerStr)

        // 2. оновлюємо загальний UI стан
        _uiState.update { current ->
            current.copy(
                humanPlayerColor = myColor.toJniValue(),
                isBoardFlipped = myColor == PlayerColor.BLACK, // чорні знизу для гравця за чорних
                outcome = data["outcome"] as? String,
                notationHistory = (data["notationHistory"] as? List<String>) ?: emptyList()
            )
        }

        // 3. парсимо дошку (boardState)
        // Firebase зберігає List<List<Map>> (перелік переліків - по рядах), треба заповнити ним boardUiState
        @Suppress("UNCHECKED_CAST")
        val rawBoard = data["boardState"] as? List<List<Map<String, String>?>> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val lastMoveMap = data["lastMove"] as? Map<String, Long>

        // витягаємо координати останнього ходу для підсвітки
        val lastMoveFromIndex = parseFirebaseSquareIndex(lastMoveMap?.get("from") as? Map<String, Long>)
        val lastMoveToIndex = parseFirebaseSquareIndex(lastMoveMap?.get("to") as? Map<String, Long>)

        // оновлюємо кожну клітинку. TODO: замінити на інкрементальні оновлення
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val index = row * 9 + col
                // отримуємо дані фігури (безпечно обробляючи null і межі масиву)
                val pieceMap = rawBoard.getOrNull(row)?.getOrNull(col)
                val piece = if (pieceMap != null) {
                    try {
                        ChessPiece(
                            type = PieceType.valueOf(pieceMap["type"] ?: "PAWN"),
                            color = PlayerColor.valueOf(pieceMap["color"] ?: "WHITE")
                        )
                    } catch (e: Exception) { null }
                } else null

                // оновлюємо стан клітинки
                boardUiState[index] = boardUiState[index].copy(
                    piece = piece,
                    squareName = getSquareName(index, myColor == PlayerColor.BLACK), // треба взяти з глобальних налаштувань
                    isLastMove = (index == lastMoveFromIndex || index == lastMoveToIndex),
                    isSelected = false, // скидаємо виділення
                    isLegalMoveTarget = false // скидаємо можливі ходи
                )
            }
        }

        // 4. оновлюємо взяті фігури (поки що просто заглушка)
        // TODO: Реалізувати парсинг списків whiteCaptured/blackCaptured з Firebase у List<Int> кодів
    }

    // допоміжна функція для отримання індексу з координат Firebase (зберігаються як map "first"->row, "second"->col)
    private fun parseFirebaseSquareIndex(coordMap: Map<String, Long>?): Int? {
        if (coordMap == null) return null
        val row = coordMap["first"]?.toInt() ?: return null
        val col = coordMap["second"]?.toInt() ?: return null
        return row * 9 + col
    }

    // допоміжна функція для назв клітинок (можна винести в util)
    private fun getSquareName(index: Int, isFlipped: Boolean): String {
        return if (isFlipped) squareNamesAllFlipped[index] else squareNamesAll[index]
    }

    // логіка ходу (тимчасова, потрібно підімкнути C++)
    fun handleSquareClick(index: Int) {
        // поки що лише відображаємо дошку. треба використати глобальний handleSquareClick або ідентичний
        // додамо сюди виклик Engine81Bridge для перевірки легальності ходу
        Log.d("OnlineGame", "Clicked square: $index")
    }

    // конвертує поточний стан UI в формат списків для Firebase (спрощено)
    private fun createNewBoardListAfterMove(from: Int, to: Int): List<List<Map<String, String>?>> {
        val rows = MutableList(9) { MutableList<Map<String, String>?>(9) { null } }

        // заповнюємо зі старого стану
        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            val piece = boardUiState[i].piece
            if (piece != null) {
                rows[r][c] = mapOf("type" to piece.type.name, "color" to piece.color.name)
            }
        }

        // робимо хід
        val movingPieceMap = rows[from / 9][from % 9]
        rows[to / 9][to % 9] = movingPieceMap // ставимо на нове місце
        rows[from / 9][from % 9] = null       // видаляємо зі старого

        return rows
    }

    private fun executeMove(fromIndex: Int, toIndex: Int) {
        // формуємо об'єкт оновлення для Firebase.
        // поки зробимо "наївне" оновлення: просто перемістимо фігуру в масиві і відправимо на сервер.
        // сервер або клієнт іншого гравця перевірять і відобразять це.

        // УВАГА: Для повноцінної роботи тут треба реалізувати логіку зміни прав рокірування,
        // перетворення пішаків і т.д. - з рушія.
        // але для початку зробимо базове переміщення, щоб перевірити зв'язок.

        val newBoardList = createNewBoardListAfterMove(fromIndex, toIndex)
        val nextPlayer = if (myColor == PlayerColor.WHITE) "BLACK" else "WHITE"

        // оновлюємо дані в Firebase
        val updates = hashMapOf<String, Any>(
            "boardState" to newBoardList,
            "currentPlayer" to nextPlayer,
            "lastMove" to mapOf(
                "from" to mapOf("first" to fromIndex / 9, "second" to fromIndex % 9),
                "to" to mapOf("first" to toIndex / 9, "second" to toIndex % 9),
                "notation" to "move" // Нотацію поки що заглушимо
            )
            // також треба оновити castlingRights та enPassantTarget, якщо вони змінились
        )

        database.updateChildren(updates).addOnFailureListener {
            Log.e("OnlineGame", "Failed to send move: ${it.message}")
        }
    }

    fun resign() {
        val color = _myPlayerColor.value ?: return
        val outcome = if (color == PlayerColor.WHITE) "BLACK_WINS" else "WHITE_WINS"
        database.child("outcome").setValue(outcome)
    }
    // прибираємо слухача, коли ViewModel знищується
    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(gameEventListener)
    }

    private fun List<ChessPiece>.toFirebaseList(): List<Map<String, String>> {
        return this.map { piece ->
            mapOf("type" to piece.type.name, "color" to piece.color.name)
        }
    }

    // логіка конвертації даних

    private fun arrayFromFirebase(firebaseList: List<List<Map<String, String>?>>): Array<Array<ChessPiece?>> {
        return firebaseList.map { row ->
            row.map { pieceMap ->
                pieceMap?.let {
                    ChessPiece(PieceType.valueOf(it["type"]!!), PlayerColor.valueOf(it["color"]!!))
                }
            }.toTypedArray()
        }.toTypedArray()
    }
}

// допоміжні функції для роботи з бітбордами в Kotlin
private fun setBit(bitboard: LongArray, square: Int) {
    if (square < 64) {
        bitboard[0] = bitboard[0] or (1L shl square)
    } else {
        bitboard[1] = bitboard[1] or (1L shl (square - 64))
    }
}

private fun clearBit(bitboard: LongArray, square: Int) {
    if (square < 64) {
        bitboard[0] = bitboard[0] and (1L shl square).inv()
    } else {
        bitboard[1] = bitboard[1] and (1L shl (square - 64)).inv()
    }
}

// допоміжна функція для швидкого парсингу бітборду (LongArray з 2 елементів) у перелік індексів
fun extractIndicesFromBitboard(bitboard: LongArray): Set<Int> {
    if (bitboard.size < 2) return emptySet()
    val indices = mutableSetOf<Int>()

    // парсимо першу частину (клітинки 0-63)
    var bits0 = bitboard[0]
    while (bits0 != 0L) {
        val square = bits0.countTrailingZeroBits() // знаходимо позицію 1-ці
        indices.add(square)
        bits0 = bits0 and (bits0 - 1L) // Скидаємо цей біт в 0
    }

    // парсимо другу частину (клітинки 64-80)
    var bits1 = bitboard[1]
    while (bits1 != 0L) {
        val square = bits1.countTrailingZeroBits()
        indices.add(square + 64) // робимо зсув
        bits1 = bits1 and (bits1 - 1L)
    }

    return indices
}

// допоміжна функція для перевірки, чи встановлено біт на клітинці
private fun isBitSet(bitboard: LongArray, square: Int): Boolean {
    return if (square < 64) {
        // перевіряємо першу частину бітборду
        (bitboard[0] shr square) and 1L == 1L
    } else {
        // перевіряємо другу частину бітборду
        (bitboard[1] shr (square - 64)) and 1L == 1L
    }
}

// об'єкт-міст для зв'язку з нативною бібліотекою C++
object Engine81Bridge {
    init {
        System.loadLibrary("engine81")
    }

    // управління життєвим циклом рушія
    external fun initTablesJNI(assetManager: AssetManager) // ініціює таблиці атак (magic bitboards і т.д.)
    external fun createGameSessionJNI(timerDurationMinutes: Int): Long // створює нову ігрову сесію в c++ і повертає вказівник на неї
    external fun destroyGameSessionJNI(sessionPtr: Long) // знищує сесію в c++ і звільняє пам'ять
    external fun checkGameStatusJNI(sessionPtr: Long): JniGameStatus? // перевіряє статус гри, зокрема, таймер

    // основна взаємодія з рушієм, робить хід і повертає об'єкт з даними  для інкрементального оновлення UI та нотацією
    external fun makeMoveIncrementalJNI(sessionPtr: Long, moveEncoded: Int): JniMoveResult

    // функції отримання легальних ходів і виконання ходів
    external fun getLegalDestinationsForSquareJNI(sessionPtr: Long, fromSquare: Int): IntArray
    external fun undoMoveJNI(sessionPtr: Long): JniGameStateUpdate // відмотує на хід назад у сесії

    external fun redoMoveJNI(sessionPtr: Long): JniGameStateUpdate // повторює відмотаний хід
    external fun getHistoryInfoJNI(sessionPtr: Long): IntArray // для повторення ходів

    external fun findBestMoveJNI(sessionPtr: Long, depth: Int): String // просить рушій знайти найкращий хід для поточної позиції
    external fun stopSearchJNI() // просить рушій зупинити пошук

    external fun destroyEngineJNI() // звільнення ресурсів від потоків

    // взаємодія з історією
    external fun getHistoryMovesJNI(sessionPtr: Long): IntArray // отримує перелік зроблених ходів у вигляді масиву закодованих цілих чисел для відображення клікабельної історії в ui

    // moveIndex: індекс ходу в масиві з getHistoryMovesJNI. -1 для початкової позиції. метод не змінює реальний стан гри в сесії
    external fun getStateForHistoryMoveJNI(sessionPtr: Long, moveIndex: Int): JniGameStateUpdate // отримує стан дошки на будь-який момент в історії
    
    // завантаження завершених/незавершених партій
    external fun loadGameSessionJNI(historyMoves: IntArray, whiteTimeMs: Long, blackTimeMs: Long): Long
    
    // допоміжні функції для аналізу та UI
    external fun generateAllMovesJNI(
        java_piece_bitboards: Array<LongArray>,
        java_side_to_move: Int, // хід того кольору, чия фігура вибрана
        castling_rights: Int,  // прапори рокірування
        en_passant_square: Int
    ): IntArray

    // завершує гру за згодою, @param sessionPtr - вказівник на поточну ігрову сесію.
    // @return JniMoveResult зі статусом гри "AGREEMENT" та нотацією "½-½"
    external fun drawByAgreementJNI(sessionPtr: Long): JniMoveResult?

    // гравець здається. рушій визначає результат (поразка або нічия). @param resigningPlayerColor - колір гравця, котрий здається (0 - білі, 1 - чорні).
    external fun resignJNI(sessionPtr: Long, resigningPlayerColor: Int): JniMoveResult?

    external fun getAttackMapJNI(
        pieceBitboards: Array<LongArray>,
        colorForMap: Int
    ): LongArray

    external fun runPerftJNIBenchmark(
        depth: Int,
        pieceBitboards: Array<LongArray>,
        sideToMove: Int,
        enPassantSquare: Int,
        castlingRights: Int
    ): String

    // багатопотоковий бенчмарк
    external fun runPerftMultiThreadedJNI(
        depth: Int,
        pieceBitboards: Array<LongArray>,
        sideToMove: Int,
        enPassantSquare: Int,
        castlingRights: Int
    ): String

    // FIXME тимчасовий код для тесту 
    // глобальний для всього додатку стан типу ходу для UI
    var activeGuardMoveType by mutableIntStateOf(1)

    // JNI метод для виклику перемикача
    external fun setGuardMoveTypeJNI(type: Int)

    // функція для перемикання гвардійця
    fun updateGuardType(type: Int) {
        activeGuardMoveType = type
        setGuardMoveTypeJNI(type)
    }
}

// data class, що точно відповідає структурі даних, яку повертає JNI
data class JniKnightsTourUpdate(
    val knightPosition: Int,
    val visitedMask: LongArray,
    val legalMovesMask: LongArray,
    val status: Int, // 0: ONGOING, 1: WIN, 2: LOSS
    val pathSize: Int,
    val path: IntArray // шлях вирішення
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JniKnightsTourUpdate

        if (knightPosition != other.knightPosition) return false
        if (status != other.status) return false
        if (pathSize != other.pathSize) return false
        if (!visitedMask.contentEquals(other.visitedMask)) return false
        if (!legalMovesMask.contentEquals(other.legalMovesMask)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = knightPosition
        result = 31 * result + status
        result = 31 * result + pathSize
        result = 31 * result + visitedMask.contentHashCode()
        result = 31 * result + legalMovesMask.contentHashCode()
        return result
    }
}

// об'єкт-міст для зв'язку з C++
object KnightsTourBridge {
    init {
        System.loadLibrary("engine81")
    }
    external fun createGameSessionJNI(): Long
    external fun destroyGameSessionJNI(sessionPtr: Long)

    external fun makeMoveJNI(sessionPtr: Long, toSquare: Int): JniKnightsTourUpdate
    external fun unmakeLastMoveJNI(sessionPtr: Long)
    external fun getGameStateJNI(sessionPtr: Long): JniKnightsTourUpdate
    external fun solveJNI(sessionPtr: Long): Boolean
    external fun createGameSessionFromStateJNI(sourceSessionPtr: Long): Long
    external fun getSolutionPathJNI(sessionPtr: Long): IntArray
}

class PuzzlesViewModel : ViewModel() {
    private var sessionPtr: Long = KnightsTourBridge.createGameSessionJNI()

    private val _gameState = MutableStateFlow(KnightsTourBridge.getGameStateJNI(sessionPtr))
    val gameState: StateFlow<JniKnightsTourUpdate> = _gameState

    // стан для поточної анімації
    private val _animatedMove = MutableStateFlow<AnimatedMove?>(null)
    val animatedMove: StateFlow<AnimatedMove?> = _animatedMove

    // стан для клітинок, котрі треба приховати
    private val _squaresToHide = MutableStateFlow<Set<Int>>(emptySet())
    val squaresToHide: StateFlow<Set<Int>> = _squaresToHide

    // стан плеєра звуків
    private val _soundEffect = MutableSharedFlow<ChessSoundPlayer.SoundType>()
    val soundEffect = _soundEffect.asSharedFlow()

    private val _isSolving = MutableStateFlow(false)
    val isSolving: StateFlow<Boolean> = _isSolving

    private var solutionPath: List<Int>? = null // оголошуємо solutionPath як приватну властивість класу

    // чи існує рішення з поточної позиції
    private val _isSolvable = MutableStateFlow(true)
    val isSolvable: StateFlow<Boolean> = _isSolvable
    private val _isAutoSolved = MutableStateFlow(false)
    val isAutoSolved: StateFlow<Boolean> = _isAutoSolved
    private val _hintSquare = MutableStateFlow<Int?>(null)
    val hintSquare: StateFlow<Int?> = _hintSquare

    init {
        runBackgroundSolver()
    }

    private fun runBackgroundSolver() {
        viewModelScope.launch(Dispatchers.Default) {
            // створюємо тимчасову сесію-копію для безпечного пошуку
            val tempSessionPtr = KnightsTourBridge.createGameSessionFromStateJNI(sessionPtr)
            val success = KnightsTourBridge.solveJNI(tempSessionPtr)
            if (success) {
                // якщо рішення знайдено, отримуємо повний шлях з тимчасової сесії
                solutionPath = KnightsTourBridge.getSolutionPathJNI(tempSessionPtr).toList() // і цю
            } else {
                solutionPath = null
            }
            // оновлюємо публічний стан для UI.
            _isSolvable.value = (solutionPath != null)
            // знищуємо тимчасову сесію
            KnightsTourBridge.destroyGameSessionJNI(tempSessionPtr)
        }
    }

    fun makeMove(toSquare: Int) {
        if (_gameState.value.status != 0) return // не робимо ходи, якщо гра закінчена
        val fromSquare = _gameState.value.knightPosition // отримуємо позицію коня до ходу
        val knightPiece = ChessPiece(PieceType.KNIGHT, PlayerColor.WHITE)

        // створюємо дані для анімації та оновлюємо стан
        _animatedMove.value = AnimatedMove(knightPiece, fromSquare, toSquare)
        _squaresToHide.value = setOf(fromSquare, toSquare) // приховуємо фігуру на старті та в кінці

        // виконуємо хід у рушії (це оновить `_gameState`)
        _gameState.value = KnightsTourBridge.makeMoveJNI(sessionPtr, toSquare)
        // надсилаємо подію для відтворення звуку ходу
        viewModelScope.launch { _soundEffect.emit(ChessSoundPlayer.SoundType.MOVE) }

        _hintSquare.value = null // скидаємо підказку після ходу
    }

    // функція завершення анімації
    fun onAnimationFinished() {
        // очищуємо стан анімації
        _animatedMove.value = null
        _squaresToHide.value = emptySet()
    }

    fun undoMove() {
        if (_isSolving.value) return // блокуємо, якщо працює автовирішення
        KnightsTourBridge.unmakeLastMoveJNI(sessionPtr)
        _gameState.value = KnightsTourBridge.getGameStateJNI(sessionPtr)
        // скидаємо стани при поверненні ходу
        _hintSquare.value = null
        _isSolvable.value = true
        _isAutoSolved.value = false
    }

    fun showHint() {
        // захист від кліку якщо вже йде автовирішення
        if (_isSolving.value) return

        viewModelScope.launch(Dispatchers.Default) {
            // створюємо копію сесії для пошуку (щоб не зламати поточну гру)
            val tempSessionPtr = KnightsTourBridge.createGameSessionFromStateJNI(sessionPtr)
            val success = KnightsTourBridge.solveJNI(tempSessionPtr) // шукаємо рішення з поточної позиції

            if (success) {
                _isSolvable.value = true // рішення існує
                val fullPath = KnightsTourBridge.getSolutionPathJNI(tempSessionPtr).toList() // отримуємо знайдений шлях
                val currentPathSize = _gameState.value.pathSize

                // наступний правильний хід за індексом currentPathSize, розмір = індекс наступного ходу
                if (currentPathSize < fullPath.size) {
                    val nextSquare = fullPath[currentPathSize]

                    // оновлюємо UI на головному потоці
                    withContext(Dispatchers.Main) {
                        _hintSquare.value = nextSquare
                    }
                }
            } else {
                // рішення нема. оновлюємо статус, UI одразу покаже повідомлення "Спробуйте скасувати хід"
                _isSolvable.value = false
            }
            KnightsTourBridge.destroyGameSessionJNI(tempSessionPtr)
        }
    }

    fun solvePuzzle() {
        if (_isSolving.value) return // захист від подвійного кліку
        viewModelScope.launch(Dispatchers.Default) {
            _isSolving.value = true
            val tempSessionPtr = KnightsTourBridge.createGameSessionFromStateJNI(sessionPtr)
            val success = KnightsTourBridge.solveJNI(tempSessionPtr) // запускаємо рушій тільки після натискання кнопки

            if (success) {
                // якщо рішення існує:
                _isSolvable.value = true
                _isAutoSolved.value = true // фіксуємо, що працює алгоритм

                val fullPath = KnightsTourBridge.getSolutionPathJNI(tempSessionPtr).toList()
                val currentPathSize = _gameState.value.pathSize

                // анімуємо лише ходи, що ще не зроблені
                for (i in currentPathSize until fullPath.size) {
                    val toSquare = fullPath[i]
                    // робимо хід і оновлюємо UI на головному потоці
                    withContext(Dispatchers.Main) {
                        makeMove(toSquare)
                    }
                    delay(42) // пауза між анімованими ходами для плавності
                }
            } else {
                // якщо success == false, кажемо UI, що рішення немає!
                _isSolvable.value = false
            }
            KnightsTourBridge.destroyGameSessionJNI(tempSessionPtr)
            _isSolving.value = false
        }
    }

    fun restartGame() {
        KnightsTourBridge.destroyGameSessionJNI(sessionPtr)
        sessionPtr = KnightsTourBridge.createGameSessionJNI()
        _gameState.value = KnightsTourBridge.getGameStateJNI(sessionPtr)
        // скидаємо стани для нової гри
        _hintSquare.value = null
        _isSolvable.value = true
        _isAutoSolved.value = false
        // запускаємо пошук для нової гри на випадок, що користувач захоче лише подивитися вирішення, а не грати
        runBackgroundSolver()
    }

    override fun onCleared() {
        super.onCleared()
        KnightsTourBridge.destroyGameSessionJNI(sessionPtr)
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // повертаємо попередню орієнтацію при виході з екрана
            activity.requestedOrientation = originalOrientation
        }
    }
}

@Composable
fun PuzzlesScreen(navController: NavController) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // блокуємо екран пазлів у портретному режимі
    
    val viewModel: PuzzlesViewModel = viewModel()
    val state by viewModel.gameState.collectAsState()
    
    // підписуємось на нові стани анімації
    val animatedMove by viewModel.animatedMove.collectAsState()
    val squaresToHide by viewModel.squaresToHide.collectAsState()

    // логіка плеєра звуків
    val context = LocalContext.current
    val chessSoundPlayer = remember { AudioController.getPlayer(context) } // отримуємо синглтон плеєра
    // слухаємо події з viewModel і відтворюємо
    LaunchedEffect(viewModel.soundEffect) {
        viewModel.soundEffect.collect { soundType ->
            chessSoundPlayer.playSound(soundType)
        }
    }
    
    // підписуємось на статуси
    val isSolving by viewModel.isSolving.collectAsState() // підписуємось на стан пошуку
    val hintSquare by viewModel.hintSquare.collectAsState() // стан для підказки
    val isAutoSolved by viewModel.isAutoSolved.collectAsState() // стан розрізнення хто вирішив пазл
    val isSolvable by viewModel.isSolvable.collectAsState() // стан про можливість вирішення

    val legalMovesIndices = remember(state.legalMovesMask) {
        extractIndicesFromBitboard(state.legalMovesMask)
    }

    // кешуємо відвідані клітинки
    val visitedIndices = remember(state.visitedMask) {
        extractIndicesFromBitboard(state.visitedMask)
    }
    
    // конвертуємо стан з JNI в те, що розуміє ChessBoard
    val boardUiState = remember(state, hintSquare, legalMovesIndices, visitedIndices, squaresToHide) { // залежності remember
        List(81) { index ->
            val isKnightHere = state.knightPosition == index
            val piece = if (isKnightHere) ChessPiece(PieceType.KNIGHT, PlayerColor.WHITE) else null

            SquareUiState(
                index = index,
                squareName = "",
                piece = piece,
                isSelected = isKnightHere, // підсвічуємо коня
                isLegalMoveTarget = legalMovesIndices.contains(index),
                isVisitedSquare = visitedIndices.contains(index),
                isHintSquare = hintSquare == index || (state.knightPosition == index && hintSquare != null),
                shouldHidePiece = squaresToHide.contains(index)
            )
        }
    }

    // повідомлення про результат гри над дошкою
    val headerText = when (state.status) {
        1 -> if (isAutoSolved) "Пазл вирішено\n " else "Перемога!\nВи обійшли всю дошку!"
        2 -> "Поразка!\nНе лишилося доступних ходів."
        else -> if (!isSolvable && state.pathSize > 1) {
            "Рішення з цієї позиції не знайдено.\nСпробуйте скасувати хід."
        } else {
            "Обійдіть всі клітинки конем,\nжодну клітинку не відвідавши двічі."
        }
    }
    val headerColor = when (state.status) {
        1 -> Color(0xFFF4CE0A) // золотистий для перемоги
        2 -> Color(0xE0FF6B6B) // м'який червоний для поразки
        else -> if (!isSolvable && state.pathSize > 1) Color(0xFFFFA04C) else Color.White // помаранчевий для попередження
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        // кнопка "назад"
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = OrientationPaddings.topPadding, start = 8.dp)) {
            Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headerText,
                color = headerColor,
                fontSize = 21.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // центруємо текст
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedChessBoard(
                boardState = boardUiState,
                isFlipped = false,
                animatedMoves = animatedMove?.let { listOf(it) } ?: emptyList(),
                onAnimationFinished = { viewModel.onAnimationFinished() },
                onSquareClick = { row, col ->
                    if (!isSolving) { viewModel.makeMove(row * 9 + col) } // захист від кліків під час автовирішення
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Хід: ${state.pathSize} / 81",
                color = Color.White,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // панель керування
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button( // кнопка "Нова гра"
                    onClick = { viewModel.restartGame() },
                    enabled = !isSolving, // блокуємо під час пошуку
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.resign),
                        contentDescription = "Нова гра",
                        modifier = Modifier.size(32.dp)
                    )
                }
                // кнопка Undo
                Button(onClick = { viewModel.undoMove() }, enabled = !isSolving && state.pathSize > 1, colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)) {
                    Icon(
                        painter = painterResource(id = R.drawable.undo),
                        contentDescription = "Undo",
                        modifier = Modifier.size(32.dp)
                    )
                }
                // кнопка підказки
                Button(onClick = { viewModel.showHint() }, enabled = !isSolving && state.status == 0, colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)) {
                    Icon(
                        painter = painterResource(id = R.drawable.bulb), // іконка лампочки
                        contentDescription = "Підказка",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Button( // кнопка "Вирішити"
                    onClick = { viewModel.solvePuzzle() },
                    enabled = !isSolving && state.status == 0, // кнопка активна, лише якщо гра триває
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.done), // іконка поставити пташку
                        contentDescription = "Вирішити",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    outcomeText: String,
    moveCount: Int,
    onNewGame: () -> Unit,
    onExit: () -> Unit,
    onDismiss: () -> Unit
) {
    // форматуємо фінальне повідомлення
    val fullMoveNumber = (moveCount + 1) / 2
    val message = "$outcomeText після $fullMoveNumber ходів"

    AlertDialog(
        onDismissRequest = onDismiss, // дозволяємо закривати діалог
        title = { Text("Гру завершено", color = Color.White) },
        text = { Text(message, color = Color.White) },
        containerColor = backgroundColor, // темний фон
        confirmButton = {
            Button(onClick = onNewGame, colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)) {
                Text("Реванш", fontSize = 16.sp)
            }
        },
        dismissButton = {
            // кнопка "Меню"
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
            ) {
                Text("Меню", fontSize = 16.sp)
            }
        }
        // TODO: додати кнопку налаштувань для зміни таймера і типу гри, дозволів на відміну ходів і ходи на випередження, зміну звуків і теми тощо
        // TODO: додати кнопки аналіз і експорт запису гри
    )
}

@Composable
fun DifficultySelectorDropDown(
    currentDepth: Int,
    onDepthSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = OrientationPaddings.topPadding)) {
        // кнопка-контейнер (іконка + текст)
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp), // збільшуємо зону кліку
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.robot), // іконка робота
                contentDescription = "Глибина пошуку для шахового бота",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = "Глибина $currentDepth",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // випадаюче меню
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.DarkGray)
        ) {
            val depths = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) // перелік рівнів складності (глибини пошуку)

            depths.forEach { depth ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Глибина $depth",
                            color = if (depth == currentDepth) Color.Yellow else Color.White
                        )
                    },
                    onClick = {
                        onDepthSelected(depth) // передаємо нове значення
                        expanded = false
                    }
                )
            }
        }
    }
}

@SuppressLint("AutoboxingStateCreation")
@Composable
fun GameSetupDialog(
    showDialog: Boolean, // лямбда для закриття діалогу
    onDismiss: () -> Unit, // тап поза діалогом або вибір кольору
    onBack: () -> Unit,    // для системної кнопки "назад"
    onGameStart: (selectedColor: Int, selectedTimer: Int) -> Unit
) {
    if (!showDialog) return

    // отримуємо глобально збережений таймер як значення за замовчуванням
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    // стан вибору таймера з дефолтним вибором 0
    var selectedTimer by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_BOT_TIMER_DURATION, "0").toIntOrNull() ?: 0)
    }
    

    val timerOptions = listOf(0, 3, 5, 10, 15, 30, 60) // 0 = без таймера

    // використовуємо базовий Dialog, щоб мати повний контроль
    Dialog(onDismissRequest = { onDismiss() }) { // Dialog(onDismissRequest = { onGameStart(0, selectedTimer) }) {
        // власний обробник для системної кнопки "назад"
        BackHandler { onBack() }

        // вміст діалогу, огорнутий у Surface для фону і форми
        Surface(
            modifier = Modifier.width(260.dp), // розмір діалогу
            shape = RoundedCornerShape(28.dp), // стандартне заокруглення для діалогів
            color = backgroundColor
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // вибір кольору відразу починає гру
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(22.dp))
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10))
                            .background(Color(0xFF2E2E2E))
                            .clickable { onGameStart(0, selectedTimer) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wk), // іконка білого короля
                            contentDescription = "Грати за білих",
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10))
                            .background(Color(0xFF424242))
                            .clickable { onGameStart(1, selectedTimer) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bk), // іконка чорного короля
                            contentDescription = "Грати за чорних",
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Spacer(Modifier.width(22.dp))
                }
                Spacer(Modifier.height(16.dp))
                // вибір таймера лише оновлює стан
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(timerOptions.size) { index ->
                        val duration = timerOptions[index]
                        val isSelected = selectedTimer == duration

                        Button(
                            onClick = {
                                selectedTimer = duration
                                // відразу зберігаємо вибраний час для бота
                                settingsManager.saveSetting(SettingsManager.KEY_BOT_TIMER_DURATION, duration.toString())
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) boardDarkColor else buttonStyleColor
                            )
                        ) {
                            Text(if (duration == 0) "∞" else "$duration")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotGameScreen(navController: NavController) {
    val context = LocalContext.current
    // отримуємо viewModel, що керує C++ рушієм
    val viewModel: ChessViewModel = viewModel(factory = ChessViewModelFactory(context.applicationContext, isBotGame = true))
    val settings = LocalUiSettings.current
    LaunchedEffect(settings.animationsEnabled, settings.gameMode, settings.cellNameMode) {
        viewModel.updateRuntimeSettings(
            settings.animationsEnabled,
            settings.gameMode,
            settings.cellNameMode
        )
    }
    val jniGameState by viewModel.gameState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    // отримуємо дошку для UI
    val boardUiState = viewModel.boardUiState

    // отримуємо інформацію про історію з c++ сесії для кнопок undo/redo
    val historyInfo = viewModel.getHistoryInfo()
    val currentHistoryIndex = historyInfo[0]
    val historyTotalSize = historyInfo[1]
    // визначаємо поточний індекс в історії для підсвітки
    val currentMoveIndexForView = if (uiState.isViewingHistory) {
        uiState.currentHistoryViewIndex
    } else {
        currentHistoryIndex
    }
    // підписуємося на дані для дисплеїв матеріалу і переваги
    val topData by viewModel.topPlayerDisplayData.collectAsState()
    val bottomData by viewModel.bottomPlayerDisplayData.collectAsState()
    // стани для діалогів
    var showResignDialog by remember { mutableStateOf(false) }
    var showDrawDialog by remember { mutableStateOf(false) }

    // обробка системної кнопки "назад"
    BackHandler {
        navController.popBackStack()
    }
    // логіка для автоматичного ходу бота
    LaunchedEffect(jniGameState.sideToMove, uiState.humanPlayerColor, uiState.isViewingHistory) {
        // бот робить хід, тільки якщо ми не переглядаємо історію
        if (!uiState.isViewingHistory && jniGameState.sideToMove != uiState.humanPlayerColor && uiState.outcome == null) {
            viewModel.findAndMakeBestMove()
        }
    }

    // отримуємо синглтон плеєра
    val chessSoundPlayer = remember { AudioController.getPlayer(context) }

    // слухаємо події з viewModel і відтворюємо
    LaunchedEffect(viewModel.soundEffect) {
        viewModel.soundEffect.collect { soundType ->
            chessSoundPlayer.playSound(soundType)
        }
    }

    val settingsManager = remember { SettingsManager(context) }

    // діалог завершення гри
    if (uiState.outcome != null) {
        GameOverDialog(
            outcomeText = uiState.outcome!!,
            moveCount = uiState.notationHistory.size,
            onNewGame = { // читаємо збережений таймер бота
                val timerDuration = settingsManager.getSetting(SettingsManager.KEY_BOT_TIMER_DURATION, "0").toIntOrNull() ?: 0
                viewModel.startNewGame(timerDuration)
            },
            onExit = { navController.popBackStack() },
            onDismiss = { viewModel.dismissOutcome() }
        )
    }

    // визначаємо орієнтацію екрану
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // UI екрану
    BoardElementsLayout(
        // слот для дошки
        board = {
            AnimatedChessBoard(
                boardState = boardUiState,
                isFlipped = uiState.isBoardFlipped, // дошка перевертається залежно від вибору гравця
                animatedMoves = uiState.animatedMove?.let { listOf(it) } ?: emptyList(), // конвертуємо хід у список для анімації
                onAnimationFinished = { viewModel.clearAnimation() },
                onSquareClick = { row, col -> viewModel.handleSquareClick(row * 9 + col, "bot") }
            )
        },
        // слот для верхньої панелі
        topPlayerInfo = {
            PlayerInfoPanel(data = topData, isTopPanel = true)
        },
        // слот для нижньої панелі
        bottomPlayerInfo = {
            PlayerInfoPanel(data = bottomData, isTopPanel = false)
        },
        // слот для нотації
        notation = {
            NotationView(
                notationList = uiState.notationHistory,
                currentMoveIndex = currentMoveIndexForView, // для зеленої підсвітки
                activeEngineMoveIndex = currentHistoryIndex, // для визначення тьмяних ходів
                onNotationClick = { index ->
                    if (index == uiState.notationHistory.size - 1 && uiState.isViewingHistory) {
                        viewModel.returnToCurrentGame()
                    } else {
                        viewModel.goToHistoryState(index)
                    }
                    viewModel.playSoundForHistoryMove(index)
                }
            )
        },
        // слот для основних кнопок керування грою
        gameControls = {
            // визначаємо, чи дозволений takeback (немає таймера + дозволено в налаштуваннях)
            val isTimerGame = (uiState.whiteTimeRemainingMs ?: -1L) >= 0L || (uiState.blackTimeRemainingMs ?: -1L) >= 0L
            val allowTakeback = settings.allowUndo && !isTimerGame

            GameControlsPanel(
                isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
                gameMode = "bot",
                autoFlipEnabled = false,
                onFlipBoard = { viewModel.flipBoard() },
                onUndo = {
                    if (allowTakeback) {
                        viewModel.undoBotGameMove()
                    } else {
                        // навігація назад
                        if (currentMoveIndexForView >= 0) {
                            val prevIndex = currentMoveIndexForView - 1
                            viewModel.goToHistoryState(prevIndex)
                            viewModel.playSoundForHistoryMove(prevIndex)
                        }
                    }
                },
                onRedo = {
                    if (allowTakeback) {
                        viewModel.redoTwoHalfMoves()
                    } else {
                        // навігація вперед
                        val nextIndex = currentMoveIndexForView + 1
                        if (nextIndex == uiState.notationHistory.size - 1) {
                            viewModel.returnToCurrentGame()
                        } else if (nextIndex < uiState.notationHistory.size - 1) {
                            viewModel.goToHistoryState(nextIndex)
                        }
                        if (nextIndex < uiState.notationHistory.size) {
                            viewModel.playSoundForHistoryMove(nextIndex)
                        }
                    }
                },
                onResign = { showResignDialog = true },
                onDrawOffer = { showDrawDialog = true },
                isUndoEnabled = if (allowTakeback) true else currentMoveIndexForView >= 0, // активність кнопок змінюється залежно від ситуації
                isRedoEnabled = if (allowTakeback) currentHistoryIndex < historyTotalSize - 1 else currentMoveIndexForView < uiState.notationHistory.size - 1,
                allowTakeback = allowTakeback, // прапорець дозволу в налаштуваннях
                onSetupGame = { viewModel.showGameSetup() }
            )
        },
        statsPanel = {
            if (!isLandscape) {
                StatsPanel(history = uiState.searchStatsHistory, isVisible = uiState.isStatsPanelVisible)
            }
        }
    ) // закриваємо BoardElementsLayout

    Row { // кнопка назад у вигляді стандартної стрілки
        IconButton(onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = OrientationPaddings.topPadding, start = 8.dp),
        ) {
            Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(18.dp))
        // стан для перемикання режиму тестування
        var isMultiThreaded by remember { mutableStateOf(true) }
        if (uiState.isSearchRunning) {
            Button(
                onClick = { viewModel.stopSearch() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)), // червоний колір для зупинки
                modifier = Modifier.weight(1f).padding(top = OrientationPaddings.topPadding)
            ) {
                Text("Зупинити раніше", fontSize = 17.sp)
            }
        } else if (uiState.isStatsPanelVisible) {
            Button(
                onClick = { viewModel.runPerft(isMultiThreaded) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x00000000)), // прозорий колір для приховання кнопки
                modifier = Modifier.weight(1f).padding(top = OrientationPaddings.topPadding).height(40.dp), // встановлює фіксовану висоту 40dp
            ) {
                Text("", fontSize = 18.sp)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(8.dp))
       
        DifficultySelectorDropDown( // кнопка вибору глибини
            currentDepth = uiState.engineSearchDepth,
            onDepthSelected = { newDepth -> viewModel.changeSearchDepth(newDepth) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        // кнопка відображення статистики
        if (!isLandscape) {
            IconButton(
                onClick = { viewModel.toggleStatsVisibility() },
                modifier = Modifier.padding(top = OrientationPaddings.topPadding)
            ) {
                val tint = if (uiState.isStatsPanelVisible) Color.Yellow else Color.Gray // змінюємо колір іконки, якщо панель активна
                Icon(
                    painter = painterResource(id = R.drawable.stats), // використовуємо іконку stats
                    contentDescription = "Статистика",
                    tint = tint,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }

    // діалог вибору часу і кольору на гру
    GameSetupDialog(
        showDialog = uiState.showPreGameDialog,
        onDismiss = { viewModel.dismissPreGameDialog() }, // клік поза діалог закриває його, і гру можна почати з дефолтними налаштуваннями
        onBack = { navController.popBackStack() }, // системна кнопка "назад" - повернення на головний екран
        onGameStart = { color, timer -> viewModel.startNewGame(timer, color) } // якщо клікнули колір, починаємо гру з вибраними параметрами
    )

    // діалог підтвердження здачі
    if (showResignDialog) {
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            title = { Text("Здатися?", color = Color.White) },
            text = { Text("Ви впевнені?", color = Color.White) },
            containerColor = Color(0xFF2E2E2E),
            confirmButton = {
                Button(onClick = {
                    viewModel.stopSearch() // зупиняємо пошук із завершенням гри
                    viewModel.resign()
                    showResignDialog = false
                },
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) { Text("Так") }
            },
            dismissButton = {
                Button(onClick = { showResignDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)) { Text("Ні") }
            }
        )
    }

    // діалог підтвердження нічиєї
    if (showDrawDialog) {
        AlertDialog(
            onDismissRequest = { showDrawDialog = false },
            title = { Text("Запропонувати нічию?", color = Color.White) },
            text = { Text("Бот завжди згоден на нічию.", color = Color.White) }, // TODO: додати логіку відхилення нічиєї, якщо бот виграє
            containerColor = Color(0xFF2E2E2E),
            confirmButton = {
                Button(onClick = {
                    viewModel.stopSearch() // зупиняємо пошук із завершенням гри
                    viewModel.offerDraw()
                    showDrawDialog = false
                },
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) { Text("Прийняти") }
            },
            dismissButton = {
                Button(onClick = { showDrawDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)) { Text("Відхилити") }
            }
        )
    }

    // діалог промоції
    if (uiState.promotionCandidate != null) {
        PromotionDialog(
            playerColor = if (jniGameState.sideToMove == 0) PlayerColor.WHITE else PlayerColor.BLACK,
            onPieceSelected = { chosenPieceType ->
                // викликаємо метод ViewModel, що оновить всі стани
                viewModel.resolvePromotion(chosenPieceType)
            }
        )
    }
}

@Composable
fun CapturedPiecesDisplay(capturedPieces: List<ChessPiece>) {
    // сортуємо фігури за цінністю для красивішого відображення
    val sortedPieces = capturedPieces.sortedByDescending { it.type.value() }
    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp), // накладання іконок
        verticalAlignment = Alignment.CenterVertically
    ) {
        sortedPieces.forEach { piece ->
            Image(
                painter = painterResource(id = pieceToDrawableResource(piece)),
                contentDescription = "Captured ${piece.type}",
                modifier = Modifier.size(20.dp) // розмір іконок
            )
        }
    }
}

// повертає цінність фігури в пішаках
fun PieceType.value(): Int = when (this) {
    PieceType.PAWN -> 1
    PieceType.KNIGHT -> 3
    PieceType.BISHOP -> 4
    PieceType.ROOK -> 5
    PieceType.GUARD -> 6
    PieceType.QUEEN -> 10
    PieceType.KING -> 0 // король в розрахунку матеріалу не враховується
}

private fun promotionPieceTypeToFlag(pieceType: PieceType): Int {
    return when (pieceType) {
        PieceType.QUEEN -> 12
        PieceType.ROOK -> 11
        PieceType.GUARD -> 10
        PieceType.BISHOP -> 9
        PieceType.KNIGHT -> 8
        else -> 0 // не має трапитись
    }
}

@Composable
fun NotationView(
    notationList: List<String>,
    currentMoveIndex: Int, // індекс поточного ходу для підсвічування
    activeEngineMoveIndex: Int, // реальний індекс рушія в переліку ходів для тьмяних елементів
    onNotationClick: (Int) -> Unit // лямбда для обробки кліку
) {
    val listState = rememberLazyListState()

    // автоматично прокручуємо до нового ходу
    LaunchedEffect(currentMoveIndex) {
        if (currentMoveIndex > 0) {
            // прокручуємо до елемента, який передує поточному, щоб його було видно
            listState.animateScrollToItem( (currentMoveIndex - 1).coerceAtLeast(0) )
        }
    }

    // горизонтальний список, що прокручується
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(notationList) { index, notation -> // `index` тут від 0 до size-1
            val moveNumber = (index / 2) + 1
            val isSelected = index == currentMoveIndex // підсвічуємо вибраний індекс
            val isFutureMove = index > activeEngineMoveIndex // перевірка на майбутній хід. тьмяними стають лише ходи, більші за реальний індекс рушія

            val prefix = if (index % 2 == 0) "$moveNumber. " else ""

            // динамічний колір тексту залежно від стану
            val textColor = when {
                isSelected -> Color.White
                isFutureMove -> Color.Gray.copy(alpha = 0.7f) // темніший напівпрозорий для скасованих
                else -> Color.LightGray // світло-сірий для звичайних минулих ходів
            }

            Text(
                text = "$prefix$notation",
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(
                        // підсвічуємо вибраний хід іншим кольором
                        if (isSelected) Color(0xFF004D40) else Color.DarkGray.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onNotationClick(index) } // викликаємо лямбду чистий індекс (0-based)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun OnlineGameScreen(gameId: String, navController: NavController) {
    val viewModel: OnlineGameViewModel = viewModel(factory = OnlineGameViewModelFactory(gameId))
    // підписуємось на стани
    val uiState by viewModel.uiState.collectAsState()
//    LaunchedEffect(settings.animationsEnabled, settings.gameMode, settings.cellNameMode) {
//        viewModel.updateRuntimeSettings(
//            settings.animationsEnabled,
//            settings.gameMode,
//            settings.cellNameMode
//        )
//    }
    
    val topData by viewModel.topPlayerDisplayData.collectAsState()
    val bottomData by viewModel.bottomPlayerDisplayData.collectAsState()
    val myPlayerColor by viewModel.myPlayerColor.collectAsState()
//    val isMyTurn by viewModel.isMyTurn.collectAsState()
    // отримуємо дані для рендеру дошки
    val boardUiState = viewModel.boardUiState
    val color = myPlayerColor

    // плеєр звуків
    val context = LocalContext.current
    // отримуємо синглтон плеєра
    val chessSoundPlayer = remember { AudioController.getPlayer(context) }

    // викликаємо уніфікований лейаут
    BoardElementsLayout(
        board = {
            AnimatedChessBoard(
                boardState = boardUiState,
                isFlipped = uiState.isBoardFlipped,
                animatedMoves = emptyList(), // TODO: додати анімацію, коли підімкнемо C++ події
                onAnimationFinished = { },
                onSquareClick = { row, col ->
                    viewModel.handleSquareClick(row * 9 + col)
                }
            )
        },
        topPlayerInfo = {
            PlayerInfoPanel(data = topData, isTopPanel = true)
        },
        bottomPlayerInfo = {
            PlayerInfoPanel(data = bottomData, isTopPanel = false)
        },
        notation = {
            NotationView(
                notationList = uiState.notationHistory,
                currentMoveIndex = uiState.notationHistory.size - 1,
                activeEngineMoveIndex = 0, // для визначення тьмяних ходів // заглушка
                onNotationClick = { /* TODO: зробити перегляд історії для онлайну */ }
            )
        },
        gameControls = {
            // тут можна використати GameControlsPanel
            Button(onClick = { viewModel.resign() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF663232))) {
                Text("Здатися")
            }
        }
    )

    // діалог завершення гри
    if (uiState.outcome != null) {
        GameOverDialog(
            outcomeText = uiState.outcome ?: "Гра завершена",
            moveCount = uiState.notationHistory.size,
            onNewGame = { /* Логіка нової гри */ },
            onExit = { navController.popBackStack() },
            onDismiss = { /* viewModel.dismissOutcome() */ }
        )
    }
}

@Composable
fun PromotionDialog(playerColor: PlayerColor, onPieceSelected: (PieceType) -> Unit) {
    // перелік фігур, на котрі можна перетворити пішака
    val promotionPieces = listOf(PieceType.QUEEN, PieceType.GUARD, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

    // діалог займає весь екран, але робить фон напівпрозорим
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.36f)), // напівпрозорий чорний фон
        contentAlignment = Alignment.Center // центруємо вміст діалогу
    ) {
        // ряд кнопок для вибору фігури
        Row(
            modifier = Modifier
                .background(Color.DarkGray) // сірий фон для діалогу
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // відступи між кнопками
        ) {
            promotionPieces.forEach { pieceType ->
                // кожна фігура клікабельна
                Box(modifier = Modifier.clickable { onPieceSelected(pieceType) }) {
                    Image(
                        painter = painterResource(id = pieceToDrawableResource(ChessPiece(pieceType, playerColor))),
                        contentDescription = "Promote to $pieceType",
                        modifier = Modifier.size(48.dp) // розмір зображення фігури
                    )
                }
            }
        }
    }
}

@Composable
fun AdvantageDisplay(advantage: Int) {
    // показуємо перевагу, лише якщо вона більша 0
    if (advantage == 0) {
        Text(" ", fontSize = 16.sp)
        return
    }

    val text = "+$advantage"

    Text(
        text = text,
        color = Color.White, // колір тепер завжди білий, бо позиція вказує на гравця
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}

/**
 * компонент-шаблон, що відповідає виключно за верстку основних елементів ігрового екрану і лише приймає готові UI-блоки у слоти.
 *
 * @param board слот для основного компонента шахової дошки.
 * @param topPlayerInfo слот для верхньої інформаційної панелі гравця.
 * @param bottomPlayerInfo слот для нижньої інформаційної панелі гравця.
 * @param gameControls слот для панелі з основними кнопками керування грою (Здатися, Нічия, Поворот і т.д.).
 * @param notation слот для панелі з історією ходів (нотацією).
 * @param optionalTopControls необов'язковий слот для додаткових елементів керування над дошкою (наприклад, для режиму самоаналізу). За замовчуванням порожній.
 */
@Composable
fun BoardElementsLayout(
    board: @Composable () -> Unit,
    topPlayerInfo: @Composable () -> Unit,
    bottomPlayerInfo: @Composable () -> Unit,
    gameControls: @Composable () -> Unit,
    notation: @Composable () -> Unit,
    optionalTopControls: @Composable () -> Unit = {}, // опційний слот за замовчуванням порожній
    statsPanel: @Composable () -> Unit = {} // слот для статистики рушія теж порожній - щоб не ламати екрани, де він не застосовується
) {
    // обгортка, що встановлює фон для всього екрану
    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // верстка альбомного режиму
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // ліва колонка (нотація, верхня панель, додаткові контролери)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    topPlayerInfo()
                    Spacer(modifier = Modifier.weight(1f).height(16.dp))
                    notation()
                    Spacer(modifier = Modifier.weight(1f).height(16.dp))
                    optionalTopControls() // додаткові контролери тут, якщо вони активні в альбомному режимі
                }

                // центральна колонка з дошкою
                Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f), contentAlignment = Alignment.Center) {
                    board() // виклик слота дошки
                }

                // права колонка (нижня панель та основні кнопки керування)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f).height(16.dp))
                    gameControls() // виклик слота кнопок
                    Spacer(modifier = Modifier.weight(1f).height(16.dp))
                    bottomPlayerInfo()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // верстка портретного режиму
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                optionalTopControls() // виклик опційного слота
                Spacer(modifier = Modifier.height(8.dp))
                notation()
                topPlayerInfo()
                board() // виклик дошки
                bottomPlayerInfo()
                Spacer(modifier = Modifier.height(8.dp))
                gameControls() // виклик слота кнопок
                Spacer(modifier = Modifier.height(8.dp))
                statsPanel()  // слот лише для панелі статистики пошуку
            }
        }
    }
}

@Composable
fun LocalGameScreen(navController: NavController, gameMode: String, isTrainingMode: Boolean) {
    val context = LocalContext.current
    // отримуємо ViewModel, що керує C++ сесією
    val viewModel: ChessViewModel = viewModel(factory = ChessViewModelFactory(context.applicationContext))
    val settings = LocalUiSettings.current
    LaunchedEffect(settings.animationsEnabled, settings.gameMode, settings.cellNameMode) {
        viewModel.updateRuntimeSettings(
            settings.animationsEnabled,
            settings.gameMode,
            settings.cellNameMode
        )
    }
    val jniGameState by viewModel.gameState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val boardUiState = viewModel.boardUiState

    // готові дані для дисплеїв переваги і матеріалу
    val topData by viewModel.topPlayerDisplayData.collectAsState()
    val bottomData by viewModel.bottomPlayerDisplayData.collectAsState()

    // стани для діалогів здачі та нічиєї
    var showResignDialog by remember { mutableStateOf(false) }
    var showDrawDialog by remember { mutableStateOf(false) }
    
    // отримуємо інформацію про історію з C++ сесії
    val historyInfo = viewModel.getHistoryInfo()
    val currentHistoryIndex = historyInfo[0]
    val historyTotalSize = historyInfo[1]
    // визначаємо поточний індекс в історії для підсвітки
    val currentMoveIndexForView = if (uiState.isViewingHistory) {
        uiState.currentHistoryViewIndex
    } else {
        currentHistoryIndex
    }

    // визначаємо, чи потрібно повертати дошку
    val isBoardFlipped by remember(gameMode) {
        derivedStateOf {
            if (gameMode == "hotseat" && uiState.autoFlipEnabled && !uiState.isViewingHistory) { // автоповорот тільки в hotseat і тільки під час активної гри
                jniGameState.sideToMove == PlayerColor.BLACK.toJniValue()
            } else {
                uiState.isBoardFlipped // в усіх інших випадках (dual, bot, або перегляд історії) отримуємо ручний вибір користувача
            }
        }
    }

    // отримуємо синглтон плеєра
    val chessSoundPlayer = remember { AudioController.getPlayer(context) }

    // слухаємо події з viewModel і відтворюємо
    LaunchedEffect(viewModel.soundEffect) {
        viewModel.soundEffect.collect { soundType ->
            chessSoundPlayer.playSound(soundType)
        }
    }

    val settingsManager = remember { SettingsManager(context) }

    // стани для налаштувань
    var showLocalSettingsDialog by remember { mutableStateOf(false) }
    var currentTimerValue by remember {
        mutableStateOf(settingsManager.getSetting(SettingsManager.KEY_TIMER_DURATION, "0").toIntOrNull() ?: 0)
    }
    // гра почалася, якщо наявний хоча б один запис у нотації
    val isGameStarted = uiState.notationHistory.isNotEmpty()

    // діалог завершення гри
    if (uiState.outcome != null) {
        GameOverDialog(
            outcomeText = uiState.outcome!!,
            moveCount = uiState.notationHistory.size,
            onNewGame = {
                val timerDuration = settingsManager.getSetting(
                    SettingsManager.KEY_TIMER_DURATION, "0"
                ).toIntOrNull() ?: 0
                viewModel.startNewGame(timerDuration)
            },
            onExit = { navController.popBackStack() },
            onDismiss = { viewModel.dismissOutcome() }
        )
    }

    // формуємо пару координат для підсвітки
    val hintFromTo = if (uiState.hintFromSquare != null && uiState.hintToSquare != null) {
        // використовуємо !!, щоб запевнити компілятор, що значення не null
        uiState.hintFromSquare!! to uiState.hintToSquare!!
    } else {
        null
    }

    // викликаємо шаблон верстки та заповнюємо його слоти
    BoardElementsLayout(
        // слот для дошки
        board = {
            AnimatedChessBoard(
                boardState = boardUiState,
                isFlipped = isBoardFlipped, // isBoardFlipped - це локальна змінна
                animatedMoves = uiState.animatedMove?.let { listOf(it) } ?: emptyList(), // конвертуємо хід у список для анімації
                onAnimationFinished = { viewModel.clearAnimation() },
                onSquareClick = { row, col -> viewModel.handleSquareClick(row * 9 + col, gameMode) }
            )
        },

        // слоти для інформаційних панелей гравців
        topPlayerInfo = {
            PlayerInfoPanel(data = if (isBoardFlipped) bottomData else topData, isTopPanel = true, isTrainingMode = isTrainingMode)
        },
        bottomPlayerInfo = {
            PlayerInfoPanel(data = if (isBoardFlipped) topData else bottomData, isTopPanel = false, isTrainingMode = isTrainingMode)
        },

        // слот для нотації
        notation = {
            NotationView(
                notationList = uiState.notationHistory, // нотацію беремо з ViewModel
                currentMoveIndex = currentMoveIndexForView, // для зеленої підсвітки
                activeEngineMoveIndex = currentHistoryIndex, // для визначення тьмяних ходів
                onNotationClick = { index ->
                    if (index == uiState.notationHistory.size - 1 && uiState.isViewingHistory) {
                        viewModel.returnToCurrentGame()
                    } else {
                        viewModel.goToHistoryState(index)
                    }
                    // відтворюємо звук для вибраного ходу
                    viewModel.playSoundForHistoryMove(index)
                }
            )
        },

        // слот для основних кнопок
        gameControls = {
            val isTimerGame = (uiState.whiteTimeRemainingMs ?: -1L) >= 0L || (uiState.blackTimeRemainingMs ?: -1L) >= 0L
            val allowTakeback = settings.allowUndo && !isTimerGame

            GameControlsPanel(
                isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
                gameMode = gameMode,
                autoFlipEnabled = uiState.autoFlipEnabled, // в режимі "hotseat" ввімкнено
                onFlipBoard = {
                    // викликаємо метод ViewModel залежно від режиму
                    if (gameMode == "hotseat") {
                        viewModel.toggleAutoFlip()
                    } else {
                        viewModel.flipBoard()
                    }
                },
                onUndo = {
                    if (allowTakeback) {
                        viewModel.undoMove()
                    } else {
                        // навігація назад
                        if (currentMoveIndexForView >= 0) {
                            val prevIndex = currentMoveIndexForView - 1
                            viewModel.goToHistoryState(prevIndex)
                            viewModel.playSoundForHistoryMove(prevIndex)
                        }
                    }
                },
                onRedo = {
                    if (allowTakeback) {
                        viewModel.redoMove()
                    } else {
                        // навігація вперед
                        val nextIndex = currentMoveIndexForView + 1
                        if (nextIndex == uiState.notationHistory.size - 1) {
                            viewModel.returnToCurrentGame()
                        } else if (nextIndex < uiState.notationHistory.size - 1) {
                            viewModel.goToHistoryState(nextIndex)
                        }
                        if (nextIndex < uiState.notationHistory.size) {
                            viewModel.playSoundForHistoryMove(nextIndex)
                        }
                    }
                },
                onResign = { showResignDialog = true },
                onDrawOffer = { showDrawDialog = true },
                allowTakeback = allowTakeback, // прапорець з налаштувань
                isUndoEnabled = if (allowTakeback) currentHistoryIndex >= 0 else currentMoveIndexForView >= 0,
                isRedoEnabled = if (allowTakeback) currentHistoryIndex < historyTotalSize - 1 else currentMoveIndexForView < uiState.notationHistory.size - 1, // redo активна, якщо ми не в кінці історії
                onSetupGame = { showLocalSettingsDialog = true }
            )
        },

        // опційний слот для додаткових кнопок
        optionalTopControls = {
            if (isTrainingMode) {
                TrainingControlsPanel(
                    onHintClick = { viewModel.getBestMoveHint() },
                    onAttackMapClick = { viewModel.cycleAttackMapDisplay() }, // викликаємо функцію циклічного відображення мапи з ViewModel
                    onShowThreats = { null }, // викликаємо функцію з ViewModel, лише якщо клітинка вибрана
                    isShowingAttackers = uiState.isShowingAttackers // передаємо стан для підсвітки кнопки
                )
            }
        }
    )
    IconButton(onClick = { navController.popBackStack() },
        modifier = Modifier.padding(top = OrientationPaddings.topPadding, start = 8.dp),
    ) {
        Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
    }
    
    // виклик діалогу налаштувань у грі
    LocalGameSettingsDialog(
        showDialog = showLocalSettingsDialog,
        currentTimer = currentTimerValue,
        isGameStarted = isGameStarted,
        onDismiss = { showLocalSettingsDialog = false },
        onTimerSelected = { newTimer ->
            currentTimerValue = newTimer
            settingsManager.saveSetting(SettingsManager.KEY_TIMER_DURATION, newTimer.toString())

            // якщо гра ще не почалася, застосовуємо таймер негайно
            if (!isGameStarted) {
                viewModel.startNewGame(newTimer)
            }
        }
    )

    // отримуємо результат в локальну змінну, щоб уникнути проблем зі smart cast
    val currentOutcome = uiState.outcome
    if (currentOutcome != null) {
        GameOverDialog(
            outcomeText = currentOutcome,
            moveCount = uiState.notationHistory.size,
            onNewGame = { viewModel.startNewGame(0) },
            onExit = { navController.popBackStack() }, // на цьому екрані вихід дозволено
            onDismiss = { viewModel.dismissOutcome() }
        )
    }

    // діалог здачі
    if (showResignDialog) {
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            title = { Text("Здатися?", color = Color.White) },
            text = { Text("Ви певні, що хочете визнати поразку?", color = Color.White) },
            containerColor = backgroundColor,
            confirmButton = {
                Button(onClick = {
                    viewModel.resign() // викликаємо функцію ViewModel
                    showResignDialog = false
                },
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) { Text("Так") }
            },
            dismissButton = {
                Button(onClick = { showResignDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)) { Text("Ні") }
            }
        )
    }

    if (showDrawDialog) {
        AlertDialog(
            onDismissRequest = { showDrawDialog = false },
            title = { Text("Запропонувати нічию?", color = Color.White) },
            text = { Text("Інший гравець має погодитись.", color = Color.White) },
            containerColor = backgroundColor,
            confirmButton = {
                Button(onClick = {
                    viewModel.offerDraw() // викликаємо функцію ViewModel
                    showDrawDialog = false
                },
                    colors = ButtonDefaults.buttonColors(containerColor = goldButtonColor)
                ) { Text("Запропонувати") }
            },
            dismissButton = {
                Button(onClick = { showDrawDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor)) { Text("Скасувати") }
            }
        )
    }

    if (uiState.promotionCandidate != null) { // перевіряємо стан з ViewModel
        PromotionDialog(
            playerColor = if (jniGameState.sideToMove == 0) PlayerColor.WHITE else PlayerColor.BLACK,
            onPieceSelected = { pieceType ->
//                val (from, to) = promotionCandidate!!
//                val flag = promotionPieceTypeToFlag(pieceType)
//                viewModel.makeMove(from, to, flag)
//                promotionCandidate = null
//                selectedSquare = null
//                legalMoves = emptyList()
                // Викликаємо метод з ViewModel для завершення промоції
                viewModel.resolvePromotion(pieceType)
            }
        )
    }
} // закриваємо LocalGameScreen

// допоміжна функція для ChessBoard. reusable компонент для дошки
@Composable
private fun ChessBoardWrapper(
    viewModel: ChessViewModel,
    gameMode: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val boardUiState = viewModel.boardUiState

    ChessBoard(
        modifier = modifier.aspectRatio(1f),
        boardState = boardUiState,
        isFlipped = uiState.isBoardFlipped,
        onSquareClick = { row, col -> viewModel.handleSquareClick(row * 9 + col, gameMode) }
    )
}

@Composable
fun DualGameScreen(navController: NavController, gameMode: String) {
    val viewModel: ChessViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val boardUiState = viewModel.boardUiState

    val gameState by viewModel.gameState.collectAsState()
    // ...отримання стану з viewModel як у LocalGameScreen...

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // визначаємо, чи перевернута дошка, на основі режиму гри
    val isBoardFlipped = false // У режимі "dual" дошка не перевертається автоматично
    // визначаємо активного гравця для повороту таймерів
    val isWhitesTurn = gameState.sideToMove == 0

    if (isLandscape) {
        // альбомний режим - унікальна верстка
//        DualGameLandscapeLayout(...)
    } else {
        // портретний режим - адаптація LocalGameScreen
        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor)
        ) {
            // верхня панель - повернута для гравця чорними
            PlayerInfoPanel(
                modifier = Modifier.fillMaxWidth(),
                data = if (isWhitesTurn) viewModel.bottomPlayerDisplayData.collectAsState().value
                else viewModel.topPlayerDisplayData.collectAsState().value,
                isTopPanel = true,
                isTrainingMode = false
            )

            // шахова дошка
            ChessBoardWrapper(
                viewModel = viewModel,
                gameMode = gameMode,
                modifier = Modifier.weight(1f)
            )

            // нижня панель - для гравця білими
            PlayerInfoPanel(
                modifier = Modifier.fillMaxWidth(),
                data = if (isWhitesTurn) viewModel.topPlayerDisplayData.collectAsState().value
                else viewModel.bottomPlayerDisplayData.collectAsState().value,
                isTopPanel = false,
                isTrainingMode = false
            )

//            // кнопки управління
//            GameControlsPanel(...)
        }
    }
    // унікальна верстка для двох гравців навпроти
//    Column(Modifier.fillMaxSize().background(backgroundColor)) {
//        PlayerInfoPanel(...) // верхня панель, повернута на 180 градусів
//        //.rotate(180f)
//        ChessBoard(...)
//        PlayerInfoPanel(...) // нижня панель
//        GameControlsPanel(...)
//    }
    
    // ... спливаючі меню, діалоги і т.п. ...
}

class ReviewViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            val settingsManager = SettingsManager(context)
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ReviewViewModel(private val settingsManager: SettingsManager) : ViewModel() {
   
    val sessionManager = GameSessionManager(0) // ізольована сесія лише для цього екрану
    val boardUiState = mutableStateListOf<SquareUiState>() // стан для дошки

    // використовуємо EngineUiState для збереження стану
    private val _uiState = MutableStateFlow(ChessViewModel.EngineUiState(isViewingHistory = true))
    val uiState: StateFlow<ChessViewModel.EngineUiState> = _uiState

    var maxPly by mutableIntStateOf(-1)

    init {
        // ініціюємо дошку 81 клітинкою
        repeat(81) { index ->
            boardUiState.add(SquareUiState(index = index, squareName = "", piece = null))
        }
    }

    // логіка застосування збереженої гри
    private fun applyGameToUi(targetGame: SavedGameData) {
        maxPly = targetGame.historyMoves.size - 1 // оновлюємо все в головному потоці

        sessionManager.loadSession( // завантажуємо архівну партію через менеджер
            targetGame.historyMoves.toIntArray(),
            targetGame.whiteTimeMs,
            targetGame.blackTimeMs
        )

        _uiState.update { it.copy(
            notationHistory = targetGame.notation,
            outcome = targetGame.outcome
        )}

        goToPly(maxPly) // починаємо перегляд з фінальної позиції
    }

    fun loadGame(timestamp: Long) {
        val cachedGame = GameArchiveCache.selectedGame // виймаємо з кешу
        if (cachedGame != null && cachedGame.timestamp == timestamp) {
            applyGameToUi(cachedGame)
            return
        }

        // резервний варіант (якщо кеш порожній) парсимо текстовий рядок        
        val archiveStr = settingsManager.getSetting("games_archive", "")
        if (archiveStr.isEmpty()) return

        // шукаємо потрібний рядок як звичайний текст (без важкого парсингу)
        val targetJson = archiveStr.split("|||").find {
            it.contains("\"timestamp\":$timestamp")
        } ?: return
        
        // парсимо тільки знайдену гру
        val targetGame = try {
            Gson().fromJson(targetJson, SavedGameData::class.java)
        } catch (e: Exception) { null } ?: return

        applyGameToUi(targetGame)
    }

    fun goToPly(ply: Int) {
        val safePly = ply.coerceIn(-1, maxPly)

        // отримуємо стан на хід з C++
        val historicState = sessionManager.getGameState(safePly)

        // хід з історії для підсвітки (якщо це не початкова позиція)
        val historyMoves = sessionManager.getHistoryMoves()
        val historicalLastMove = if (safePly >= 0 && safePly < historyMoves.size) {
            val encodedMove = historyMoves[safePly]
            Triple(encodedMove shr 16, (encodedMove shr 8) and 0xFF, encodedMove and 0xFF)
        } else null

        // оновлюємо загальний стан
        _uiState.update {
            it.copy(
                currentHistoryViewIndex = safePly,
                lastMove = historicalLastMove,
                checkedKingSquare = sessionManager.findCheckedKing(historicState)
            )
        }

        val isFlipped = _uiState.value.isBoardFlipped
        val cellNameMode = CellNameMode.valueOf(settingsManager.getSetting(SettingsManager.KEY_SHOW_CELL_NAMES, CellNameMode.ALL.name))

        // крапково оновлюємо візуальну дошку одним снепшотом
        Snapshot.withMutableSnapshot {
            for (index in 0..80) {
                val squareName = when (cellNameMode) {
                    CellNameMode.HIDDEN -> ""
                    CellNameMode.ALL -> squareNamesAll[index]
                    CellNameMode.EDGES -> if (isFlipped) squareNamesEdgesForBlack[index] else squareNamesEdges[index]
                }

                boardUiState[index] = SquareUiState(
                    index = index,
                    squareName = squareName,
                    piece = ChessPiece.fromCode(historicState.mailbox[index]),
                    isLastMove = historicalLastMove?.first == index || historicalLastMove?.second == index,
                    isChecked = _uiState.value.checkedKingSquare == index
                )
            }
        }
    }
    
    // потік для озвучення подій
    private val _soundEffect = MutableSharedFlow<ChessSoundPlayer.SoundType>()
    val soundEffect = _soundEffect.asSharedFlow()

    // функція викликається з UI при перемиканні ходу
    fun playSoundForPly(ply: Int) {
        val soundType = sessionManager.getSoundTypeForPly(ply) // отримуємо тип звуку з менеджера
        
        soundType?.let { // тип звуку знайдено, відправляємо його в UI
            viewModelScope.launch {
                _soundEffect.emit(it)
            }
        }
    }

    fun flipBoard() {
        _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
        goToPly(_uiState.value.currentHistoryViewIndex) // примусово перемальовуємо дошку
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.destroy() // безпечно звільняємо пам'ять C++ при закритті екрану
    }
}

@Composable
fun ReviewScreen(
    timestamp: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ReviewViewModel = viewModel(factory = ReviewViewModelFactory(context))

    val uiState by viewModel.uiState.collectAsState()
    val boardUiState = viewModel.boardUiState

    // отримуємо синглтон плеєра. remember гарантує, що ми не створюватимемо його заново при кожному перемалюванні екрану
    val chessSoundPlayer = remember { AudioController.getPlayer(context) }

    // підписуємося на події звуку // LaunchedEffect запускає корутину, що живе, поки цей екран відкритий
    LaunchedEffect(viewModel.soundEffect) {
        viewModel.soundEffect.collect { soundType ->
            chessSoundPlayer.playSound(soundType) // відтворюємо звук, коли він приходить з ViewModel
        }
    }

    // завантажуємо партію при відкритті екрану
    LaunchedEffect(timestamp) {
        viewModel.loadGame(timestamp)
    }

    val currentPly = uiState.currentHistoryViewIndex
    
    BoardElementsLayout( // використовуємо шаблон верстки
        board = {
            AnimatedChessBoard(
                boardState = boardUiState,
                isFlipped = uiState.isBoardFlipped,
                animatedMoves = emptyList(), // без анімації при перегляді
                onAnimationFinished = { },
                onSquareClick = { _, _ -> /* тільки перегляд, ігноруємо кліки по фігурах */ }
            )
        },

        // інфопанелі (залишаємо порожніми або виводимо)
        topPlayerInfo = { PlayerInfoPanel(data = PlayerDisplayData(), isTopPanel = true) },
        bottomPlayerInfo = { PlayerInfoPanel(data = PlayerDisplayData(), isTopPanel = false) },
        
        notation = {
            NotationView(
                notationList = uiState.notationHistory,
                currentMoveIndex = currentPly,
                activeEngineMoveIndex = viewModel.maxPly, // всі ходи "вже зроблені", нічого не буде тьмяним
                onNotationClick = { index ->
                    viewModel.goToPly(index)
                    viewModel.playSoundForPly(index) // отримуємо потрібний звук з ViewModel
                }
            )
        },
        
        gameControls = {
            GameControlsPanel(
                isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
                gameMode = "review",
                autoFlipEnabled = false,
                onFlipBoard = { viewModel.flipBoard() },
                onUndo = { // крок назад
                    val prevPly = currentPly - 1
                    viewModel.goToPly(prevPly)
                    viewModel.playSoundForPly(prevPly)
                },
                onRedo = { // повтор історичного ходу
                    val nextPly = currentPly + 1
                    viewModel.goToPly(nextPly)
                    viewModel.playSoundForPly(nextPly)
                },
                onResign = { navController.popBackStack() },    // кнопка Здатися для виходу з перегляду
                onDrawOffer = { },                              // деактивовано
                isUndoEnabled = currentPly >= 0,
                isRedoEnabled = currentPly < viewModel.maxPly,
                allowTakeback = false, // false автоматично замінить іконки Undo/Redo на стрілочки < та >
                onSetupGame = { }                               // деактивовано
            )
        }
    )
}

// допоміжна функція для конвертації Kotlin-типу в C++ індекс
fun PieceType.toJniValue(): Int {
    return when(this) {
        PieceType.PAWN -> 0
        PieceType.KNIGHT -> 1
        PieceType.BISHOP -> 2
        PieceType.GUARD -> 3
        PieceType.ROOK -> 4
        PieceType.QUEEN -> 5
        PieceType.KING -> 6
    }
}
fun PlayerColor.toJniValue(): Int {
    return when(this) {
        // використовуємо числові константи, як в c++
        PlayerColor.WHITE -> 0
        PlayerColor.BLACK -> 1
    }
}

@Composable
fun RowScope.PieceButton(piece: ChessPiece, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = buttonStyleColor),
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.White else Color.Transparent
            ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = pieceToDrawableResource(piece)),
            contentDescription = piece.type.name,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}

@Composable
fun PieceSelectionPanel(
    modifier: Modifier = Modifier,
    activePiece: ChessPiece?,
    onPieceSelected: (ChessPiece) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ряд для чорних фігур
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            PieceType.entries.forEach { pieceType ->
                val currentPiece = ChessPiece(pieceType, PlayerColor.BLACK)
                PieceButton(
                    piece = currentPiece,
                    isSelected = activePiece == currentPiece,
                    onClick = { onPieceSelected(currentPiece) }
                )
            }
        }
        // ряд для білих фігур
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            PieceType.entries.forEach { pieceType ->
                val currentPiece = ChessPiece(pieceType, PlayerColor.WHITE)
                PieceButton(
                    piece = currentPiece,
                    isSelected = activePiece == currentPiece,
                    onClick = { onPieceSelected(currentPiece) }
                )
            }
        }
    }
}

@Composable
fun TrainingGameScreen(navController: NavController) {
    val context = LocalContext.current
    // отримуємо viewModel та її стан
    val viewModel: ChessViewModel = viewModel(factory = ChessViewModelFactory(context.applicationContext))
    val uiState by viewModel.uiState.collectAsState()
    // стан для ChessBoard
    val boardState = viewModel.boardUiState

    // входимо в режим самоаналізу при першому запуску екрану
    LaunchedEffect(Unit) {
        viewModel.enterTrainingGameMode()
    }
    // отримуємо синглтон плеєра
    val chessSoundPlayer = remember { AudioController.getPlayer(context) }

    // слухаємо події з viewModel і відтворюємо
    LaunchedEffect(viewModel.soundEffect) {
        viewModel.soundEffect.collect { soundType ->
            chessSoundPlayer.playSound(soundType)
        }
    }
    // кешуємо бітборди. оновлюватимемо їх лише при зміні розставлених фігур
    val pieceBitboards by remember(uiState.placedPieces) {
        mutableStateOf(
            Array(14) { LongArray(2) }.also { boards ->
                uiState.placedPieces.forEach { (square, piece) ->
                    val pieceJniIndex = piece.type.toJniValue() + piece.color.toJniValue() * 7
                    setBit(boards[pieceJniIndex], square)
                }
            }
        )
    }

//    // стан для фігур, розставлених на дошці. Map<SquareIndex, ChessPiece>
//    var placedPieces by remember { mutableStateOf<Map<Int, ChessPiece>>(emptyMap()) }
//    // стан для фігури, яку вибрали кнопкою, але ще не поставили на дошку.
//    var activePieceToPlace by remember { mutableStateOf<ChessPiece?>(null) }
//    // клітинка, вибрана для показу ходів (після того, як фігури розставлені).
//    var selectedSquare by remember { mutableStateOf<Int?>(null) }
//    var legalMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
//    // для вибору кольору
//    var sideToMove by remember { mutableIntStateOf(0) }

//
//    // логіка з C++ рушія для змін позиції або вибору клітинки для показу ходів.
//    LaunchedEffect(placedPieces, selectedSquare) {
//        val square = selectedSquare
//        val pieceToInspect = square?.let { placedPieces[it] }
//
//        // у режимі аналізу (humanPlayerColor == -1) можна вибрати будь-яку фігуру.
//        val canSelectPiece = jniGameState.sideToMove == pieceColor
//        val canSelectPiece = pieceColor != null // дозволяємо вибір будь-якої фігури на дошці
//
//        // якщо клітинка для показу ходів не вибрана або на ній немає фігури - очищуємо підсвітку і виходимо.
//        if (square == null || pieceToInspect == null) {
//            legalMoves = emptyList()
//            return@LaunchedEffect
//        }
//
//        // створюємо "штучний" стан гри на основі розставлених фігур
//        val pieceBitboards = Array(14) { LongArray(2) } // 14 порожніх бітбордів
//        placedPieces.forEach { (s, p) ->
//            // обчислюємо індекс фігури для c++ рушія
//            val pieceJniIndex = p.type.toJniValue() + p.color.toJniValue() * 7
//            setBit(pieceBitboards[pieceJniIndex], s)
//        }
//
//        // формуємо список координат для підсвітки легальних ходів
//        legalMoves = allMovesEncoded
//            .filter { encodedMove -> (encodedMove shr 16) == square }
//            .map { encodedMove ->
//                val toSquare = (encodedMove shr 8) and 0xFF
//                toSquare / 9 to toSquare % 9 // конвертуємо індекс в (рядок, стовпець)
//            }
//    }
//
//    // створюємо стан дошки для UI: порожня дошка + розставлені фігури, оновлюється при зміні placedPieces
//    val boardStateForUI = remember(placedPieces) {
//        val board = Array(9) { Array<ChessPiece?>(9) { null } }
//        placedPieces.forEach { (square, piece) ->
//            board[square / 9][square % 9] = piece
//        }
//        board
//    }
//
//    // обробник кліків по дошці
//    fun handleSquareClick(row: Int, col: Int) {
//        val clickedSquare = row * 9 + col
//        val pieceToPlace = activePieceToPlace
//        val pieceAlreadyOnSquare = placedPieces[clickedSquare]
//
//        if (pieceToPlace != null) {
//            // режим конструктора: ставимо фігуру, що "в руці", на дошку
//            placedPieces = placedPieces + (clickedSquare to pieceToPlace)
//            activePieceToPlace = null // звільняємо "руку"
//            selectedSquare = null // скидаємо аналіз
//        } else {
//            // режим аналізу та переміщення
//            if (pieceAlreadyOnSquare != null) {
//                // клікнули на фігуру
//                if (selectedSquare == clickedSquare) {
//                    // якщо клікнули на вже вибрану фігуру, "беремо її в руку" для переміщення
//                    activePieceToPlace = pieceAlreadyOnSquare
//                    placedPieces = placedPieces - clickedSquare
//                    selectedSquare = null
//                } else {
//                    // якщо клікнули на нову фігуру, вибираємо її для аналізу ходів
//                    selectedSquare = clickedSquare
//                    sideToMove = pieceAlreadyOnSquare.color.toJniValue()
//                }
//            } else {
//                // клікнули на порожню клітинку, знімаємо будь-яке виділення
//                selectedSquare = null
//            }
//        }
//    }

/*    // відображення
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // кнопка назад у вигляді стандартної стрілки
        IconButton(onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = OrientationPaddings.topPadding, start = 8.dp).align(Alignment.Start),
            ) {
            Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
        }

        // панель вибору фігур
        PieceSelectionPanel(
            modifier = Modifier.padding(vertical = 8.dp),
            activePiece = activePieceToPlace,
            onPieceSelected = { selectedPiece ->
                activePieceToPlace = selectedPiece
            }
        )

        ChessBoard(
            boardState = boardState,
            isFlipped = false,
            onSquareClick = ::handleSquareClick
        )
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                // викликаємо ту саму централізовану функцію, але передаємо їй кастомні бітборди з екрану
                viewModel.cycleAttackMapDisplay(customBitboards = pieceBitboards)
            }) {
                Text("⚔️")
            }
            Button(
                onClick = {
                    val square = selectedSquare
                    if (square != null) {
                        placedPieces = placedPieces - square
                        selectedSquare = null
                    }
                },
                enabled = selectedSquare != null // кнопка активна, лише коли вибрано фігуру
            ) {
                Text("🗑️")
            }
            Button(onClick = {
                placedPieces = emptyMap()
                selectedSquare = null
                activePieceToPlace = null
            }) {
                Text("♻️")
            }

        }
    }
}*/
// використовуємо наш новий шаблон для верстки
    BoardElementsLayout(
        // слот для дошки, тут все стандартно
        board = {
            AnimatedChessBoard(
                boardState = boardState, // використовуємо новий boardUiState
                isFlipped = false, // в режимі самоаналізу дошка не перевертається
                animatedMoves = uiState.animatedMove?.let { listOf(it) } ?: emptyList(), // конвертуємо хід у список для анімації
                onAnimationFinished = { viewModel.clearAnimation() },
                onSquareClick = { row, col ->
                    viewModel.handleTrainingGameSquareClick(row * 9 + col)
                }
            )
        },
        // інформаційні панелі нам знадобляться пізніше, поки залишаємо слоти порожніми
        topPlayerInfo = {},
        bottomPlayerInfo = {},
        // панель нотації також
        notation = {},
        // а в цей слот кладемо панель вибору фігур, унікальну для цього екрану
        optionalTopControls = {
            PieceSelectionPanel(
                modifier = Modifier.padding(vertical = 8.dp),
                activePiece = uiState.activePieceToPlace,
                onPieceSelected = { selectedPiece ->
                    viewModel.setActivePieceToPlace(selectedPiece)
                }
            )
        },
        // у слот для основних кнопок ми передаємо панель керування режимом самоаналізу
        gameControls = {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // кнопка показу мапи атак
                Button(onClick = {
                    viewModel.cycleAttackMapDisplay(customBitboards = pieceBitboards)
                }) { Text("⚔️", fontSize = 24.sp) }

                Button(onClick = { viewModel.toggleAttackMap(PlayerColor.WHITE) }) { Text("⚔️ ⬜") }
                Button(onClick = { viewModel.toggleAttackMap(PlayerColor.BLACK) }) { Text("⚔️ ⬛") }

                // кнопка підказки
                // ВАЖЛИВО: логіка підказки потребує сесії в C++
                // поточна функція getBestMoveHint() працює з внутрішнім станом рушія, а не з позицією, що її ми конструюємо тут.
                Button(onClick = { /* TODO: логіка підказки для позиції */ }) {
                    Text("💡", fontSize = 24.sp)
                }

                // кнопка видалення фігури з вибраної клітинки
                Button(
                    onClick = { viewModel.removePieceFromSelectedSquare() },
                    enabled = uiState.selectedSquare != null
                ) { Text("🗑️", fontSize = 24.sp) }

                // кнопка повного очищення дошки
                Button(onClick = { viewModel.clearTrainingGameBoard() }) {
                    Text("♻️", fontSize = 24.sp)
                }
            }
        }
    )

    // кнопка "назад" поза основною версткою, щоб бути завжди в одному місці
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = OrientationPaddings.topPadding, start = 8.dp),
        ) {
            Icon(painterResource(id = R.drawable.back), contentDescription = "Назад", tint = Color.White)
        }
    }
}