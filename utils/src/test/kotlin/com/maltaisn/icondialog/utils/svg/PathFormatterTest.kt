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

import org.junit.Test
import kotlin.test.assertEquals


internal class PathFormatterTest {

    private val formatter = PathFormatter(1)

    @Test
    fun `single command`() {
        val pathStr = formatter.format(PathTokens(listOf('M'), listOf(10.0, 10.0)))
        assertEquals("M10 10", pathStr)
    }

    @Test
    fun `multiple command`() {
        val pathStr = formatter.format(PathTokens(listOf('M', 'h', 'v', 'h', 'Z'),
                listOf(10.0, 10.0, 30.0, 30.0, -30.0)))
        assertEquals("M10 10h30v30h-30Z", pathStr)
    }

    @Test
    fun `multiple command repeated`() {
        val pathStr = formatter.format(PathTokens(listOf('M', 'Q', 'Q'),
                listOf(10.0, 10.0, 30.0, -30.0, 10.0, -30.0, 0.0, 0.0, -10.0, 10.0)))
        assertEquals("M10 10Q30-30 10-30 0 0-10 10", pathStr)
    }

    @Test
    fun `multiple command implicit line to`() {
        val pathStr = formatter.format(PathTokens(listOf('M', 'L', 'L', 'L'),
                listOf(10.0, 10.0, 30.0, -30.0, 10.0, -30.0, 0.0, 0.0)))
        assertEquals("M10 10 30-30 10-30 0 0", pathStr)
    }

    @Test
    fun `no leading zero`() {
        val pathStr = formatter.format(PathTokens(listOf('M', 'Q'),
                listOf(0.1, -0.1, 1.0, -1.0, 0.0, 0.5)))
        assertEquals("M.1-.1Q1-1 0 .5", pathStr)
    }

    @Test
    fun `no whitespace if sign and decimal point allow it`() {
        val pathStr = formatter.format(PathTokens(listOf('M', 'Q', 'L', 'Z'),
                listOf(0.1, 0.1, 0.0, 0.1, 0.0, -0.1, 0.1, 1.1)))
        assertEquals("M.1.1Q0 .1 0-.1L.1 1.1Z", pathStr)
    }

    @Test
    fun `minus zero`() {
        val pathStr = formatter.format(PathTokens(listOf('M'),
                listOf(-0.0, -0.001)))
        assertEquals("M0 0", pathStr)
    }

}
