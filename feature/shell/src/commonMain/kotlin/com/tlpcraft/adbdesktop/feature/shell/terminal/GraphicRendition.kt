package com.tlpcraft.adbdesktop.feature.shell.terminal

import com.tlpcraft.adbdesktop.uikit.component.TerminalColor
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan

private const val BOLD = 1
private const val DIM = 2
private const val UNDERLINED = 4
private const val INVERTED = 7
private const val NORMAL_INTENSITY = 22
private const val NOT_UNDERLINED = 24
private const val NOT_INVERTED = 27
private const val FOREGROUND_DEFAULT = 39
private const val BACKGROUND_DEFAULT = 49
private const val EXTENDED_FOREGROUND = 38
private const val EXTENDED_BACKGROUND = 48
private const val INDEXED = 5
private const val RGB = 2
private const val RGB_LENGTH = 4
private const val INDEXED_LENGTH = 2
private const val BASE_COLORS = 16
private const val CUBE_START = 16
private const val GRAY_START = 232
private const val CUBE_SIDE = 6
private const val CUBE_LEVEL = 51
private const val GRAY_STEP = 10
private const val GRAY_BASE = 8
private const val HALF_LEVEL = 128
private const val BRIGHT_LEVEL = 192
private const val RED_BIT = 1
private const val GREEN_BIT = 2
private const val BLUE_BIT = 4

private const val FIRST_FOREGROUND = 30
private const val FIRST_BACKGROUND = 40
private const val FIRST_BRIGHT_FOREGROUND = 90
private const val FIRST_BRIGHT_BACKGROUND = 100
private const val COLOR_COUNT = 8
private const val RGB_BLUE_OFFSET = 3

private val FOREGROUNDS = FIRST_FOREGROUND until FIRST_FOREGROUND + COLOR_COUNT
private val BACKGROUNDS = FIRST_BACKGROUND until FIRST_BACKGROUND + COLOR_COUNT
private val BRIGHT_FOREGROUNDS = FIRST_BRIGHT_FOREGROUND until FIRST_BRIGHT_FOREGROUND + COLOR_COUNT
private val BRIGHT_BACKGROUNDS = FIRST_BRIGHT_BACKGROUND until FIRST_BRIGHT_BACKGROUND + COLOR_COUNT

/** The style after a `CSI ... m` sequence. */
internal fun TerminalSpan.withRendition(values: List<Int?>): TerminalSpan {
    if (values.isEmpty()) return BLANK_STYLE
    var style = this
    var index = 0
    while (index < values.size) {
        val code = values[index] ?: 0
        if (code == EXTENDED_FOREGROUND || code == EXTENDED_BACKGROUND) {
            val color = values.extendedColor(index + 1)
            style = if (code == EXTENDED_FOREGROUND) style.copy(foreground = color) else style.copy(background = color)
            index += values.extendedLength(index + 1)
        } else {
            style = style.withCode(code)
        }
        index++
    }
    return style
}

private fun TerminalSpan.withCode(code: Int): TerminalSpan = when (code) {
    0 -> BLANK_STYLE
    BOLD -> copy(isBold = true)
    DIM -> copy(isDim = true)
    UNDERLINED -> copy(isUnderlined = true)
    INVERTED -> copy(isInverted = true)
    NORMAL_INTENSITY -> copy(isBold = false, isDim = false)
    NOT_UNDERLINED -> copy(isUnderlined = false)
    NOT_INVERTED -> copy(isInverted = false)
    FOREGROUND_DEFAULT -> copy(foreground = TerminalColor.Default)
    BACKGROUND_DEFAULT -> copy(background = TerminalColor.Default)
    in FOREGROUNDS -> copy(foreground = baseColor(code - FOREGROUNDS.first))
    in BACKGROUNDS -> copy(background = baseColor(code - BACKGROUNDS.first))
    in BRIGHT_FOREGROUNDS -> copy(foreground = baseColor(code - BRIGHT_FOREGROUNDS.first, isBright = true))
    in BRIGHT_BACKGROUNDS -> copy(background = baseColor(code - BRIGHT_BACKGROUNDS.first, isBright = true))
    else -> this
}

private fun List<Int?>.extendedLength(start: Int): Int = when (getOrNull(start)) {
    INDEXED -> INDEXED_LENGTH
    RGB -> RGB_LENGTH
    else -> 0
}

private fun List<Int?>.extendedColor(start: Int): TerminalColor = when (getOrNull(start)) {
    INDEXED -> indexedColor(getOrNull(start + 1) ?: 0)
    RGB -> nearestColor(getOrNull(start + 1) ?: 0, getOrNull(start + 2) ?: 0, getOrNull(start + RGB_BLUE_OFFSET) ?: 0)
    else -> TerminalColor.Default
}

private fun indexedColor(index: Int): TerminalColor = when {
    index < BASE_COLORS -> {
        baseColor(index % COLOR_COUNT, isBright = index >= COLOR_COUNT)
    }

    index >= GRAY_START -> {
        (GRAY_BASE + (index - GRAY_START) * GRAY_STEP).let { nearestColor(it, it, it) }
    }

    else -> {
        val cube = index - CUBE_START
        nearestColor(
            cube / (CUBE_SIDE * CUBE_SIDE) * CUBE_LEVEL,
            cube / CUBE_SIDE % CUBE_SIDE * CUBE_LEVEL,
            cube % CUBE_SIDE * CUBE_LEVEL,
        )
    }
}

/** The theme defines the terminal colors, so finer shades map to the closest of them. */
private fun nearestColor(red: Int, green: Int, blue: Int): TerminalColor {
    val bits = listOf(red to RED_BIT, green to GREEN_BIT, blue to BLUE_BIT).filter { (level) -> level >= HALF_LEVEL }.sumOf { (_, bit) -> bit }
    return baseColor(bits, isBright = maxOf(red, green, blue) >= BRIGHT_LEVEL)
}

private fun baseColor(index: Int, isBright: Boolean = false): TerminalColor {
    val first = if (isBright) TerminalColor.BrightBlack else TerminalColor.Black
    return TerminalColor.entries[first.ordinal + index]
}
