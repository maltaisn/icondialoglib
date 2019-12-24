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

import com.maltaisn.icondialog.utils.svg.PathTransformer.ArgumentType.*


/**
 * Class used to translate and scale paths.
 */
class PathTransformer(private val tx: Double, private val ty: Double,
                      private val sx: Double, private val sy: Double) {

    /**
     * Applied transform on path [tokens], returning new tokens.
     */
    fun applyTransform(tokens: PathTokens): PathTokens {
        val values = mutableListOf<Double>()
        var i = 0
        for (command in tokens.commands) {
            for (argType in COMMAND_ARGUMENTS[command.toUpperCase()] ?: continue) {
                val value = tokens.values[i]
                values += when (argType) {
                    POS_X -> (value + tx) * sx
                    POS_Y -> (value + ty) * sy
                    DIM_X -> value * sx
                    DIM_Y -> value * sy
                    NONE -> value
                }
                i++
            }
        }
        return PathTokens(tokens.commands.toList(), values)
    }

    /**
     * Possible types of argument for a path command.
     * Each argument type is transformed differently.
     */
    enum class ArgumentType {
        POS_X,
        POS_Y,
        DIM_X,
        DIM_Y,
        NONE
    }

    companion object {
        private val COMMAND_ARGUMENTS = mapOf(
                'M' to listOf(POS_X, POS_Y),
                'L' to listOf(POS_X, POS_Y),
                'H' to listOf(POS_X),
                'V' to listOf(POS_Y),
                'Q' to listOf(POS_X, POS_Y, POS_X, POS_Y),
                'T' to listOf(POS_X, POS_Y),
                'C' to listOf(POS_X, POS_Y, POS_X, POS_Y, POS_X, POS_Y),
                'S' to listOf(POS_X, POS_Y, POS_X, POS_Y),
                'A' to listOf(DIM_X, DIM_Y, NONE, NONE, NONE, POS_X, POS_Y),
                'Z' to emptyList())
    }

}
