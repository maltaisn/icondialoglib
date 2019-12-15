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

package com.maltaisn.icondialog

import org.junit.Test
import kotlin.test.assertEquals


internal class StringNormalizeTest {

    @Test
    fun `should remove whitespace`() {
        assertEquals("ab", "a \t\n\rb".normalize())
    }

    @Test
    fun `should put to lowercase`() {
        assertEquals("abcdefghi", "AbCdEfGHI".normalize())
    }

    @Test
    fun `should remove diacritics`() {
        assertEquals("aei", "äéì".normalize())
    }

    @Test
    fun `should keep digits`() {
        assertEquals("0123456789", "0123456789".normalize())
    }

    @Test
    fun `should flatten unicode`() {
        assertEquals("1720", "⑰⑳".normalize())
    }

}
