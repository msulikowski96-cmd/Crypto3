package com.example.cryptograms.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptograms.data.Quotes
import com.example.cryptograms.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(app: Application) : AndroidViewModel(app) {
    val repository = SettingsRepository(app)
    val settings = repository.settings
    val stats = repository.stats

    /** Emits once per rejected/wrong guess so the UI can buzz. */
    private val _mistakeEvents = MutableStateFlow(0)
    val mistakeEvents: StateFlow<Int> = _mistakeEvents.asStateFlow()

    private var timerJob: Job? = null
    private var screenVisible = true

    private val _state = MutableStateFlow(newGame(settings.value.difficulty, settings.value.language))
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        startTimer()
    }

    private fun newGame(difficulty: Difficulty, language: Language, exclude: String? = null): GameState =
        GameState.new(Puzzle.generate(Quotes.random(difficulty, language, exclude), difficulty))

    fun newPuzzle(difficulty: Difficulty = settings.value.difficulty) {
        if (difficulty != settings.value.difficulty) {
            repository.update { it.copy(difficulty = difficulty) }
        }
        _state.value = newGame(difficulty, settings.value.language, exclude = _state.value.puzzle.text)
    }

    /** Switches the quote language and starts a new puzzle in it. */
    fun setLanguage(language: Language) {
        if (language == settings.value.language) return
        repository.update { it.copy(language = language) }
        newPuzzle()
    }

    fun select(index: Int) = _state.update { it.select(index) }

    fun type(letter: Char) {
        val before = _state.value
        val after = before.guess(letter, settings.value.autoAdvance)
        _state.value = after
        if (after.mistakes > before.mistakes) _mistakeEvents.value++
        onMaybeSolved(before, after)
    }

    fun erase() = _state.update { it.erase() }

    fun undo() = _state.update { it.undo() }

    fun hint() {
        val before = _state.value
        val after = before.hint(settings.value.autoAdvance)
        _state.value = after
        onMaybeSolved(before, after)
    }

    fun revealSolution() = _state.update { it.revealAll() }

    fun togglePause() = _state.update { if (it.isSolved) it else it.copy(paused = !it.paused) }

    /** Called from the activity lifecycle: pause the clock while the app is hidden. */
    fun setScreenVisible(visible: Boolean) {
        screenVisible = visible
        if (!visible) _state.update { if (it.isSolved) it else it.copy(paused = true) }
    }

    private fun onMaybeSolved(before: GameState, after: GameState) {
        if (!before.isSolved && after.isSolved && !after.gaveUp) {
            repository.recordWin(after.puzzle.difficulty, after.elapsedSeconds)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _state.update {
                    if (it.paused || it.isSolved || !screenVisible) it
                    else it.copy(elapsedSeconds = it.elapsedSeconds + 1)
                }
            }
        }
    }
}
