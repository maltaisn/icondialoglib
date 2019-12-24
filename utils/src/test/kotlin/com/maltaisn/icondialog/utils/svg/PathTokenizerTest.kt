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


internal class PathTokenizerTest {

    private val tokenizer = PathTokenizer()

    @Test
    fun `single command with coordinate space separated`() {
        val tokens = tokenizer.tokenize("M 10 10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, 10.0), tokens.values)
    }

    @Test
    fun `single command with coordinate comma separated`() {
        val tokens = tokenizer.tokenize("M,10,10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, 10.0), tokens.values)
    }

    @Test
    fun `bunch of unused separators`() {
        val tokens = tokenizer.tokenize("  ,, , M,, , ,, ,10,  ,, ,10, ,, , ")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, 10.0), tokens.values)
    }

    @Test
    fun `single command with fractional coordinate`() {
        val tokens = tokenizer.tokenize("M 10.0 0.10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, 0.1), tokens.values)
    }

    @Test
    fun `single command with fractional coordinate no separator`() {
        val tokens = tokenizer.tokenize("M10..10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, 0.1), tokens.values)
    }

    @Test
    fun `single command with exponent coordinate`() {
        val tokens = tokenizer.tokenize("M 9.9E-1 1e+2")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(0.99, 100.0), tokens.values)
    }

    @Test
    fun `single command with exponent coordinate no separator`() {
        val tokens = tokenizer.tokenize("M9.e-1-.2E1")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(0.9, -2.0), tokens.values)
    }

    @Test
    fun `single command with signed coordinate`() {
        val tokens = tokenizer.tokenize("M +10 -10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, -10.0), tokens.values)
    }

    @Test
    fun `single command with signed coordinate no separator`() {
        val tokens = tokenizer.tokenize("M+10-10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, -10.0), tokens.values)
    }

    @Test
    fun `single command with signed coordinate 2 no separator`() {
        val tokens = tokenizer.tokenize("M10-10")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(10.0, -10.0), tokens.values)
    }

    @Test
    fun `single command with signed fractional coordinate no separator`() {
        val tokens = tokenizer.tokenize("M+1.-.1")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(1.0, -0.1), tokens.values)
    }

    @Test
    fun `single command with fractional coordinate without leading zero no separator`() {
        val tokens = tokenizer.tokenize("M.1.2")
        assertEquals(listOf('M'), tokens.commands)
        assertEquals(listOf(0.1, 0.2), tokens.values)
    }

    @Test
    fun `multiple command with coordinates`() {
        val tokens = tokenizer.tokenize("M 0,0 L 10,10 h 10 v-10 Z")
        assertEquals(listOf('M', 'L', 'h', 'v', 'Z'), tokens.commands)
        assertEquals(listOf(0.0, 0.0, 10.0, 10.0, 10.0, -10.0), tokens.values)
    }

    @Test
    fun `multiple command with coordinates dirty`() {
        val tokens = tokenizer.tokenize("M-.1.1l2.,+.2H10v10z")
        assertEquals(listOf('M', 'l', 'H', 'v', 'z'), tokens.commands)
        assertEquals(listOf(-0.1, 0.1, 2.0, 0.2, 10.0, 10.0), tokens.values)
    }

    @Test
    fun `implicit polyline with absolute M`() {
        val tokens = tokenizer.tokenize("M10,10,20,20,30,30,40,40")
        assertEquals(listOf('M', 'L', 'L', 'L'), tokens.commands)
        assertEquals(listOf(10.0, 10.0, 20.0, 20.0, 30.0, 30.0, 40.0, 40.0), tokens.values)
    }

    @Test
    fun `implicit polyline with relative M`() {
        val tokens = tokenizer.tokenize("m10,10,20,20,30,30,40,40")
        assertEquals(listOf('m', 'l', 'l', 'l'), tokens.commands)
        assertEquals(listOf(10.0, 10.0, 20.0, 20.0, 30.0, 30.0, 40.0, 40.0), tokens.values)
    }

    @Test
    fun `polyline with extra L coordinate pairs`() {
        val tokens = tokenizer.tokenize("M0,0 L10,10, 10,20, 20,20, 20,10")
        assertEquals(listOf('M', 'L', 'L', 'L', 'L'), tokens.commands)
        assertEquals(listOf(0.0, 0.0, 10.0, 10.0, 10.0, 20.0, 20.0, 20.0, 20.0, 10.0), tokens.values)
    }

    @Test
    fun `polyline with extra H,V values`() {
        val tokens = tokenizer.tokenize("M0,0 h20 10 5 v20 10 5")
        assertEquals(listOf('M', 'h', 'h', 'h', 'v', 'v', 'v'), tokens.commands)
        assertEquals(listOf(0.0, 0.0, 20.0, 10.0, 5.0, 20.0, 10.0, 5.0), tokens.values)
    }

    @Test
    fun `polyline with extra H,V values no separators`() {
        val tokens = tokenizer.tokenize("M0,0h20 10 5v20 10 5")
        assertEquals(listOf('M', 'h', 'h', 'h', 'v', 'v', 'v'), tokens.commands)
        assertEquals(listOf(0.0, 0.0, 20.0, 10.0, 5.0, 20.0, 10.0, 5.0), tokens.values)
    }

    @Test
    fun `polybezier with extra Q coordinate pairs`() {
        val tokens = tokenizer.tokenize("M0,0 Q10,10 20,0 30,-10 40,0 50,10 60,0")
        assertEquals(listOf('M', 'Q', 'Q', 'Q'), tokens.commands)
        assertEquals(listOf(0.0, 0.0, 10.0, 10.0, 20.0, 0.0, 30.0, -10.0, 40.0, 0.0, 50.0, 10.0, 60.0, 0.0), tokens.values)
    }

    @Test
    fun `arc without separator between flags`() {
        val tokens = tokenizer.tokenize("A10,10 0 1020,10")
        assertEquals(listOf('A'), tokens.commands)
        assertEquals(listOf(10.0, 10.0, 0.0, 1.0, 0.0, 20.0, 10.0), tokens.values)
    }

    @Test
    fun `multiarc without separator between flags`() {
        val tokens = tokenizer.tokenize("A10,10 0 1020,10 10,10 0 110,10")
        assertEquals(listOf('A', 'A'), tokens.commands)
        assertEquals(listOf(10.0, 10.0, 0.0, 1.0, 0.0, 20.0, 10.0, 10.0, 10.0, 0.0, 1.0, 1.0, 0.0, 10.0), tokens.values)
    }

    @Test(expected = SvgParseException::class)
    fun `fail trailing values start`() {
        tokenizer.tokenize("10,10M10,10Z")
    }

    @Test(expected = SvgParseException::class)
    fun `fail trailing values end`() {
        tokenizer.tokenize("M10,10Z10")
    }

    @Test(expected = SvgParseException::class)
    fun `missing values fail`() {
        tokenizer.tokenize("M10Z")
    }

    @Test(expected = SvgParseException::class)
    fun `extra values fail`() {
        tokenizer.tokenize("M10,10,10Z")
    }

    @Test(expected = SvgParseException::class)
    fun `unknown character fail`() {
        tokenizer.tokenize("M10,10@@@L10,10")
    }

    @Test(expected = SvgParseException::class)
    fun `invalid number literal fail`() {
        tokenizer.tokenize("M-.10..0.1")
    }

}
