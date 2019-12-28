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


internal class PathTransformerTest {

    private val transformer = PathTransformer(5.0, 10.0, 1.2, 0.8)

    @Test
    fun `transform move to absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'M'), listOf(10.0, 10.0)))
        assertEquals(listOf(18.0, 16.0), newTokens.values)
    }

    @Test
    fun `transform move to relative`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'm'), listOf(10.0, 10.0)))
        assertEquals(listOf(12.0, 8.0), newTokens.values)
    }

    @Test
    fun `transform close path`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'Z'), emptyList()))
        assertEquals(emptyList(), newTokens.values)
    }

    @Test
    fun `transform line to absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'L'), listOf(10.0, 10.0)))
        assertEquals(listOf(18.0, 16.0), newTokens.values)
    }

    @Test
    fun `transform horizontal absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'H'), listOf(10.0)))
        assertEquals(listOf(18.0), newTokens.values)
    }

    @Test
    fun `transform vertical absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'V'), listOf(10.0)))
        assertEquals(listOf(16.0), newTokens.values)
    }

    @Test
    fun `transform quadratic absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'Q'), listOf(10.0, 10.0, 15.0, 5.0)))
        assertEquals(listOf(18.0, 16.0, 24.0, 12.0), newTokens.values)
    }

    @Test
    fun `transform quadratic shorthand absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'T'), listOf(15.0, 5.0)))
        assertEquals(listOf(24.0, 12.0), newTokens.values)
    }

    @Test
    fun `transform cubic absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'C'), listOf(10.0, 10.0, 15.0, 5.0, -10.0, -10.0)))
        assertEquals(listOf(18.0, 16.0, 24.0, 12.0, -6.0, 0.0), newTokens.values)
    }

    @Test
    fun `transform cubic shorthand absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'S'), listOf(15.0, 5.0, 10.0, 10.0)))
        assertEquals(listOf(24.0, 12.0, 18.0, 16.0), newTokens.values)
    }

    @Test
    fun `transform arc absolute`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf( 'A'), listOf(10.0, 20.0, 30.0, 1.0, 0.0, 20.0, 25.0)))
        assertEquals(listOf(12.0, 16.0, 30.0, 1.0, 0.0, 30.0, 28.0), newTokens.values)
    }

    @Test
    fun `transform multiple`() {
        val newTokens = transformer.applyTransform(PathTokens(
                listOf('M', 'h', 'v', 'h', 'Z'), listOf(10.0, 10.0, 30.0, 30.0, -30.0)))
        assertEquals(listOf(18.0, 16.0, 36.0, 24.0, -36.0), newTokens.values)
    }

}
