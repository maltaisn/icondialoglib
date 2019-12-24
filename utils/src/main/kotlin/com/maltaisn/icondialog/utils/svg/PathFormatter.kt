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

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


/**
 * Class used to format path objects to SVG path strings.
 * @param precision Precision of formatted numbers.
 */
class PathFormatter(private val precision: Int) {

    private val numberFmt = DecimalFormat().apply {
        maximumFractionDigits = precision
        decimalFormatSymbols = DecimalFormatSymbols().apply {
            isGroupingUsed = false
            decimalSeparator = '.'
        }
    }

    /**
     * Create an optimized SVG path string from path [tokens].
     *
     * While resulting string is somewhat optimized, there are most optimizations possible:
     * - Compacting arc flags without separator eg: 'A10 10 30 0020 20'. This is not supported
     * by the Android path parser.
     * - Folding very big or very small numbers with scientific notation eg: '10000' becomes '1e4'
     * and '.0001' becomes '1e-4'. Since icons should be smaller than 100px, this optimization
     * would be useless.
     */
    fun format(tokens: PathTokens): String {
        val sb = StringBuilder()

        var lastCommand = '_'
        var lastNbStr = ""
        var i = 0
        for (command in tokens.commands) {
            // Append command char
            if (!(lastCommand == command
                    || lastCommand == 'M' && command == 'L'
                    || lastCommand == 'm' && command == 'l')) {
                // - Command can be ommited if previous command is the same.
                // - Line to command can be ommited if placed immediately after a move to command.
                sb.append(command)
                lastNbStr = ""
            }
            lastCommand = command

            // Append all values for that command
            repeat(PathTokenizer.COMMAND_ARITY[command.toUpperCase()] ?: 0) {
                val n = tokens.values[i]

                // Format value to string.
                // Remove leading zero if value is -1 < n < 1 and n != 0
                var nbStr = numberFmt.format(n)
                if (n > -1.0 && n != 0.0 && n < 1.0) {
                    nbStr = nbStr.replaceFirst("0", "")
                }

                // Append space separator if value has no sign '-' or decimal point '.'
                // to separate it from previous value, and if value isn't the first after command.
                if (lastNbStr != "" && n >= 0.0 && ('.' !in lastNbStr || !nbStr.startsWith('.'))) {
                    sb.append(' ')
                }

                // Append value.
                sb.append(nbStr)

                lastNbStr = nbStr
                i++
            }
        }

        return sb.toString()
    }

}
