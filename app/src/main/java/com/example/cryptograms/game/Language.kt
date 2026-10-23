package com.example.cryptograms.game

/** Language of the quotes: decides which letters get encrypted and the keyboard layout. */
enum class Language(val alphabet: List<Char>, val keyboardRows: List<String>) {
    ENGLISH(
        alphabet = ('A'..'Z').toList(),
        keyboardRows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"),
    ),
    POLISH(
        alphabet = ('A'..'Z').toList() + "ĄĆĘŁŃÓŚŹŻ".toList(),
        keyboardRows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM", "ĄĆĘŁŃÓŚŹŻ"),
    ),
}
