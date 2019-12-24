/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.icondialog.utils.svg


/**
 * Used to split a SVG path string into commands and numeric values.
 * More info on SVG grammar at [https://www.w3.org/TR/SVG/paths.html#PathDataBNF].
 */
class PathTokenizer {

    private var valuesSinceCommand = 0
    private val commands = mutableListOf<Char>()
    private val values = mutableListOf<Double>()


    /**
     * Tokenize a SVG [path string][pathStr].
     * @throws SvgParseException Thrown on parse errors.
     */
    fun tokenize(pathStr: String): PathTokens {
        valuesSinceCommand = 0
        commands.clear()
        values.clear()

        // Parse commands and values
        var i = 0
        while (i < pathStr.length) {
            val c = pathStr[i]
            when {
                c == ',' || c.isWhitespace() -> {
                    i++
                }
                c.toUpperCase() in "MZLHVQTCSA" -> {
                    checkLastCommandArity(i)

                    // Add command
                    commands += c
                    valuesSinceCommand = 0
                    i++
                }
                else -> {
                    // Parse value starting at current index.
                    i = parseValue(pathStr, i)

                    // If there are too many values
                    val lastArity = lastCommandArity
                    if (valuesSinceCommand >= 2 * lastArity) {
                        if (lastArity == 0) {
                            // Path has a trailing value.
                            parseError("Trailing values", i)

                        } else {
                            // Multiple values sets, repeat command.
                            val last = commands.last()
                            commands += when (last) {
                                'M' -> 'L'
                                'm' -> 'l'
                                else -> last
                            }
                            valuesSinceCommand = lastArity
                        }
                    }
                }
            }
        }

        checkLastCommandArity(pathStr.length)

        return PathTokens(commands, values)
    }

    /**
     * Parse value starting at a [start] position and add it to [values].
     * Throws parse error if value is invalid.
     */
    private fun parseValue(str: String, start: Int): Int {
        // Parse value starting at current index.
        val end = findValueEndIndex(str, start)
        val valueStr = str.substring(start, end)
        val value = valueStr.toDoubleOrNull()

        if (valueStr == "." || value == null) {
            // Invalid or empty number literal
            if (valueStr.isEmpty()) {
                parseError("Invalid character '${str[start + 1]}'", start)
            } else {
                parseError("Invalid number literal '$valueStr'", start)
            }
        } else {
            // Add value
            values += value
            valuesSinceCommand++
        }
        return end
    }

    /**
     * Find the end index of a value at a [start] position.
     */
    private fun findValueEndIndex(str: String, start: Int): Int {
        if (lastCommand == 'A' && valuesSinceCommand % 7 in 3..4 && str[start] in '0'..'1') {
            // Special case for valid syntax: "A10,10 0 1110,10" equivalent to "A10,10 0 1 1 10,10".
            return start + 1
        }

        var i = start
        var valueHasPoint = false
        var exponentPos = -1
        while (i < str.length) {
            when (str[i]) {
                '.' -> {
                    if (valueHasPoint) {
                        return i
                    } else {
                        valueHasPoint = true
                    }
                }
                'e', 'E' -> {
                    if (exponentPos != -1) {
                        return i
                    } else {
                        exponentPos = i
                    }
                }
                '+', '-' -> {
                    if (i != start && i != exponentPos + 1) {
                        return i
                    }
                }
                !in '0'..'9' -> {
                    return i
                }
            }
            i++
        }
        return i
    }

    /**
     * Check if last command has the correct number of values.
     */
    private fun checkLastCommandArity(i: Int) {
        // Check if previous command has the right number of values.
        val lastArity = lastCommandArity
        if (valuesSinceCommand < lastArity) {
            // Not enough values. Even lenient can't recover from this.
            parseError("Not enough values on command", i)
        } else if (valuesSinceCommand > lastArity) {
            // Too many values.
            parseError("Too many values on command", i)
        }
    }

    private val lastCommand: Char?
        get() = commands.lastOrNull()?.toUpperCase()

    private val lastCommandArity: Int
        get() = COMMAND_ARITY[lastCommand] ?: 0

    companion object {
        /** The number of values each command expects to find. */
        val COMMAND_ARITY = mapOf(
                'M' to 2,
                'Z' to 0,
                'L' to 2,
                'H' to 1,
                'V' to 1,
                'Q' to 4,
                'T' to 2,
                'C' to 6,
                'S' to 4,
                'A' to 7)
    }
}
