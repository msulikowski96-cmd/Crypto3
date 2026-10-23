package com.example.cryptograms.data

import android.content.Context
import com.example.cryptograms.game.Difficulty
import com.example.cryptograms.game.Language
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Settings(
    val showMistakes: Boolean = true,
    val highlightMatching: Boolean = true,
    val autoAdvance: Boolean = true,
    val showTimer: Boolean = true,
    val haptics: Boolean = true,
    val difficulty: Difficulty = Difficulty.EASY,
    val tutorialSeen: Boolean = false,
    val language: Language = defaultLanguage(),
)

/** Polish quotes for phones set to Polish, English otherwise. */
fun defaultLanguage(): Language =
    if (Locale.getDefault().language == "pl") Language.POLISH else Language.ENGLISH

data class DifficultyStats(val solved: Int = 0, val bestSeconds: Long? = null)

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("cryptograms", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<Map<Difficulty, DifficultyStats>> = _stats.asStateFlow()

    fun update(transform: (Settings) -> Settings) {
        val s = transform(_settings.value)
        _settings.value = s
        prefs.edit()
            .putBoolean(KEY_SHOW_MISTAKES, s.showMistakes)
            .putBoolean(KEY_HIGHLIGHT, s.highlightMatching)
            .putBoolean(KEY_AUTO_ADVANCE, s.autoAdvance)
            .putBoolean(KEY_SHOW_TIMER, s.showTimer)
            .putBoolean(KEY_HAPTICS, s.haptics)
            .putString(KEY_DIFFICULTY, s.difficulty.name)
            .putBoolean(KEY_TUTORIAL, s.tutorialSeen)
            .putString(KEY_LANGUAGE, s.language.name)
            .apply()
    }

    fun recordWin(difficulty: Difficulty, seconds: Long) {
        val old = _stats.value[difficulty] ?: DifficultyStats()
        val new = DifficultyStats(
            solved = old.solved + 1,
            bestSeconds = old.bestSeconds?.coerceAtMost(seconds) ?: seconds,
        )
        _stats.value = _stats.value + (difficulty to new)
        prefs.edit()
            .putInt(solvedKey(difficulty), new.solved)
            .putLong(bestKey(difficulty), new.bestSeconds ?: -1)
            .apply()
    }

    fun resetStats() {
        val editor = prefs.edit()
        Difficulty.entries.forEach {
            editor.remove(solvedKey(it)).remove(bestKey(it))
        }
        editor.apply()
        _stats.value = loadStats()
    }

    private fun load() = Settings(
        showMistakes = prefs.getBoolean(KEY_SHOW_MISTAKES, true),
        highlightMatching = prefs.getBoolean(KEY_HIGHLIGHT, true),
        autoAdvance = prefs.getBoolean(KEY_AUTO_ADVANCE, true),
        showTimer = prefs.getBoolean(KEY_SHOW_TIMER, true),
        haptics = prefs.getBoolean(KEY_HAPTICS, true),
        difficulty = prefs.getString(KEY_DIFFICULTY, null)
            ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
            ?: Difficulty.EASY,
        tutorialSeen = prefs.getBoolean(KEY_TUTORIAL, false),
        language = prefs.getString(KEY_LANGUAGE, null)
            ?.let { name -> Language.entries.firstOrNull { it.name == name } }
            ?: defaultLanguage(),
    )

    private fun loadStats() = Difficulty.entries.associateWith {
        DifficultyStats(
            solved = prefs.getInt(solvedKey(it), 0),
            bestSeconds = prefs.getLong(bestKey(it), -1).takeIf { s -> s >= 0 },
        )
    }

    private fun solvedKey(d: Difficulty) = "solved_${d.name}"
    private fun bestKey(d: Difficulty) = "best_${d.name}"

    private companion object {
        const val KEY_SHOW_MISTAKES = "show_mistakes"
        const val KEY_HIGHLIGHT = "highlight_matching"
        const val KEY_AUTO_ADVANCE = "auto_advance"
        const val KEY_SHOW_TIMER = "show_timer"
        const val KEY_HAPTICS = "haptics"
        const val KEY_DIFFICULTY = "difficulty"
        const val KEY_TUTORIAL = "tutorial_seen"
        const val KEY_LANGUAGE = "quote_language"
    }
}
