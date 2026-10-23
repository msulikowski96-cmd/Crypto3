package com.example.cryptograms.game

import com.example.cryptograms.data.Quotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameStateTest {
    private val quote = Quote("To fly, we have to have resistance.", "Maya Lin")

    private fun puzzle(difficulty: Difficulty = Difficulty.HARD, seed: Int = 1) =
        Puzzle.generate(quote, difficulty, Random(seed))

    private fun GameState.selectCipher(c: Char) = select(puzzle.cipherText.indexOf(c))

    @Test
    fun cipherIsDerangement() {
        repeat(50) { seed ->
            val map = Puzzle.derangement(Random(seed))
            assertEquals(26, map.values.toSet().size)
            assertTrue(map.none { (k, v) -> k == v })
        }
    }

    @Test
    fun punctuationAndSpacesAreKept() {
        val p = puzzle()
        assertEquals(quote.text, p.originalText)
        assertEquals(p.text.length, p.cipherText.length)
        assertEquals(',', p.cipherText[6])
        assertEquals('.', p.cipherText.last())
        assertEquals(p.text, p.cipherText.map { p.decrypt[it] ?: it }.joinToString(""))
    }

    @Test
    fun easyRevealsLettersHardRevealsNone() {
        assertTrue(GameState.new(puzzle(Difficulty.HARD)).guesses.isEmpty())
        val easy = GameState.new(puzzle(Difficulty.EASY))
        assertTrue(easy.guesses.isNotEmpty())
        easy.guesses.forEach { (c, p) -> assertEquals(easy.puzzle.decrypt[c], p) }
    }

    @Test
    fun wrongGuessCountsMistakeAndCorrectDoesNot() {
        val s = GameState.new(puzzle())
        val c = s.selectedCipher!!
        val right = s.puzzle.decrypt.getValue(c)
        val wrong = ('A'..'Z').first { it != right }
        val afterWrong = s.guess(wrong, autoAdvance = false)
        assertEquals(1, afterWrong.mistakes)
        assertTrue(afterWrong.isWrong(c))
        val afterRight = afterWrong.guess(right, autoAdvance = false)
        assertEquals(1, afterRight.mistakes)
        assertTrue(afterRight.isCorrect(c))
    }

    @Test
    fun plainLetterMovesBetweenCipherLetters() {
        val s0 = GameState.new(puzzle())
        val (a, b) = s0.puzzle.cipherLetters.take(2)
        val s = s0.selectCipher(a).guess('Q', false).selectCipher(b).guess('Q', false)
        assertNull(s.guesses[a])
        assertEquals('Q', s.guesses[b])
    }

    @Test
    fun autoAdvanceSkipsFilledTiles() {
        val s = GameState.new(puzzle())
        val first = s.selectedCipher!!
        val next = s.guess('Z', autoAdvance = true)
        assertTrue(next.selectedCipher != first)
        assertFalse(next.selectedCipher!! in next.guesses)
    }

    @Test
    fun eraseAndUndo() {
        val s = GameState.new(puzzle())
        val c = s.selectedCipher!!
        val guessed = s.guess('K', false)
        val erased = guessed.erase()
        assertNull(erased.guesses[c])
        assertEquals('K', erased.undo().guesses[c])
        assertNull(erased.undo().undo().guesses[c])
    }

    @Test
    fun hintLocksLetterAndUsesHint() {
        val s = GameState.new(puzzle())
        val c = s.selectedCipher!!
        val h = s.hint(autoAdvance = false)
        assertTrue(h.isCorrect(c))
        assertTrue(c in h.locked)
        assertEquals(s.hintsLeft - 1, h.hintsLeft)
        // Locked letters ignore typing and erasing.
        val sel = h.selectCipher(c)
        assertEquals(sel.guesses, sel.guess('Z', false).guesses)
        assertEquals(sel.guesses, sel.erase().guesses)
    }

    @Test
    fun undoKeepsHints() {
        val s = GameState.new(puzzle())
        val c = s.selectedCipher!!
        val plain = s.puzzle.decrypt.getValue(c)
        val other = s.puzzle.cipherLetters.first { it != c }
        // Put the right letter on the wrong tile, then hint the right tile.
        val s1 = s.selectCipher(other).guess(plain, false)
        val s2 = s1.selectCipher(c).hint(false)
        val undone = s2.undo()
        assertEquals(plain, undone.guesses[c])
        assertEquals(1, undone.guesses.values.count { it == plain })
    }

    @Test
    fun solvingTheWholePuzzle() {
        var s = GameState.new(puzzle())
        s.puzzle.cipherLetters.forEach { c ->
            s = s.selectCipher(c).guess(s.puzzle.decrypt.getValue(c), autoAdvance = true)
        }
        assertTrue(s.isSolved)
        assertNull(s.selectedIndex)
        assertEquals(0, s.mistakes)
        // No more input once solved.
        assertEquals(s, s.select(0).guess('A', true))
    }

    @Test
    fun revealAllIsSolvedButMarkedAsGivenUp() {
        val s = GameState.new(puzzle()).revealAll()
        assertTrue(s.isSolved)
        assertTrue(s.gaveUp)
    }

    @Test
    fun everyDifficultyHasQuotesInEveryLanguage() {
        Language.entries.forEach { language ->
            Difficulty.entries.forEach { d ->
                assertTrue("$language $d", Quotes.forDifficulty(d, language).size >= 10)
            }
        }
        // All quotes are encryptable, and every letter in them belongs to the alphabet.
        Quotes.all.forEach { q ->
            val p = Puzzle.generate(q, Difficulty.EASY, Random(3))
            assertEquals(p.text, p.cipherText.map { p.decrypt[it] ?: it }.joinToString(""))
            assertTrue(q.text, p.text.filter { it.isLetter() }.all { it in q.language.alphabet })
        }
    }

    @Test
    fun polishLettersAreEncryptedAndTypeable() {
        val q = Quote("W Szczebrzeszynie chrząszcz brzmi w trzcinie.", "Jan Brzechwa", Language.POLISH)
        val map = Puzzle.derangement(Random(5), Language.POLISH.alphabet)
        assertEquals(35, map.values.toSet().size)
        assertTrue(map.none { (k, v) -> k == v })

        var s = GameState.new(Puzzle.generate(q, Difficulty.HARD, Random(4)))
        val cipherOfA = s.puzzle.encrypt.getValue('Ą')
        assertTrue(cipherOfA in s.puzzle.cipherText)
        s = s.select(s.puzzle.cipherText.indexOf(cipherOfA)).guess('ą', autoAdvance = false)
        assertTrue(s.isCorrect(cipherOfA))
        s.puzzle.cipherLetters.forEach { c ->
            s = s.select(s.puzzle.cipherText.indexOf(c)).guess(s.puzzle.decrypt.getValue(c), true)
        }
        assertTrue(s.isSolved)
    }

    @Test
    fun englishPuzzleRejectsPolishLetters() {
        val s = GameState.new(puzzle())
        assertEquals(s, s.guess('Ż', autoAdvance = true))
    }

    @Test
    fun randomQuoteAvoidsTheCurrentOne() {
        val current = Quotes.forDifficulty(Difficulty.EASY, Language.POLISH).first()
        repeat(30) { seed ->
            val next = Quotes.random(Difficulty.EASY, Language.POLISH, current.text.uppercase(), Random(seed))
            assertTrue(next.text != current.text)
            assertEquals(Language.POLISH, next.language)
        }
    }
}
