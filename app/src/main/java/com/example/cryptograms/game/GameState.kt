package com.example.cryptograms.game

/**
 * Immutable state of one game. All rules live here so they can be unit-tested
 * without Android.
 */
data class GameState(
    val puzzle: Puzzle,
    /** cipher letter -> the player's guess */
    val guesses: Map<Char, Char>,
    /** Cipher letters that can't be changed (revealed at start or by a hint). */
    val locked: Set<Char>,
    /** Index in [Puzzle.cipherText] of the selected tile. */
    val selectedIndex: Int? = null,
    val mistakes: Int = 0,
    val hintsLeft: Int = puzzle.difficulty.hints,
    val hintsUsed: Int = 0,
    val elapsedSeconds: Long = 0,
    val paused: Boolean = false,
    /** True when the player chose "show solution" — not counted as a win. */
    val gaveUp: Boolean = false,
    val history: List<Map<Char, Char>> = emptyList(),
) {
    val selectedCipher: Char? get() = selectedIndex?.let { puzzle.cipherText[it] }

    val isSolved: Boolean
        get() = puzzle.cipherLetters.all { guesses[it] == puzzle.decrypt[it] }

    val solvedLetters: Int
        get() = puzzle.cipherLetters.count { guesses[it] == puzzle.decrypt[it] }

    fun isCorrect(cipher: Char): Boolean = guesses[cipher]?.let { it == puzzle.decrypt[cipher] } ?: false

    fun isWrong(cipher: Char): Boolean = guesses[cipher]?.let { it != puzzle.decrypt[cipher] } ?: false

    /** Plain letters currently in use, used to dim keyboard keys. */
    val usedLetters: Set<Char> get() = guesses.values.toSet()

    fun select(index: Int): GameState {
        if (isSolved || paused) return this
        val c = puzzle.cipherText.getOrNull(index) ?: return this
        if (c !in puzzle.decrypt) return this
        return copy(selectedIndex = index)
    }

    /** Assigns [letter] to the selected cipher letter. */
    fun guess(letter: Char, autoAdvance: Boolean): GameState {
        val cipher = selectedCipher ?: return this
        if (isSolved || paused || cipher in locked) return this
        val plain = letter.uppercaseChar()
        if (plain !in puzzle.language.alphabet) return this
        if (guesses[cipher] == plain) {
            return if (autoAdvance) advance() else this
        }
        // A letter already locked elsewhere can't be reused.
        val owner = guesses.entries.firstOrNull { it.value == plain }?.key
        if (owner != null && owner in locked) return this

        val newGuesses = guesses.toMutableMap().apply {
            if (owner != null) remove(owner) // each plain letter is used only once
            put(cipher, plain)
        }
        val wrong = puzzle.decrypt[cipher] != plain
        val next = copy(
            guesses = newGuesses,
            mistakes = mistakes + if (wrong) 1 else 0,
            history = history + listOf(guesses),
        )
        return when {
            next.isSolved -> next.copy(selectedIndex = null)
            autoAdvance -> next.advance()
            else -> next
        }
    }

    /** Clears the guess for the selected cipher letter. */
    fun erase(): GameState {
        val cipher = selectedCipher ?: return this
        if (isSolved || paused || cipher in locked || cipher !in guesses) return this
        return copy(guesses = guesses - cipher, history = history + listOf(guesses))
    }

    /** Reveals the selected cipher letter, or the first unsolved one. */
    fun hint(autoAdvance: Boolean): GameState {
        if (isSolved || paused || hintsLeft <= 0) return this
        val selected = selectedCipher
        val target = if (selected != null && !isCorrect(selected)) {
            selected
        } else {
            puzzle.cipherLetters.firstOrNull { !isCorrect(it) } ?: return this
        }
        val plain = puzzle.decrypt.getValue(target)
        val newGuesses = guesses.filterValues { it != plain } + (target to plain)
        val targetIndex = if (selected == target) selectedIndex else puzzle.cipherText.indexOf(target)
        val next = copy(
            guesses = newGuesses,
            locked = locked + target,
            hintsLeft = hintsLeft - 1,
            hintsUsed = hintsUsed + 1,
            selectedIndex = targetIndex,
        )
        return when {
            next.isSolved -> next.copy(selectedIndex = null)
            autoAdvance -> next.advance()
            else -> next
        }
    }

    fun undo(): GameState {
        if (isSolved || paused || history.isEmpty()) return this
        val previous = history.last()
        // Hints are permanent: keep locked letters even when undoing.
        val lockedGuesses = guesses.filterKeys { it in locked }
        val restored = previous.filter { (cipher, plain) ->
            cipher !in locked && plain !in lockedGuesses.values
        } + lockedGuesses
        return copy(guesses = restored, history = history.dropLast(1))
    }

    /** Fills in the whole solution. */
    fun revealAll(): GameState {
        if (isSolved) return this
        return copy(
            guesses = puzzle.decrypt,
            locked = puzzle.cipherLetters.toSet(),
            selectedIndex = null,
            paused = false,
            gaveUp = true,
        )
    }

    /** Moves the selection to the next tile without a guess, in reading order. */
    fun advance(): GameState {
        val text = puzzle.cipherText
        val start = selectedIndex ?: -1
        for (step in 1..text.length) {
            val i = (start + step).mod(text.length)
            val c = text[i]
            if (c in puzzle.decrypt && c !in guesses) return copy(selectedIndex = i)
        }
        return this
    }

    companion object {
        fun new(puzzle: Puzzle): GameState {
            val guesses = puzzle.revealed.associateWith { puzzle.decrypt.getValue(it) }
            return GameState(puzzle = puzzle, guesses = guesses, locked = puzzle.revealed).advance()
        }
    }
}
