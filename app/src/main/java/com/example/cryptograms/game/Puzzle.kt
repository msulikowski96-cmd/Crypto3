package com.example.cryptograms.game

import kotlin.random.Random

data class Quote(val text: String, val author: String, val language: Language = Language.ENGLISH)

enum class Difficulty(
    /** Share of distinct letters revealed at the start. */
    val revealRatio: Double,
    val hints: Int,
    val lengthRange: IntRange,
) {
    EASY(revealRatio = 0.30, hints = 3, lengthRange = 0..42),
    MEDIUM(revealRatio = 0.12, hints = 2, lengthRange = 30..72),
    HARD(revealRatio = 0.0, hints = 1, lengthRange = 56..Int.MAX_VALUE),
}

/**
 * One encrypted quote.
 *
 * @param text the plain quote in upper case.
 * @param encrypt plain letter -> cipher letter (a derangement: no letter maps to itself).
 * @param revealed cipher letters shown from the start.
 */
class Puzzle(
    val text: String,
    val language: Language,
    val author: String,
    val difficulty: Difficulty,
    val encrypt: Map<Char, Char>,
    val revealed: Set<Char>,
    val originalText: String = text,
) {
    /** cipher letter -> plain letter */
    val decrypt: Map<Char, Char> = encrypt.entries.associate { (plain, cipher) -> cipher to plain }

    /** The text as the player sees it: letters encrypted, everything else unchanged. */
    val cipherText: String = text.map { encrypt[it] ?: it }.joinToString("")

    /** Distinct cipher letters, in reading order. */
    val cipherLetters: List<Char> = cipherText.filter { it in decrypt }.toList().distinct()

    companion object {
        fun generate(
            quote: Quote,
            difficulty: Difficulty,
            random: Random = Random.Default,
        ): Puzzle {
            val text = quote.text.uppercase()
            val alphabet = quote.language.alphabet
            val encrypt = derangement(random, alphabet)
            val plainLetters = text.filter { it in alphabet }.toList().distinct()
            val revealCount = (plainLetters.size * difficulty.revealRatio).toInt()
                .coerceAtMost(plainLetters.size - 1)
            val revealed = plainLetters.shuffled(random)
                .take(revealCount)
                .map { encrypt.getValue(it) }
                .toSet()
            return Puzzle(text, quote.language, quote.author, difficulty, encrypt, revealed, quote.text)
        }

        /** A random substitution of [alphabet] where no letter maps to itself. */
        fun derangement(random: Random, alphabet: List<Char> = Language.ENGLISH.alphabet): Map<Char, Char> {
            while (true) {
                val shuffled = alphabet.shuffled(random)
                if (alphabet.indices.none { alphabet[it] == shuffled[it] }) {
                    return alphabet.zip(shuffled).toMap()
                }
            }
        }
    }
}
