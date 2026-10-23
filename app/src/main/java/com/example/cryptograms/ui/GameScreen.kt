package com.example.cryptograms.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.cryptograms.R
import com.example.cryptograms.data.DifficultyStats
import com.example.cryptograms.data.Settings
import com.example.cryptograms.game.Difficulty
import com.example.cryptograms.game.GameState
import com.example.cryptograms.ui.theme.Palette
import com.example.cryptograms.ui.theme.SerifFamily
import kotlinx.coroutines.flow.Flow

@Composable
fun GameScreen(
    state: GameState,
    settings: Settings,
    stats: Map<Difficulty, DifficultyStats>,
    mistakeEvents: Flow<Unit>,
    onBack: () -> Unit,
    onNewPuzzle: (Difficulty) -> Unit,
    onOpenSettings: () -> Unit,
    onSelect: (Int) -> Unit,
    onType: (Char) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onTogglePause: () -> Unit,
    onRevealSolution: () -> Unit,
) {
    var showResults by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(mistakeEvents, settings.haptics) {
        mistakeEvents.collect {
            if (settings.haptics) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }
    LaunchedEffect(state.isSolved, state.puzzle) {
        if (state.isSolved && !state.gaveUp) {
            kotlinx.coroutines.delay(700)
            showResults = true
        }
    }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
    // Short screens (small phones, split screen) get tighter spacing and keys
    // so the puzzle board keeps enough room.
    val compact = maxHeight < 760.dp
    val gap = if (compact) 10.dp else 20.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            // Hardware keyboard support (emulator, Chromebook, BT keyboards).
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.Backspace || event.key == Key.Delete -> { onErase(); true }
                    event.utf16CodePoint.toChar().isLetter() -> {
                        onType(event.utf16CodePoint.toChar()); true
                    }
                    else -> false
                }
            }
            .padding(horizontal = 16.dp),
    ) {
        TopBar(
            compact = compact,
            onBack = onBack,
            onNewPuzzle = { onNewPuzzle(state.puzzle.difficulty) },
            onOpenSettings = onOpenSettings,
        )
        StatusRow(
            difficulty = state.puzzle.difficulty,
            seconds = state.elapsedSeconds,
            showTimer = settings.showTimer,
            mistakes = state.mistakes,
            onDifficulty = onNewPuzzle,
        )
        Spacer(Modifier.height(gap))
        PuzzleCard(
            state = state,
            settings = settings,
            onSelect = onSelect,
            onResume = onTogglePause,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(gap))
        Keyboard(
            compact = compact,
            rows = state.puzzle.language.keyboardRows,
            used = state.usedLetters,
            enabled = !state.paused && !state.isSolved,
            onKey = onType,
        )
        Spacer(Modifier.height(if (compact) 6.dp else 12.dp))
        ActionBar(
            compact = compact,
            state = state,
            onPause = onTogglePause,
            onHint = onHint,
            onUndo = onUndo,
            onErase = onErase,
            onResults = { showResults = true },
        )
    }
    }

    if (showResults) {
        ResultsSheet(
            state = state,
            stats = stats[state.puzzle.difficulty] ?: DifficultyStats(),
            onDismiss = { showResults = false },
            onNewPuzzle = {
                showResults = false
                onNewPuzzle(state.puzzle.difficulty)
            },
            onRevealSolution = {
                showResults = false
                onRevealSolution()
            },
        )
    }
}

@Composable
private fun TopBar(compact: Boolean, onBack: () -> Unit, onNewPuzzle: () -> Unit, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 52.dp else 64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(Icons.Filled.ChevronLeft, stringResource(R.string.back), Modifier.size(32.dp))
        }
        Text(
            text = stringResource(R.string.title),
            fontFamily = SerifFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp,
            color = Palette.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNewPuzzle) {
            Icon(Icons.Filled.Autorenew, stringResource(R.string.new_puzzle), Modifier.size(28.dp))
        }
        IconButton(onClick = onOpenSettings, modifier = Modifier.offset(x = 8.dp)) {
            Icon(Icons.Filled.Tune, stringResource(R.string.settings), Modifier.size(28.dp))
        }
    }
}

@Composable
fun difficultyLabel(d: Difficulty): String = stringResource(
    when (d) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
    }
)

private fun difficultyColors(d: Difficulty): Pair<Color, Color> = when (d) {
    Difficulty.EASY -> Palette.GreenBg to Palette.GreenText
    Difficulty.MEDIUM -> Palette.AmberBg to Palette.Amber
    Difficulty.HARD -> Palette.RedBg to Palette.RedText
}

fun formatTime(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun StatusRow(
    difficulty: Difficulty,
    seconds: Long,
    showTimer: Boolean,
    mistakes: Int,
    onDifficulty: (Difficulty) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Box(Modifier.align(Alignment.CenterStart)) {
            val (bg, fg) = difficultyColors(difficulty)
            Pill(background = bg, onClick = { menuOpen = true }) {
                Text(difficultyLabel(difficulty), color = fg, fontSize = 17.sp)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier.background(Palette.SurfaceHigh),
            ) {
                Difficulty.entries.forEach { d ->
                    DropdownMenuItem(
                        text = { Text(difficultyLabel(d), color = difficultyColors(d).second) },
                        onClick = {
                            menuOpen = false
                            onDifficulty(d)
                        },
                    )
                }
            }
        }
        if (showTimer) {
            Text(
                text = formatTime(seconds),
                fontFamily = SerifFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                color = Palette.TextPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Pill(
            background = Palette.SurfaceHigh,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Text("$mistakes", color = Palette.TextSecondary, fontSize = 17.sp)
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.mistakes_desc),
                tint = Palette.TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun Pill(
    background: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .width(104.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .background(background)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

// ---------------------------------------------------------------------------
// Puzzle card
// ---------------------------------------------------------------------------

private data class Word(val start: Int, val text: String)

private fun splitWords(text: String): List<Word> {
    val words = mutableListOf<Word>()
    var start = 0
    text.split(' ').forEach { part ->
        if (part.isNotEmpty()) words += Word(start, part)
        start += part.length + 1
    }
    return words
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PuzzleCard(
    state: GameState,
    settings: Settings,
    onSelect: (Int) -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val puzzle = state.puzzle
    val words = remember(puzzle) { splitWords(puzzle.cipherText) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.5.dp, Palette.Border, RoundedCornerShape(26.dp))
            .background(Palette.Background.copy(alpha = 0.6f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Text(
                text = "— ${puzzle.author}",
                color = Palette.TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            if (!state.paused) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val gap = 4.dp
                    val longest = words.maxOf { it.text.length }
                    // Longer quotes get smaller tiles so more fits on screen.
                    val perRow = maxOf(
                        longest,
                        when {
                            puzzle.text.length > 70 -> 13
                            puzzle.text.length > 45 -> 12
                            else -> 10
                        },
                    )
                    val tileWidth = min(46.dp, (maxWidth - gap * (perRow - 1)) / perRow)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(tileWidth * 0.45f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        words.forEach { word ->
                            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                word.text.forEachIndexed { offset, c ->
                                    val index = word.start + offset
                                    if (c in puzzle.decrypt) {
                                        LetterTile(
                                            cipher = c,
                                            guess = state.guesses[c],
                                            width = tileWidth,
                                            selected = state.selectedIndex == index,
                                            matching = settings.highlightMatching && state.selectedCipher == c,
                                            wrong = settings.showMistakes && state.isWrong(c),
                                            correct = (settings.showMistakes || state.isSolved) && state.isCorrect(c),
                                            onClick = { onSelect(index) },
                                        )
                                    } else {
                                        Punctuation(c, tileWidth)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (state.paused) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onResume),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Palette.SurfaceHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, null, Modifier.size(40.dp), tint = Palette.TextPrimary)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.paused),
                    fontFamily = SerifFamily,
                    fontSize = 24.sp,
                    color = Palette.TextPrimary,
                )
                Text(stringResource(R.string.paused_desc), color = Palette.TextSecondary, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun LetterTile(
    cipher: Char,
    guess: Char?,
    width: Dp,
    selected: Boolean,
    matching: Boolean,
    wrong: Boolean,
    correct: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val background by animateColorAsState(
        when {
            selected -> Palette.TileSelected
            matching -> Palette.TileMatching
            else -> Color.Transparent
        },
        label = "tileBg",
    )
    val borderColor = when {
        selected -> Palette.TextPrimary
        matching -> Palette.TextSecondary
        else -> Palette.TileBorder
    }
    val density = LocalDensity.current
    val letterSize = with(density) { (width * 0.5f).toSp() }
    val cipherSize = with(density) { (width * 0.3f).toSp() }
    Box(
        modifier = Modifier
            .width(width)
            .height(width * 1.48f)
            .clip(shape)
            .background(background)
            .border(1.5.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
      Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1.1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = guess?.toString() ?: "",
                fontFamily = SerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = letterSize,
                lineHeight = letterSize,
                style = TightText,
                color = if (wrong) Palette.RedText else Palette.TextPrimary,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Palette.Border)
        )
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = cipher.toString(),
                fontSize = cipherSize,
                lineHeight = cipherSize,
                style = TightText,
                color = Palette.TextSecondary,
            )
        }
      }
        if (correct) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(width * 0.32f)
                    .background(Palette.Green, CornerTriangle)
            )
        }
    }
}

/** Text without the extra font padding, so letters fit small tiles. */
private val TightText = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

/** Right triangle filling the top-right corner of its bounds. */
private val CornerTriangle = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    close()
}

@Composable
private fun Punctuation(c: Char, tileWidth: Dp) {
    val size = with(LocalDensity.current) { (tileWidth * 0.5f).toSp() }
    Box(
        modifier = Modifier
            .width(tileWidth * 0.4f)
            .height(tileWidth * 1.48f * 0.52f),
        contentAlignment = Alignment.Center,
    ) {
        Text(c.toString(), fontFamily = SerifFamily, fontWeight = FontWeight.Bold, fontSize = size, color = Palette.TextPrimary)
    }
}

// ---------------------------------------------------------------------------
// Keyboard
// ---------------------------------------------------------------------------

@Composable
private fun Keyboard(
    compact: Boolean,
    rows: List<String>,
    used: Set<Char>,
    enabled: Boolean,
    onKey: (Char) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Palette.Surface)
            .padding(horizontal = 8.dp, vertical = if (compact) 8.dp else 10.dp),
    ) {
        val gap = 5.dp
        val keyWidth = (maxWidth - gap * 9) / 10
        // The Polish layout has an extra row, so keys get a bit shorter.
        val keyHeight = when {
            compact && rows.size > 3 -> 38.dp
            compact -> 44.dp
            rows.size > 3 -> 46.dp
            else -> 52.dp
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { letter ->
                        val dim = letter in used || !enabled
                        Box(
                            modifier = Modifier
                                .width(keyWidth)
                                .height(keyHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Palette.Key)
                                .clickable(enabled = enabled) { onKey(letter) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = letter.toString(),
                                fontSize = 21.sp,
                                color = if (dim) Palette.TextMuted else Palette.TextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom action bar
// ---------------------------------------------------------------------------

@Composable
private fun ActionBar(
    compact: Boolean,
    state: GameState,
    onPause: () -> Unit,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onResults: () -> Unit,
) {
    val playing = !state.isSolved && !state.paused
    val selected = state.selectedCipher
    Box(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, Palette.Border, RoundedCornerShape(22.dp))
                .padding(top = if (compact) 6.dp else 10.dp, bottom = 24.dp),
        ) {
            Action(
                icon = if (state.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                label = stringResource(if (state.paused) R.string.resume else R.string.pause),
                enabled = !state.isSolved,
                onClick = onPause,
            )
            Action(
                icon = Icons.Outlined.Lightbulb,
                label = stringResource(R.string.hint),
                enabled = playing && state.hintsLeft > 0,
                badge = state.hintsLeft,
                onClick = onHint,
            )
            Action(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = stringResource(R.string.undo),
                enabled = playing && state.history.isNotEmpty(),
                onClick = onUndo,
            )
            Action(
                icon = Icons.Outlined.CleaningServices,
                label = stringResource(R.string.erase),
                enabled = playing && selected != null && selected in state.guesses && selected !in state.locked,
                onClick = onErase,
            )
        }
        // "Results ^" tab sitting on the bottom edge.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(Palette.SurfaceHigh)
                .clickable(onClick = onResults)
                .padding(horizontal = 26.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.results), color = Palette.TextPrimary, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.KeyboardArrowUp, null, tint = Palette.TextPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun RowScope.Action(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    badge: Int? = null,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp)
            .alpha(if (enabled) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Icon(icon, null, tint = Palette.TextSecondary, modifier = Modifier.size(28.dp))
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-8).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Palette.TextMuted),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$badge", fontSize = 11.sp, color = Palette.TextPrimary)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 14.sp, color = Palette.TextSecondary, maxLines = 1)
    }
}
